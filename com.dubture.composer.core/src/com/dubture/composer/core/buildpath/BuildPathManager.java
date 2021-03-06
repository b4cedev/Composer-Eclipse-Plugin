package com.dubture.composer.core.buildpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.php.internal.core.buildpath.BuildPathUtils;

import com.dubture.composer.core.ComposerPlugin;
import com.dubture.composer.core.ComposerPluginConstants;
import com.dubture.composer.core.log.Logger;
import com.dubture.composer.core.resources.IComposerProject;

@SuppressWarnings("restriction")
public class BuildPathManager {

	private IComposerProject composerProject;
	private IPath[] exclusions; // gnoar, shouldn't be a property, what did I thought? DI 'n stuff...
	private IPath vendorPath;
	private IPath composerPath;
	
	public BuildPathManager(IComposerProject composerProject) {
		this.composerProject = composerProject;
		vendorPath = composerProject.getProject().getFullPath().append(composerProject.getVendorDir());
		composerPath = vendorPath.append("composer");
	}
	
	public void update() throws CoreException {
		update(new NullProgressMonitor());
	}
	
	public void update(IProgressMonitor monitor) throws CoreException {
		IProject project = composerProject.getProject();
		IScriptProject scriptProject = composerProject.getScriptProject();
		BuildPathParser parser = new BuildPathParser(composerProject);
		List<String> paths = parser.getPaths();
		
		// project prefs
		IEclipsePreferences prefs = ComposerPlugin.getDefault().getProjectPreferences(project);
		IPath[] inclusions;

		try {
			String encoded = prefs.get(ComposerPluginConstants.BUILDPATH_INCLUDES_EXCLUDES, "");
			exclusions = scriptProject.decodeBuildpathEntry(encoded).getExclusionPatterns();
			inclusions = scriptProject.decodeBuildpathEntry(encoded).getInclusionPatterns();
		} catch (Exception e) {
			exclusions = new IPath[]{};
			inclusions = new IPath[]{};
		}
		
		// add includes
		for (IPath inclusion : inclusions) {
			paths.add(inclusion.toString());
		}
		
		// clean up exclusion patterns: remove exact matches
		List<IPath> exs = new ArrayList<IPath>();
		for (IPath exclusion : exclusions) {
			String exc = exclusion.removeTrailingSeparator().toString();
			
			if (paths.contains(exc)) {
				paths.remove(exc);
			} else {
				exs.add(exclusion);
			}
		}
		exclusions = exs.toArray(new IPath[]{});
		
		// clean build path
		IBuildpathEntry[] rawBuildpath = scriptProject.getRawBuildpath();
		for (IBuildpathEntry entry : rawBuildpath) {
			if (entry.getEntryKind() == IBuildpathEntry.BPE_SOURCE) {
				BuildPathUtils.removeEntryFromBuildPath(scriptProject, entry);
			}
		}
		
		// sort paths for nesting detection
		Collections.sort(paths);
		
		// add new entries to buildpath
		List<IBuildpathEntry> newEntries = new ArrayList<IBuildpathEntry>();
		for (String path : paths) {
			IPath entry = new Path(path);
			IFolder folder = project.getFolder(entry);
			if (folder != null && folder.exists()) {
				addPath(folder.getFullPath(), newEntries);
			}
		}

		if (newEntries.size() > 0) {
			BuildPathUtils.addNonDupEntriesToBuildPath(scriptProject, newEntries);
		}

		IFolder folder = project.getFolder(new Path(composerProject.getVendorDir()));
		
		if (folder != null && folder.exists() && !folder.isDerived()) {
			folder.setDerived(true, monitor);
		}
	}
	
	private void addPath(IPath path, List<IBuildpathEntry> entries) {
		// find parent
		IBuildpathEntry parent = null;
		int parentLength = 0;
		IPath entryPath;
		for (IBuildpathEntry entry : entries) {
			entryPath = entry.getPath();
			if (entryPath.isPrefixOf(path) 
					&& (parent == null || (entryPath.toString().length() > parentLength))) {
				parent = entry;
				parentLength = parent.getPath().toString().length();
			}
		}
		
		// add path as exclusion to found parent
		if (parent != null) {
			List<IPath> exclusions = new ArrayList<IPath>(); 
			exclusions.addAll(Arrays.asList(parent.getExclusionPatterns()));
			
			IPath diff = path.removeFirstSegments(path.matchingFirstSegments(parent.getPath()));
			if (parent.getPath().equals(composerPath)) {
				diff = path.uptoSegment(1);
			}
			diff = diff.removeTrailingSeparator().addTrailingSeparator();
			if (!exclusions.contains(diff)) {
				exclusions.add(diff);
			}
			
			entries.remove(parent);
			
			parent = DLTKCore.newSourceEntry(parent.getPath(), exclusions.toArray(new IPath[]{}));
			entries.add(parent);
		}
		
		// add own entry
		// leave vendor/composer untouched with exclusions
		if (vendorPath.equals(path) || composerPath.equals(path)) {
			entries.add(DLTKCore.newSourceEntry(path));
			
		// add exclusions
		} else {
			List<IPath> ex = new ArrayList<IPath>();
			
			// find the applying exclusion patterns for the new entry
			for (IPath exclusion : exclusions) {
				
				if (!exclusion.toString().startsWith("*")) {
					exclusion = composerProject.getProject().getFullPath().append(exclusion);
				}
				
				// remove buildpath entries with exact exclusion matches
				if (path.equals(exclusion)) {
					return;
				}
				
				// if exclusion matches path, add the trailing path segments as exclusion
				if (path.removeTrailingSeparator().isPrefixOf(exclusion)) {
					ex.add(exclusion.removeFirstSegments(path.matchingFirstSegments(exclusion)));
				}
				
				// if exclusion starts with wildcard, add also
				else if (exclusion.toString().startsWith("*")) {
					ex.add(exclusion);
				}
			}
 
			entries.add(DLTKCore.newSourceEntry(path, ex.toArray(new IPath[]{})));			
		}
	}
	
	// is this method necessary at all?
	public static void setExclusionPattern(IScriptProject project, IBuildpathEntry entry) {

		try {
			String encoded = project.encodeBuildpathEntry(entry);
			IEclipsePreferences prefs = ComposerPlugin.getDefault().getProjectPreferences(project.getProject());
			prefs.put(ComposerPluginConstants.BUILDPATH_INCLUDES_EXCLUDES, encoded);
		} catch (Exception e) {
			Logger.logException(e);
		}
	}
}
