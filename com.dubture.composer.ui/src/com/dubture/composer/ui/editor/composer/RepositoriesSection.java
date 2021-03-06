package com.dubture.composer.ui.editor.composer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.dubture.composer.ui.ComposerUIPluginImages;
import com.dubture.composer.ui.controller.IController;
import com.dubture.composer.ui.dialogs.RepositoryDialog;
import com.dubture.composer.ui.editor.ComposerFormPage;
import com.dubture.composer.ui.editor.FormLayoutFactory;
import com.dubture.composer.ui.editor.TableSection;
import com.dubture.composer.ui.parts.TablePart;
import com.dubture.getcomposer.core.collection.Repositories;
import com.dubture.getcomposer.core.repositories.ComposerRepository;
import com.dubture.getcomposer.core.repositories.GitRepository;
import com.dubture.getcomposer.core.repositories.MercurialRepository;
import com.dubture.getcomposer.core.repositories.PackageRepository;
import com.dubture.getcomposer.core.repositories.PearRepository;
import com.dubture.getcomposer.core.repositories.Repository;
import com.dubture.getcomposer.core.repositories.SubversionRepository;

public class RepositoriesSection extends TableSection implements PropertyChangeListener {

	private TableViewer repositoryViewer;
	
	private IAction addAction;
	private IAction editAction;
	private IAction removeAction;
	
	private static final int ADD_INDEX = 0;
	private static final int EDIT_INDEX = 1;
	private static final int REMOVE_INDEX = 2;

	class RepositoriesController extends LabelProvider implements IController {

		private Repositories repositories;
		private Map<String, Image> images = new HashMap<String, Image>();
		private Map<String, ImageDescriptor> descriptors = new HashMap<String, ImageDescriptor> () {
			private static final long serialVersionUID = -2019489473873127982L;

			{
				put("generic", ComposerUIPluginImages.REPO_GENERIC);
				put("git", ComposerUIPluginImages.REPO_GIT);
				put("svn", ComposerUIPluginImages.REPO_SVN);
				put("mercurial", ComposerUIPluginImages.REPO_MERCURIAL);
				put("pear", ComposerUIPluginImages.REPO_PEAR);
				put("composer", ComposerUIPluginImages.REPO_COMPOSER);
				put("package", ComposerUIPluginImages.REPO_PACKAGE);
			}
		};

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			repositories = (Repositories)newInput;
		}

		public Object[] getElements(Object inputElement) {
			return repositories.toArray();
		}
		
		@Override
		public String getText(Object element) {
			if (element instanceof Repository) {
				Repository repo = (Repository)element;
				String name;
				
				// name
				if (repo.has("name")) {
					name = repo.getAsString("name");
				} else {
					name = repo.getUrl();
				}
				
				return name;
			}

			return super.getText(element);
		}
		
		private Image createImage(String type) {
			if (descriptors.containsKey(type)) {
				return descriptors.get(type).createImage();
			}
			
			return null;
		}
		
		private Image getRepoImage(String type) {
			if (!images.containsKey(type)) {
				images.put(type, createImage(type));
			}
			return images.get(type);
		}
		
		@Override
		public Image getImage(Object element) {
			if (element instanceof GitRepository) {
				return getRepoImage("git");
			} else if (element instanceof SubversionRepository) {
				return getRepoImage("svn");
			} else if (element instanceof MercurialRepository) {
				return getRepoImage("mercurial");
			} else if (element instanceof PearRepository) {
				return getRepoImage("pear");
			} else if (element instanceof PackageRepository) {
				return getRepoImage("package");
			} else if (element instanceof ComposerRepository) {
				return getRepoImage("composer");
			} else if (element instanceof Repository) {
				return getRepoImage("generic");
			}
			
			return null;
		}
	}
	
	public RepositoriesSection(ComposerFormPage page, Composite parent) {
		super(page, parent, Section.DESCRIPTION, new String[]{"Add...", "Edit...", "Remove"});
		createClient(getSection(), page.getManagedForm().getToolkit());
	}
	

	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		section.setText("Repositories");
		section.setDescription("Manage repositories as sources for this package.");
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite container = createClientContainer(section, 2, toolkit);
		createViewerPartControl(container, SWT.MULTI, 2, toolkit);
		TablePart tablePart = getTablePart();
		RepositoriesController repositoriesController = new RepositoriesController();
		repositoryViewer = tablePart.getTableViewer();
		repositoryViewer.setContentProvider(repositoriesController);
		repositoryViewer.setLabelProvider(repositoriesController);
		
		toolkit.paintBordersFor(container);
		section.setClient(container);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));

		repositoryViewer.setInput(composerPackage.getRepositories());
		composerPackage.addPropertyChangeListener(this);
		updateButtons();
		
		makeActions();
		updateMenu();
	}
	
	protected boolean createCount() {
		return true;
	}
	
	private void updateButtons() {
		ISelection selection = repositoryViewer.getSelection();
		
		TablePart tablePart = getTablePart();
		tablePart.setButtonEnabled(EDIT_INDEX, !selection.isEmpty());
		tablePart.setButtonEnabled(REMOVE_INDEX, !selection.isEmpty());
	}
	
	private void updateMenu() {
		IStructuredSelection selection = (IStructuredSelection)repositoryViewer.getSelection();
		
		editAction.setEnabled(selection.size() > 0);
		removeAction.setEnabled(selection.size() > 0);
	}

	public void refresh() {
		repositoryViewer.refresh();
		super.refresh();
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().startsWith("repositories")) {
			refresh();
		}
	}
	
	protected void selectionChanged(IStructuredSelection sel) {
		updateButtons();
		updateMenu();
	}
	
	private void makeActions() {
		addAction = new Action("Add...") {
			public void run() {
				handleAdd();
			}
		};
		
		editAction = new Action("Edit...") {
			public void run() {
				handleEdit();
			}
		};
		
		removeAction = new Action("Remove") {
			public void run() {
				handleRemove();
			}
		};
	}
	
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(addAction);
		manager.add(editAction);
		manager.add(removeAction);
	}
	
	private void handleAdd() {
		RepositoryDialog diag = new RepositoryDialog(repositoryViewer.getTable().getShell());
		if (diag.open() == Dialog.OK) {
			composerPackage.getRepositories().add(diag.getRepository());
		}
	}
	
	private void handleEdit() {
		Repository repo = (Repository)((StructuredSelection)repositoryViewer.getSelection()).getFirstElement();
		RepositoryDialog diag = new RepositoryDialog(repositoryViewer.getTable().getShell(), repo.clone());
		if (diag.open() == Dialog.OK) {
			Repository newRepo = diag.getRepository();
			repo.setName(newRepo.getName());
			repo.setUrl(newRepo.getUrl());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void handleRemove() {
		StructuredSelection selection = ((StructuredSelection)repositoryViewer.getSelection());
		Iterator<Object> it = selection.iterator();
		String[] names = new String[selection.size()];
		List<Repository> repos = new ArrayList<Repository>();

		for (int i = 0; it.hasNext(); i++) {
			Repository repo = (Repository)it.next();
			repos.add(repo);
			names[i] = repo.getName();
		}
		
		MessageDialog diag = new MessageDialog(
				repositoryViewer.getTable().getShell(), 
				"Remove Repositor" + (selection.size() > 1 ? "ies" : "y"), 
				null, 
				"Do you really wan't to remove " + StringUtils.join(names, ", ") + "?", 
				MessageDialog.WARNING,
				new String[] {"Yes", "No"},
				0);
		
		if (diag.open() == Dialog.OK) {
			for (Repository repo : repos) {
				composerPackage.getRepositories().remove(repo);
			}
		}
	}
	
	@Override
	protected void buttonSelected(int index) {
		switch (index) {
		case ADD_INDEX:
			handleAdd();
			break;
			
		case EDIT_INDEX:
			handleEdit();
			break;
			
		case REMOVE_INDEX:
			handleRemove();
			break;
		}
	}
}
