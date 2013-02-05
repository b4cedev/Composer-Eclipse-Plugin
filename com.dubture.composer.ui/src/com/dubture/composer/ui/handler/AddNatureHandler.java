package com.dubture.composer.ui.handler;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

import com.dubture.composer.core.ComposerNature;
import com.dubture.composer.core.log.Logger;
import com.dubture.indexing.core.ExtensionManager;
import com.dubture.indexing.core.build.BuildParticipant;

public class AddNatureHandler extends ComposerHandler implements IHandler
{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        try {
            init(event);
        } catch (Exception e) {
            Logger.logException(e);
        } finally {
            try {
                if (scriptProject.getProject() != null && ! scriptProject.getProject().hasNature(ComposerNature.NATURE_ID)) {
                    toggleNature();
                } else {
                    Logger.debug("No composer nature set");
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private void toggleNature()
    {
        try {

            IProjectDescription description = scriptProject.getProject().getDescription();
            String[] natures = description.getNatureIds();

            for (int i = 0; i < natures.length; ++i) {
                if (ComposerNature.NATURE_ID.equals(natures[i])) {
                    // Remove the nature
                    String[] newNatures = new String[natures.length - 1];
                    System.arraycopy(natures, 0, newNatures, 0, i);
                    System.arraycopy(natures, i + 1, newNatures, i,
                            natures.length - i - 1);
                    description.setNatureIds(newNatures);
                    scriptProject.getProject().setDescription(description, null);
                    return;
                }
            }

            // Add the nature
            String[] newNatures = new String[natures.length + 1];
            System.arraycopy(natures, 0, newNatures, 0, natures.length);
            newNatures[natures.length] = ComposerNature.NATURE_ID;
            description.setNatureIds(newNatures);
            scriptProject.getProject().setDescription(description, null);
            
            // add the lucene builder
            ExtensionManager manager = ExtensionManager.getInstance();
            List<BuildParticipant> participants = manager.getBuildParticipants();
            
            for (BuildParticipant participant : participants) {
                
                if (ComposerNature.NATURE_ID.equals(participant.getNature())) {
                    participant.addBuilder(scriptProject.getProject());
                    break;
                }
            }

        } catch (CoreException e) {

            Logger.logException(e);
        }
    }    
}
