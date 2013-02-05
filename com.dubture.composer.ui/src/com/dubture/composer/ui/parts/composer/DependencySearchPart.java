package com.dubture.composer.ui.parts.composer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Twistie;

import com.dubture.composer.ui.utils.WidgetFactory;
import com.dubture.composer.ui.utils.WidgetHelper;

public class DependencySearchPart extends PackageSearchPart {

	protected Twistie toggle;
	protected VersionSuggestion suggestion;
	
	public DependencySearchPart(Composite parent, FormToolkit toolkit, String name) {
		super(parent, toolkit, name);
	}

	protected void create(Composite parent, WidgetFactory factory) {
		createBody(parent, factory);
		WidgetHelper.trimComposite(body, 0, 0, 0, 0, 0, 0);

		// title
		Composite title = factory.createComposite(body, SWT.NO_BACKGROUND);
		title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		title.setLayout(new GridLayout(3, false));
		WidgetHelper.trimComposite(title, -5,-5,-5,-5, 0, 0);

		// toggle box
		Composite toggleBox = factory.createComposite(title, SWT.NO_BACKGROUND);
		toggleBox.setLayout(new GridLayout());
		toggle = new Twistie(toggleBox, SWT.NO_BACKGROUND | SWT.NO_FOCUS);
		toggle.setExpanded(true); //TODO: REMOVE!
		toggle.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				boolean expanded = toggle.isExpanded();
				suggestion.getBody().setVisible(expanded);
				((GridData)suggestion.getBody().getLayoutData()).exclude = !expanded; 
				body.layout(true, true);
			}
		});
		WidgetHelper.trimComposite(toggleBox, 3, -7, 0, 0, 0, 0);
		
		// package
		createPackageCheckbox(title, factory, name);
		
		// version
		Text version = factory.createText(title, SWT.SINGLE | SWT.BORDER);
		version.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		// suggestion
		suggestion = new VersionSuggestion(name, body, version, toolkit);
		WidgetHelper.trimComposite(suggestion.getBody(), -5,-5,-5,-5, 0, 0);
	}
	
	public void addToggleListener(IHyperlinkListener listener) {
		toggle.addHyperlinkListener(listener);
	}
	
	public void removeToggleListener(IHyperlinkListener listener) {
		toggle.removeHyperlinkListener(listener);
	}
	
	public boolean isExpanded() {
		return toggle.isExpanded();
	}
}
