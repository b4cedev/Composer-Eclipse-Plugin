package com.dubture.composer.ui.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.dubture.getcomposer.core.ComposerPackage;


public abstract class ComposerSection extends SectionPart {

	private ComposerFormPage page;
	protected ComposerPackage composerPackage;
	
	public ComposerSection(ComposerFormPage page, Composite parent, int style) {
		this(page, parent, style, true);
	}
	
	public ComposerSection(ComposerFormPage page, Composite parent, int style, boolean titleBar) {
		super(parent, page.getManagedForm().getToolkit(), titleBar ? (ExpandableComposite.TITLE_BAR | style) : style);
		this.page = page;
		composerPackage = page.getComposerEditor().getComposerPackge();
		initialize(page.getManagedForm());
	}
	
	public ComposerFormPage getPage() {
		return page;
	}
	
	protected abstract void createClient(Section section, FormToolkit toolkit);
}
