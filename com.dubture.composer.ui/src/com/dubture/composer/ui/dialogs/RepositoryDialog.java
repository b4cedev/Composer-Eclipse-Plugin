package com.dubture.composer.ui.dialogs;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dubture.composer.ui.ComposerUIPluginConstants;
import com.dubture.composer.ui.ComposerUIPluginImages;
import com.dubture.getcomposer.core.repositories.Repository;
import com.dubture.getcomposer.core.repositories.RepositoryFactory;

public class RepositoryDialog extends Dialog {

	private Repository repository;
	private String url;
	private String name;
	private String type;
	
	private BidiMap repos = new DualHashBidiMap() {
		private static final long serialVersionUID = 2864558369860037123L;
		{
			put("composer", "Composer");
			put("package", "Package");
			put("git", "Git");
			put("svn", "Subversion");
			put("hg", "Mercurial");
			put("pear", "Pear");
		}		
	};
	
	public RepositoryDialog(Shell parentShell, Repository repository) {
		super(parentShell);
		this.repository = repository;
	}
	
	public RepositoryDialog(IShellProvider parentShell, Repository repository) {
		super(parentShell);
		this.repository = repository;
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public RepositoryDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public RepositoryDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Repository");
		getShell().setImage(ComposerUIPluginImages.REPO_GENERIC.createImage());
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		
		Label lblType = new Label(container, SWT.NONE);
		GridData gd_lblType = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblType.widthHint = ComposerUIPluginConstants.DIALOG_LABEL_WIDTH;
		lblType.setLayoutData(gd_lblType);
		lblType.setText("Type");
		
		final Combo typeControl = new Combo(container, SWT.READ_ONLY);
		GridData gd_type = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_type.widthHint = ComposerUIPluginConstants.DIALOG_CONTROL_WIDTH;
		typeControl.setLayoutData(gd_type);
		typeControl.setItems((String[]) repos.values().toArray(new String[]{}));
		if (repository != null) {
			String type = repository.getType();
			if (repos.containsKey(type)) {
				typeControl.setText((String) repos.get(type));
			}
			typeControl.setEnabled(false);
		}
		typeControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				type = typeControl.getText();
			}
		});
		
		Label lblUrl = new Label(container, SWT.NONE);
		lblUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUrl.setText("URL");
		
		final Text urlControl = new Text(container, SWT.BORDER);
		urlControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (repository != null && repository.has("url")) {
			urlControl.setText(repository.getUrl());
		}
		urlControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (repository != null) {
					repository.setUrl(urlControl.getText());
				}
				url = urlControl.getText();
			}
		});
		
		Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name");
		
		final Text nameControl = new Text(container, SWT.BORDER);
		nameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (repository != null && repository.has("name")) {
			nameControl.setText(repository.getName());
		}
		nameControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (repository != null) {
					repository.setName(nameControl.getText());
				}
				name = nameControl.getText();
			}
		});
		
		return container;
	}
	
	public Repository getRepository() {
		if (repository != null) {
			return repository;
		}
		
		Repository repo = RepositoryFactory.create((String) repos.getKey(type));
		repo.setUrl(url);
		repo.setName(name);
		
		return repo;
	}
}
