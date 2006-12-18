package org.supremica.external.sag.diagram.part;

import org.eclipse.core.runtime.Platform;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;

import org.supremica.external.sag.BoundedZone;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.Project;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.UnboundedZone;

import org.supremica.external.sag.diagram.edit.parts.BoundedZone2EditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacity2EditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacityEditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphGraphCompartmentEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.Node2EditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeSensorEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone2EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone3EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone4EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZoneEditPart;

/**
 * This registry is used to determine which type of visual object should be
 * created for the corresponding Diagram, Node, ChildNode or Link represented 
 * by a domain model object.
 *
 * @generated
 */
public class SagVisualIDRegistry {

	/**
	 * @generated
	 */
	private static final String DEBUG_KEY = SagDiagramEditorPlugin
			.getInstance().getBundle().getSymbolicName()
			+ "/debug/visualID"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static int getVisualID(View view) {
		if (view instanceof Diagram) {
			if (ProjectEditPart.MODEL_ID.equals(view.getType())) {
				return ProjectEditPart.VISUAL_ID;
			} else {
				return -1;
			}
		}
		return getVisualID(view.getType());
	}

	/**
	 * @generated
	 */
	public static String getModelID(View view) {
		View diagram = view.getDiagram();
		while (view != diagram) {
			EAnnotation annotation = view.getEAnnotation("Shortcut"); //$NON-NLS-1$
			if (annotation != null) {
				return (String) annotation.getDetails().get("modelID"); //$NON-NLS-1$
			}
			view = (View) view.eContainer();
		}
		return diagram != null ? diagram.getType() : null;
	}

	/**
	 * @generated
	 */
	public static int getVisualID(String type) {
		try {
			return Integer.parseInt(type);
		} catch (NumberFormatException e) {
			if (Boolean.TRUE.toString().equalsIgnoreCase(
					Platform.getDebugOption(DEBUG_KEY))) {
				SagDiagramEditorPlugin.getInstance().logError(
						"Unable to parse view type as a visualID number: "
								+ type);
			}
		}
		return -1;
	}

	/**
	 * @generated
	 */
	public static String getType(int visualID) {
		return String.valueOf(visualID);
	}

	/**
	 * @generated
	 */
	public static int getDiagramVisualID(EObject domainElement) {
		if (domainElement == null) {
			return -1;
		}
		EClass domainElementMetaclass = domainElement.eClass();
		return getDiagramVisualID(domainElement, domainElementMetaclass);
	}

	/**
	 * @generated
	 */
	private static int getDiagramVisualID(EObject domainElement,
			EClass domainElementMetaclass) {
		if (SagPackage.eINSTANCE.getProject().isSuperTypeOf(
				domainElementMetaclass)
				&& isDiagramProject_79((Project) domainElement)) {
			return ProjectEditPart.VISUAL_ID;
		}
		return getUnrecognizedDiagramID(domainElement);
	}

	/**
	 * @generated
	 */
	public static int getNodeVisualID(View containerView, EObject domainElement) {
		if (domainElement == null) {
			return -1;
		}
		EClass domainElementMetaclass = domainElement.eClass();
		return getNodeVisualID(containerView, domainElement,
				domainElementMetaclass, null);
	}

	/**
	 * @generated
	 */
	public static int getNodeVisualID(View containerView,
			EObject domainElement, EClass domainElementMetaclass,
			String semanticHint) {
		String containerModelID = getModelID(containerView);
		if (!ProjectEditPart.MODEL_ID.equals(containerModelID)) {
			return -1;
		}
		int containerVisualID;
		if (ProjectEditPart.MODEL_ID.equals(containerModelID)) {
			containerVisualID = getVisualID(containerView);
		} else {
			if (containerView instanceof Diagram) {
				containerVisualID = ProjectEditPart.VISUAL_ID;
			} else {
				return -1;
			}
		}
		int nodeVisualID = semanticHint != null ? getVisualID(semanticHint)
				: -1;
		switch (containerVisualID) {
		case GraphEditPart.VISUAL_ID:
			if (GraphNameEditPart.VISUAL_ID == nodeVisualID) {
				return GraphNameEditPart.VISUAL_ID;
			}
			if (GraphGraphCompartmentEditPart.VISUAL_ID == nodeVisualID) {
				return GraphGraphCompartmentEditPart.VISUAL_ID;
			}
			return getUnrecognizedGraph_1001ChildNodeID(domainElement,
					semanticHint);
		case NodeEditPart.VISUAL_ID:
			if (NodeSensorEditPart.VISUAL_ID == nodeVisualID) {
				return NodeSensorEditPart.VISUAL_ID;
			}
			return getUnrecognizedNode_2001ChildNodeID(domainElement,
					semanticHint);
		case Node2EditPart.VISUAL_ID:
			return getUnrecognizedNode_2002ChildNodeID(domainElement,
					semanticHint);
		case GraphGraphCompartmentEditPart.VISUAL_ID:
			if ((semanticHint == null || NodeEditPart.VISUAL_ID == nodeVisualID)
					&& SagPackage.eINSTANCE.getNode().isSuperTypeOf(
							domainElementMetaclass)
					&& (domainElement == null || isNodeNode_2001((Node) domainElement))) {
				return NodeEditPart.VISUAL_ID;
			}
			if ((semanticHint == null || Node2EditPart.VISUAL_ID == nodeVisualID)
					&& SagPackage.eINSTANCE.getNode().isSuperTypeOf(
							domainElementMetaclass)
					&& (domainElement == null || isNodeNode_2002((Node) domainElement))) {
				return Node2EditPart.VISUAL_ID;
			}
			return getUnrecognizedGraphGraphCompartment_5001ChildNodeID(
					domainElement, semanticHint);
		case ProjectEditPart.VISUAL_ID:
			if ((semanticHint == null || GraphEditPart.VISUAL_ID == nodeVisualID)
					&& SagPackage.eINSTANCE.getGraph().isSuperTypeOf(
							domainElementMetaclass)
					&& (domainElement == null || isNodeGraph_1001((Graph) domainElement))) {
				return GraphEditPart.VISUAL_ID;
			}
			return getUnrecognizedProject_79ChildNodeID(domainElement,
					semanticHint);
		case BoundedZoneEditPart.VISUAL_ID:
			if (BoundedZoneCapacityEditPart.VISUAL_ID == nodeVisualID) {
				return BoundedZoneCapacityEditPart.VISUAL_ID;
			}
			return getUnrecognizedBoundedZone_3001LinkLabelID(semanticHint);
		case BoundedZone2EditPart.VISUAL_ID:
			if (BoundedZoneCapacity2EditPart.VISUAL_ID == nodeVisualID) {
				return BoundedZoneCapacity2EditPart.VISUAL_ID;
			}
			return getUnrecognizedBoundedZone_3002LinkLabelID(semanticHint);
		}
		return -1;
	}

	/**
	 * @generated
	 */
	public static int getLinkWithClassVisualID(EObject domainElement) {
		if (domainElement == null) {
			return -1;
		}
		EClass domainElementMetaclass = domainElement.eClass();
		return getLinkWithClassVisualID(domainElement, domainElementMetaclass);
	}

	/**
	 * @generated
	 */
	public static int getLinkWithClassVisualID(EObject domainElement,
			EClass domainElementMetaclass) {
		if (SagPackage.eINSTANCE.getBoundedZone().isSuperTypeOf(
				domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassBoundedZone_3001((BoundedZone) domainElement))) {
			return BoundedZoneEditPart.VISUAL_ID;
		} else if (SagPackage.eINSTANCE.getBoundedZone().isSuperTypeOf(
				domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassBoundedZone_3002((BoundedZone) domainElement))) {
			return BoundedZone2EditPart.VISUAL_ID;
		} else if (SagPackage.eINSTANCE.getUnboundedZone().isSuperTypeOf(
				domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassUnboundedZone_3003((UnboundedZone) domainElement))) {
			return UnboundedZoneEditPart.VISUAL_ID;
		} else if (SagPackage.eINSTANCE.getUnboundedZone().isSuperTypeOf(
				domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassUnboundedZone_3004((UnboundedZone) domainElement))) {
			return UnboundedZone2EditPart.VISUAL_ID;
		} else if (SagPackage.eINSTANCE.getUnboundedZone().isSuperTypeOf(
				domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassUnboundedZone_3005((UnboundedZone) domainElement))) {
			return UnboundedZone3EditPart.VISUAL_ID;
		} else if (SagPackage.eINSTANCE.getUnboundedZone().isSuperTypeOf(
				domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassUnboundedZone_3006((UnboundedZone) domainElement))) {
			return UnboundedZone4EditPart.VISUAL_ID;
		} else {
			return getUnrecognizedLinkWithClassID(domainElement);
		}
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isDiagramProject_79(Project element) {
		return true;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedDiagramID(EObject domainElement) {
		return -1;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isNodeGraph_1001(Graph element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isNodeNode_2001(Node element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isNodeNode_2002(Node element) {
		return true;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedGraph_1001ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedNode_2001ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedNode_2002ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedGraphGraphCompartment_5001ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedProject_79ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedBoundedZone_3001LinkLabelID(
			String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedBoundedZone_3002LinkLabelID(
			String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedLinkWithClassID(EObject domainElement) {
		return -1;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isLinkWithClassBoundedZone_3001(BoundedZone element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isLinkWithClassBoundedZone_3002(BoundedZone element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isLinkWithClassUnboundedZone_3003(
			UnboundedZone element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isLinkWithClassUnboundedZone_3004(
			UnboundedZone element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isLinkWithClassUnboundedZone_3005(
			UnboundedZone element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isLinkWithClassUnboundedZone_3006(
			UnboundedZone element) {
		return true;
	}
}
