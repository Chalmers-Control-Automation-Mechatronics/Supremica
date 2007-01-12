package org.supremica.external.sag.diagram.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
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
import org.supremica.external.sag.diagram.edit.parts.EndNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphGraphCompartmentEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeEditPart;

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
			types.add(SagElementTypes.SensorNode_3006);
			types.add(SagElementTypes.EndNode_3007);
			return types;
		}
		if (editPart instanceof ProjectEditPart) {
			List types = new ArrayList();
			types.add(SagElementTypes.Graph_2010);
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated NOT
	 */
	public List getRelTypesOnSource(IAdaptable source) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
				.getAdapter(IGraphicalEditPart.class);
		if (sourceEditPart instanceof SensorNodeEditPart
				|| sourceEditPart instanceof EndNodeEditPart) {
			List<IElementType> types = new ArrayList<IElementType>();
			types.add(SagElementTypes.BoundedZone_4007);
			types.add(SagElementTypes.UnboundedZone_4009);
			return types;
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated NOT
	 */
	public List getRelTypesOnTarget(IAdaptable target) {
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		if (targetEditPart instanceof SensorNodeEditPart
				|| targetEditPart instanceof EndNodeEditPart) {
			List<IElementType> types = new ArrayList<IElementType>();
			types.add(SagElementTypes.BoundedZone_4007);
			types.add(SagElementTypes.UnboundedZone_4009);
			return types;
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated NOT
	 */
	public List getRelTypesOnSourceAndTarget(IAdaptable source,
			IAdaptable target) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
				.getAdapter(IGraphicalEditPart.class);
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		if ((sourceEditPart instanceof SensorNodeEditPart && (targetEditPart instanceof SensorNodeEditPart || targetEditPart instanceof EndNodeEditPart))
				|| (sourceEditPart instanceof EndNodeEditPart && targetEditPart instanceof SensorNodeEditPart)) {
			List<IElementType> types = new ArrayList<IElementType>();
			types.add(SagElementTypes.BoundedZone_4007);
			types.add(SagElementTypes.UnboundedZone_4009);
			return types;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @generated NOT
	 */
	public List getTypesForSource(IAdaptable target,
			IElementType relationshipType) {
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		List<IElementType> types = new ArrayList<IElementType>();
		if (relationshipType == SagElementTypes.BoundedZone_4007
				|| relationshipType == SagElementTypes.UnboundedZone_4009) {
			if (targetEditPart instanceof SensorNodeEditPart) {
				types.add(SagElementTypes.SensorNode_3006);
				types.add(SagElementTypes.EndNode_3007);
			}
			if (targetEditPart instanceof EndNodeEditPart) {
				types.add(SagElementTypes.SensorNode_3006);
			}
		}
		return types;
	}

	/**
	 * @generated NOT
	 */
	public List getTypesForTarget(IAdaptable source,
			IElementType relationshipType) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
				.getAdapter(IGraphicalEditPart.class);
		List<IElementType> types = new ArrayList<IElementType>();
		if (relationshipType == SagElementTypes.BoundedZone_4007
				|| relationshipType == SagElementTypes.UnboundedZone_4009) {
			if (sourceEditPart instanceof SensorNodeEditPart) {
				types.add(SagElementTypes.SensorNode_3006);
				types.add(SagElementTypes.EndNode_3007);
			}
			if (sourceEditPart instanceof EndNodeEditPart) {
				types.add(SagElementTypes.SensorNode_3006);
			}
		}
		return types;
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
