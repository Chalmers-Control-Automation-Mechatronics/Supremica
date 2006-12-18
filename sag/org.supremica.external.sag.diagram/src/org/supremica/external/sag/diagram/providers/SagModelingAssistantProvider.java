package org.supremica.external.sag.diagram.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.ui.services.modelingassistant.ModelingAssistantProvider;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.supremica.external.sag.diagram.edit.parts.GraphGraphCompartmentEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.Node2EditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;

import org.supremica.external.sag.diagram.part.SagDiagramEditorPlugin;

/**
 * @generated
 */
public class SagModelingAssistantProvider extends ModelingAssistantProvider {

	/**
	 * @generated
	 */
	public List getTypesForPopupBar(IAdaptable host) {
		IGraphicalEditPart editPart = (IGraphicalEditPart) host
				.getAdapter(IGraphicalEditPart.class);
		if (editPart instanceof GraphGraphCompartmentEditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.Node_2001);
			types.add(SagElementTypes.Node_2002);
			return types;
		}
		if (editPart instanceof ProjectEditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.Graph_1001);
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated
	 */
	public List getRelTypesOnSource(IAdaptable source) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
				.getAdapter(IGraphicalEditPart.class);
		if (sourceEditPart instanceof NodeEditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.BoundedZone_3001);
			types.add(SagElementTypes.BoundedZone_3002);
			types.add(SagElementTypes.UnboundedZone_3003);
			types.add(SagElementTypes.UnboundedZone_3004);
			types.add(SagElementTypes.UnboundedZone_3005);
			types.add(SagElementTypes.UnboundedZone_3006);
			return types;
		}
		if (sourceEditPart instanceof Node2EditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.BoundedZone_3001);
			types.add(SagElementTypes.BoundedZone_3002);
			types.add(SagElementTypes.UnboundedZone_3003);
			types.add(SagElementTypes.UnboundedZone_3004);
			types.add(SagElementTypes.UnboundedZone_3005);
			types.add(SagElementTypes.UnboundedZone_3006);
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated
	 */
	public List getRelTypesOnTarget(IAdaptable target) {
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		if (targetEditPart instanceof NodeEditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.BoundedZone_3001);
			types.add(SagElementTypes.BoundedZone_3002);
			types.add(SagElementTypes.UnboundedZone_3003);
			types.add(SagElementTypes.UnboundedZone_3004);
			types.add(SagElementTypes.UnboundedZone_3005);
			types.add(SagElementTypes.UnboundedZone_3006);
			return types;
		}
		if (targetEditPart instanceof Node2EditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.BoundedZone_3001);
			types.add(SagElementTypes.BoundedZone_3002);
			types.add(SagElementTypes.UnboundedZone_3003);
			types.add(SagElementTypes.UnboundedZone_3004);
			types.add(SagElementTypes.UnboundedZone_3005);
			types.add(SagElementTypes.UnboundedZone_3006);
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated
	 */
	public List getRelTypesOnSourceAndTarget(IAdaptable source,
			IAdaptable target) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
				.getAdapter(IGraphicalEditPart.class);
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		if (sourceEditPart instanceof NodeEditPart) {
			List types = new ArrayList();
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.BoundedZone_3001);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.BoundedZone_3001);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.BoundedZone_3002);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.BoundedZone_3002);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3003);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3003);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3004);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3004);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3005);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3005);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3006);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3006);
			}
			return types;
		}
		if (sourceEditPart instanceof Node2EditPart) {
			List types = new ArrayList();
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.BoundedZone_3001);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.BoundedZone_3001);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.BoundedZone_3002);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.BoundedZone_3002);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3003);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3003);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3004);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3004);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3005);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3005);
			}
			if (targetEditPart instanceof NodeEditPart) {
				types.add(SagElementTypes.UnboundedZone_3006);
			}
			if (targetEditPart instanceof Node2EditPart) {
				types.add(SagElementTypes.UnboundedZone_3006);
			}
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated
	 */
	public List getTypesForSource(IAdaptable target,
			IElementType relationshipType) {
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		if (targetEditPart instanceof NodeEditPart) {
			List types = new ArrayList();
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2002);
			}
			return types;
		}
		if (targetEditPart instanceof Node2EditPart) {
			List types = new ArrayList();
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2002);
			}
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated
	 */
	public List getTypesForTarget(IAdaptable source,
			IElementType relationshipType) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
				.getAdapter(IGraphicalEditPart.class);
		if (sourceEditPart instanceof NodeEditPart) {
			List types = new ArrayList();
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2002);
			}
			return types;
		}
		if (sourceEditPart instanceof Node2EditPart) {
			List types = new ArrayList();
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3001) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.BoundedZone_3002) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3003) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3004) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3005) {
				types.add(SagElementTypes.Node_2002);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2001);
			}
			if (relationshipType == SagElementTypes.UnboundedZone_3006) {
				types.add(SagElementTypes.Node_2002);
			}
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated
	 */
	public EObject selectExistingElementForSource(IAdaptable target,
			IElementType relationshipType) {
		return selectExistingElement(target, getTypesForSource(target,
				relationshipType));
	}

	/**
	 * @generated
	 */
	public EObject selectExistingElementForTarget(IAdaptable source,
			IElementType relationshipType) {
		return selectExistingElement(source, getTypesForTarget(source,
				relationshipType));
	}

	/**
	 * @generated
	 */
	protected EObject selectExistingElement(IAdaptable host, Collection types) {
		if (types.isEmpty()) {
			return null;
		}
		IGraphicalEditPart editPart = (IGraphicalEditPart) host
				.getAdapter(IGraphicalEditPart.class);
		if (editPart == null) {
			return null;
		}
		Diagram diagram = (Diagram) editPart.getRoot().getContents().getModel();
		Collection elements = new HashSet();
		for (Iterator it = diagram.getElement().eAllContents(); it.hasNext();) {
			EObject element = (EObject) it.next();
			if (isApplicableElement(element, types)) {
				elements.add(element);
			}
		}
		if (elements.isEmpty()) {
			return null;
		}
		return selectElement((EObject[]) elements.toArray(new EObject[elements
				.size()]));
	}

	/**
	 * @generated
	 */
	protected boolean isApplicableElement(EObject element, Collection types) {
		IElementType type = ElementTypeRegistry.getInstance().getElementType(
				element);
		return types.contains(type);
	}

	/**
	 * @generated
	 */
	protected EObject selectElement(EObject[] elements) {
		Shell shell = Display.getCurrent().getActiveShell();
		ILabelProvider labelProvider = new AdapterFactoryLabelProvider(
				SagDiagramEditorPlugin.getInstance()
						.getItemProvidersAdapterFactory());
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				shell, labelProvider);
		dialog.setMessage("Available domain model elements:");
		dialog.setTitle("Select domain model element");
		dialog.setMultipleSelection(false);
		dialog.setElements(elements);
		EObject selected = null;
		if (dialog.open() == Window.OK) {
			selected = (EObject) dialog.getFirstResult();
		}
		return selected;
	}
}
