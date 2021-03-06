package com.dubture.composer.ui.editor.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.dubture.composer.ui.ComposerUIPluginImages;

public class SearchControl extends ControlContribution {

	private final IManagedForm managedForm;

	Text searchText;
	
	private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

	public SearchControl(String id, IManagedForm managedForm) {
		super(id);
		this.managedForm = managedForm;
	}

	private boolean isMac() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return os != null && os.startsWith("Mac"); //$NON-NLS-1$
	}
	
	
	public String getText() {
		return searchText.getText().trim();
	}

	@Override
	protected Control createControl(Composite parent) {
		if (parent instanceof ToolBar) {
			// the FormHeading class sets the toolbar cursor to hand for some
			// reason,
			// we change it back so the input control can use a proper I-beam
			// cursor
			parent.setCursor(null);
		}

		FormToolkit toolkit = managedForm.getToolkit();
		Composite composite = toolkit.createComposite(parent);

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		// gross, but on the Mac the search controls are cut off on the bottom,
		// so they need to be bumped up a little. other OSs are fine.
		if (isMac()) {
			layout.marginHeight = -1;
		}
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setBackground(null);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Control label = toolkit.createLabel(composite, "Search");
		label.setBackground(null);

		searchText = toolkit.createText(composite, "", SWT.FLAT | SWT.SEARCH); //$NON-NLS-1$
		searchText.setData(FormToolkit.TEXT_BORDER, Boolean.TRUE);

		searchText.setLayoutData(new GridData(200, -1));
		ToolBar cancelBar = new ToolBar(composite, SWT.FLAT);

		final ToolItem clearToolItem = new ToolItem(cancelBar, SWT.NONE);
		clearToolItem.setEnabled(false);
		clearToolItem.setImage(ComposerUIPluginImages.CLEAR.createImage());
		clearToolItem.setDisabledImage(ComposerUIPluginImages.CLEAR_DISABLED.createImage());
		clearToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchText.setText(""); //$NON-NLS-1$
			}
		});

		searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				clearToolItem.setEnabled(searchText.getText().length() > 0);
				for (ModifyListener listener : modifyListeners) {	
					listener.modifyText(e);
				}
			}
		});

		toolkit.paintBordersFor(composite);

		return composite;
	}
	
	public void addModifyListener(ModifyListener listener) {
		if (!modifyListeners.contains(listener)) {
			modifyListeners.add(listener);
		}
	}
}
