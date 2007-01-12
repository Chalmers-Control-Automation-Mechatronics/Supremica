package org.supremica.external.sag.diagram.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.core.providers.AbstractViewProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacityEditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneEditPart;
import org.supremica.external.sag.diagram.edit.parts.EndNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphGraphCompartmentEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeSensorNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZoneEditPart;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

import org.supremica.external.sag.diagram.view.factories.BoundedZoneCapacityViewFactory;
import org.supremica.external.sag.diagram.view.factories.BoundedZoneViewFactory;
import org.supremica.external.sag.diagram.view.factories.EndNodeViewFactory;
import org.supremica.external.sag.diagram.view.factories.GraphGraphCompartmentViewFactory;
import org.supremica.external.sag.diagram.view.factories.GraphNameViewFactory;
import org.supremica.external.sag.diagram.view.factories.GraphViewFactory;
import org.supremica.external.sag.diagram.view.factories.ProjectViewFactory;
import org.supremica.external.sag.diagram.view.factories.SensorNodeSensorNameViewFactory;
import org.supremica.external.sag.diagram.view.factories.SensorNodeViewFactory;
import org.supremica.external.sag.diagram.view.factories.UnboundedZoneViewFactory;

/**
 * @generated
 */
public class SagViewProvider extends AbstractViewProvider {

	/**
	 * @generated
	 */
	protected Class getDiagramViewClass(IAdaptable semanticAdapter,
			String diagramKind) {
		EObject semanticElement = getSemanticElement(semanticAdapter);
		if (ProjectEditPart.MODEL_ID.equals(diagramKind)
				&& SagVisualIDRegistry.getDiagramVisualID(semanticElement) != -1) {
			return ProjectViewFactory.class;
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected Class getNodeViewClass(IAdaptable semanticAdapter,
			View containerView, String semanticHint) {
		if (containerView == null) {
			return null;
		}
		IElementType elementType = getSemanticElementType(semanticAdapter);
		if (elementType != null
				&& !SagElementTypes.isKnownElementType(elementType)) {
			return null;
		}
		EClass semanticType = getSemanticEClass(semanticAdapter);
		EObject semanticElement = getSemanticElement(semanticAdapter);
		int nodeVID = SagVisualIDRegistry.getNodeVisualID(containerView,
				semanticElement, semanticType, semanticHint);
		switch (nodeVID) {
		case GraphEditPart.VISUAL_ID:
			return GraphViewFactory.class;
		case GraphNameEditPart.VISUAL_ID:
			return GraphNameViewFactory.class;
		case SensorNodeEditPart.VISUAL_ID:
			return SensorNodeViewFactory.class;
		case SensorNodeSensorNameEditPart.VISUAL_ID:
			return SensorNodeSensorNameViewFactory.class;
		case EndNodeEditPart.VISUAL_ID:
			return EndNodeViewFactory.class;
		case GraphGraphCompartmentEditPart.VISUAL_ID:
			return GraphGraphCompartmentViewFactory.class;
		case BoundedZoneCapacityEditPart.VISUAL_ID:
			return BoundedZoneCapacityViewFactory.class;
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected Class getEdgeViewClass(IAdaptable semanticAdapter,
			View containerView, String semanticHint) {
		IElementType elementType = getSemanticElementType(semanticAdapter);
		if (elementType != null
				&& !SagElementTypes.isKnownElementType(elementType)) {
			return null;
		}
		EClass semanticType = getSemanticEClass(semanticAdapter);
		if (semanticType == null) {
			return null;
		}
		EObject semanticElement = getSemanticElement(semanticAdapter);
		int linkVID = SagVisualIDRegistry.getLinkWithClassVisualID(
				semanticElement, semanticType);
		switch (linkVID) {
		case BoundedZoneEditPart.VISUAL_ID:
			return BoundedZoneViewFactory.class;
		case UnboundedZoneEditPart.VISUAL_ID:
			return UnboundedZoneViewFactory.class;
		}
		return getUnrecognizedConnectorViewClass(semanticAdapter,
				containerView, semanticHint);
	}

	/**
	 * @generated
	 */
	private IElementType getSemanticElementType(IAdaptable semanticAdapter) {
		if (semanticAdapter == null) {
			return null;
		}
		return (IElementType) semanticAdapter.getAdapter(IElementType.class);
	}

	/**
	 * @generated
	 */
	private Class getUnrecognizedConnectorViewClass(IAdaptable semanticAdapter,
			View containerView, String semanticHint) {
		// Handle unrecognized child node classes here
		return null;
	}

}
