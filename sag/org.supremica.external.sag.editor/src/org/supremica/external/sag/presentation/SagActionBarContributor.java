/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag.presentation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import org.eclipse.emf.common.ui.viewer.IViewerProvider;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

import org.eclipse.emf.edit.ui.action.ControlAction;
import org.eclipse.emf.edit.ui.action.CreateChildAction;
import org.eclipse.emf.edit.ui.action.CreateSiblingAction;
import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.emf.edit.ui.action.LoadResourceAction;
import org.eclipse.emf.edit.ui.action.ValidateAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.supremica.external.sag.Project;
import org.supremica.external.sag.automaton.AutomatonGenerator;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.ide.IDE;
import org.xml.sax.SAXException;

import net.sourceforge.waters.gui.WmodFileFilter;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.eclipse.emf.common.util.Diagnostic;
/**
 * This is the action bar contributor for the Sag model editor.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class SagActionBarContributor
	extends EditingDomainActionBarContributor
	implements ISelectionChangedListener {
	
	private final class GenerateAutomataAction extends Action {
		private GenerateAutomataAction(String text, int style) {
			super(text, style);
		}
		IDE ide;
		@Override
		public void runWithEvent(Event event) {
		    EditingDomain domain = ((IEditingDomainProvider)activeEditorPart).getEditingDomain();
			Project sagProject = (Project) domain.getResourceSet().getResources().get(0).getContents().get(0);
			//Validator validator = new Validator();
			Diagnostic diagnostic = Validator.INSTANCE.validate(sagProject, domain);
			if (diagnostic.getSeverity() == Diagnostic.ERROR || 
					diagnostic.getSeverity() == Diagnostic.WARNING) {
				MessageBox mb = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
		        mb.setText("Automata generation failure");
		        mb.setMessage("The automata generation was aborted since the SAG model is invalid.");
		        mb.open();
		        return;
			}
			//Diagnostic diagnostic = validateAction.run();
		    //diagnostic.getSeverity() == Diagnostic.OK;
		    FileDialog dialog = new FileDialog(getActiveEditor().getSite().getShell(), SWT.SAVE);
			dialog.setText("Save");
		    String[] filterExt = {"*."+WmodFileFilter.WMOD};
		    dialog.setFilterExtensions(filterExt);
			dialog.setFileName(sagProject.getName());
		    String filename = dialog.open();
			
		    if (filename != null) {
				ModuleProxy module = AutomatonGenerator.getInstance().generateAndSaveToFile(sagProject, filename);
				MessageBox mb = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			    mb.setText("Automata generation done");
			    mb.setMessage("Generation was successful. Do you want to open the automata in Supremica?");
			    if (mb.open() == SWT.YES) {
			    	InterfaceManager.getInstance().initLookAndFeel();
					try {
						if (ide == null) ide = new IDE();
						ide.setVisible(true);
						ide.installContainer((ModuleSubject)module);
						//List<File> fileToOpen = new ArrayList<File>();
						//fileToOpen.add(new File(filename));
						//ide.openFiles(fileToOpen);
					} catch (JAXBException e) {
						MessageBox errorBox = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
						errorBox.setText("Failure");
						errorBox.setMessage(e.getMessage());
						errorBox.open();
						e.printStackTrace();
					} catch (SAXException e) {
						MessageBox errorBox = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
						errorBox.setText("Failure");
						errorBox.setMessage(e.getMessage());
						errorBox.open();
						e.printStackTrace();					}
			    }
		    }
		}

/*		private void saveModuleToFile(String filename, ModuleProxy module) {
			try {
				JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
				marshaller.marshal(module, new File(filename));
			} catch (JAXBException e) {
				MessageBox mb = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			    mb.setText("Failure");
			    mb.setMessage(e.getMessage());
			    mb.open();
			    e.printStackTrace();
			} catch (SAXException e) {
				MessageBox mb = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			    mb.setText("Failure");
			    mb.setMessage(e.getMessage());
			    mb.open();
				e.printStackTrace();
			} catch (WatersMarshalException e) {
				MessageBox mb = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			    mb.setText("Failure");
			    mb.setMessage(e.getMessage());
			    mb.open();
				e.printStackTrace();
			} catch (IOException e) {
				MessageBox mb = new MessageBox(getActiveEditor().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			    mb.setText("Failure");
			    mb.setMessage(e.getMessage());
			    mb.open();
				e.printStackTrace();
			}
		}*/
	}

	/**
	 * This keeps track of the active editor.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IEditorPart activeEditorPart;

	/**
	 * This keeps track of the current selection provider.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ISelectionProvider selectionProvider;

	/**
	 * This action opens the Properties view.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IAction showPropertiesViewAction =
		new Action(SagEditorPlugin.INSTANCE.getString("_UI_ShowPropertiesView_menu_item")) {
			public void run() {
				try {
					getPage().showView("org.eclipse.ui.views.PropertySheet");
				}
				catch (PartInitException exception) {
					SagEditorPlugin.INSTANCE.log(exception);
				}
			}
		};

	/**
	 * This action refreshes the viewer of the current editor if the editor
	 * implements {@link org.eclipse.emf.common.ui.viewer.IViewerProvider}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IAction refreshViewerAction =
		new Action(SagEditorPlugin.INSTANCE.getString("_UI_RefreshViewer_menu_item")) {
			public boolean isEnabled() {
				return activeEditorPart instanceof IViewerProvider;
			}

			public void run() {
				if (activeEditorPart instanceof IViewerProvider) {
					Viewer viewer = ((IViewerProvider)activeEditorPart).getViewer();
					if (viewer != null) {
						viewer.refresh();
					}
				}
			}
		};

	/**
	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateChildAction} corresponding to each descriptor
	 * generated for the current selection by the item provider.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection createChildActions;

	/**
	 * This is the menu manager into which menu contribution items should be added for CreateChild actions.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IMenuManager createChildMenuManager;

	/**
	 * This will contain one {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} corresponding to each descriptor
	 * generated for the current selection by the item provider.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection createSiblingActions;

	/**
	 * This is the menu manager into which menu contribution items should be added for CreateSibling actions.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IMenuManager createSiblingMenuManager;

	/**
	 * This creates an instance of the contributor.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SagActionBarContributor() {
		super(ADDITIONS_LAST_STYLE);
		loadResourceAction = new LoadResourceAction();
		validateAction = new ValidateAction();
		controlAction = new ControlAction();
	}

	/**
	 * This adds Separators for editor additions to the tool bar.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Separator("sag-settings"));
		toolBarManager.add(new Separator("sag-additions"));
	}

	/**
	 * This adds to the menu bar a menu and some separators for editor additions,
	 * as well as the sub-menus for object creation items.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);

		IMenuManager submenuManager = new MenuManager(SagEditorPlugin.INSTANCE.getString("_UI_SagEditor_menu"), "org.supremica.external.sagMenuID");
		menuManager.insertAfter("additions", submenuManager);
		submenuManager.add(new Separator("settings"));
		submenuManager.add(new Separator("actions"));
		submenuManager.add(new Separator("additions"));
		submenuManager.add(new Separator("additions-end"));

		// Prepare for CreateChild item addition or removal.
		//
		createChildMenuManager = new MenuManager(SagEditorPlugin.INSTANCE.getString("_UI_CreateChild_menu_item"));
		submenuManager.insertBefore("additions", createChildMenuManager);

		// Prepare for CreateSibling item addition or removal.
		//
		createSiblingMenuManager = new MenuManager(SagEditorPlugin.INSTANCE.getString("_UI_CreateSibling_menu_item"));
		submenuManager.insertBefore("additions", createSiblingMenuManager);

		// Add "generate waters/supremica"
		//
		IAction generateAutomata  = new GenerateAutomataAction("Generate Automata", Action.AS_PUSH_BUTTON);
		submenuManager.insertAfter("additions", generateAutomata);
		
		// Force an update because Eclipse hides empty menus now.
		//
		submenuManager.addMenuListener
			(new IMenuListener() {
				 public void menuAboutToShow(IMenuManager menuManager) {
					 menuManager.updateAll(true);
				 }
			 });

		addGlobalActions(submenuManager);
	}

	/**
	 * When the active editor changes, this remembers the change and registers with it as a selection provider.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		activeEditorPart = part;

		// Switch to the new selection provider.
		//
		if (selectionProvider != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
		if (part == null) {
			selectionProvider = null;
		}
		else {
			selectionProvider = part.getSite().getSelectionProvider();
			selectionProvider.addSelectionChangedListener(this);

			// Fake a selection changed event to update the menus.
			//
			if (selectionProvider.getSelection() != null) {
				selectionChanged(new SelectionChangedEvent(selectionProvider, selectionProvider.getSelection()));
			}
		}
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionChangedListener},
	 * handling {@link org.eclipse.jface.viewers.SelectionChangedEvent}s by querying for the children and siblings
	 * that can be added to the selected object and updating the menus accordingly.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		// Remove any menu items for old selection.
		//
		if (createChildMenuManager != null) {
			depopulateManager(createChildMenuManager, createChildActions);
		}
		if (createSiblingMenuManager != null) {
			depopulateManager(createSiblingMenuManager, createSiblingActions);
		}

		// Query the new selection for appropriate new child/sibling descriptors
		//
		Collection newChildDescriptors = null;
		Collection newSiblingDescriptors = null;

		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection && ((IStructuredSelection)selection).size() == 1) {
			Object object = ((IStructuredSelection)selection).getFirstElement();

			EditingDomain domain = ((IEditingDomainProvider)activeEditorPart).getEditingDomain();

			newChildDescriptors = domain.getNewChildDescriptors(object, null);
			newSiblingDescriptors = domain.getNewChildDescriptors(null, object);
		}

		// Generate actions for selection; populate and redraw the menus.
		//
		createChildActions = generateCreateChildActions(newChildDescriptors, selection);
		createSiblingActions = generateCreateSiblingActions(newSiblingDescriptors, selection);

		if (createChildMenuManager != null) {
			populateManager(createChildMenuManager, createChildActions, null);
			createChildMenuManager.update(true);
		}
		if (createSiblingMenuManager != null) {
			populateManager(createSiblingMenuManager, createSiblingActions, null);
			createSiblingMenuManager.update(true);
		}
	}

	/**
	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateChildAction} for each object in <code>descriptors</code>,
	 * and returns the collection of these actions.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection generateCreateChildActions(Collection descriptors, ISelection selection) {
		Collection actions = new ArrayList();
		if (descriptors != null) {
			for (Iterator i = descriptors.iterator(); i.hasNext(); ) {
				actions.add(new CreateChildAction(activeEditorPart, selection, i.next()));
			}
		}
		return actions;
	}

	/**
	 * This generates a {@link org.eclipse.emf.edit.ui.action.CreateSiblingAction} for each object in <code>descriptors</code>,
	 * and returns the collection of these actions.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection generateCreateSiblingActions(Collection descriptors, ISelection selection) {
		Collection actions = new ArrayList();
		if (descriptors != null) {
			for (Iterator i = descriptors.iterator(); i.hasNext(); ) {
				actions.add(new CreateSiblingAction(activeEditorPart, selection, i.next()));
			}
		}
		return actions;
	}

	/**
	 * This populates the specified <code>manager</code> with {@link org.eclipse.jface.action.ActionContributionItem}s
	 * based on the {@link org.eclipse.jface.action.IAction}s contained in the <code>actions</code> collection,
	 * by inserting them before the specified contribution item <code>contributionID</code>.
	 * If <code>ID</code> is <code>null</code>, they are simply added.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void populateManager(IContributionManager manager, Collection actions, String contributionID) {
		if (actions != null) {
			for (Iterator i = actions.iterator(); i.hasNext(); ) {
				IAction action = (IAction)i.next();
				if (contributionID != null) {
					manager.insertBefore(contributionID, action);
				}
				else {
					manager.add(action);
				}
			}
		}
	}
		
	/**
	 * This removes from the specified <code>manager</code> all {@link org.eclipse.jface.action.ActionContributionItem}s
	 * based on the {@link org.eclipse.jface.action.IAction}s contained in the <code>actions</code> collection.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void depopulateManager(IContributionManager manager, Collection actions) {
		if (actions != null) {
			IContributionItem[] items = manager.getItems();
			for (int i = 0; i < items.length; i++) {
				// Look into SubContributionItems
				//
				IContributionItem contributionItem = items[i];
				while (contributionItem instanceof SubContributionItem) {
					contributionItem = ((SubContributionItem)contributionItem).getInnerItem();
				}

				// Delete the ActionContributionItems with matching action.
				//
				if (contributionItem instanceof ActionContributionItem) {
					IAction action = ((ActionContributionItem)contributionItem).getAction();
					if (actions.contains(action)) {
						manager.remove(contributionItem);
					}
				}
			}
		}
	}

	/**
	 * This populates the pop-up menu before it appears.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void menuAboutToShow(IMenuManager menuManager) {
		super.menuAboutToShow(menuManager);
		MenuManager submenuManager = null;

		submenuManager = new MenuManager(SagEditorPlugin.INSTANCE.getString("_UI_CreateChild_menu_item"));
		populateManager(submenuManager, createChildActions, null);
		menuManager.insertBefore("edit", submenuManager);

		submenuManager = new MenuManager(SagEditorPlugin.INSTANCE.getString("_UI_CreateSibling_menu_item"));
		populateManager(submenuManager, createSiblingActions, null);
		menuManager.insertBefore("edit", submenuManager);
	}

	/**
	 * This inserts global actions before the "additions-end" separator.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addGlobalActions(IMenuManager menuManager) {
		menuManager.insertAfter("additions-end", new Separator("ui-actions"));
		menuManager.insertAfter("ui-actions", showPropertiesViewAction);

		refreshViewerAction.setEnabled(refreshViewerAction.isEnabled());		
		menuManager.insertAfter("ui-actions", refreshViewerAction);

		super.addGlobalActions(menuManager);
	}

	/**
	 * This ensures that a delete action will clean up all references to deleted objects.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected boolean removeAllReferencesOnDelete() {
		return true;
	}

}
