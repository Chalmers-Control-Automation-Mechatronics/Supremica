package org.supremica.external.sag.diagram.part;

import org.eclipse.core.runtime.Platform;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;

import org.supremica.external.sag.EndNode;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Project;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.SensorNode;
import org.supremica.external.sag.Zone;

import org.supremica.external.sag.diagram.edit.parts.EndNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphGraphCompartmentEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.ZoneEditPart;

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
				&& isDiagramProject_1000((Project) domainElement)) {
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
			return getUnrecognizedGraph_2010ChildNodeID(domainElement,
					semanticHint);
		case SensorNodeEditPart.VISUAL_ID:
			if (SensorNodeNameEditPart.VISUAL_ID == nodeVisualID) {
				return SensorNodeNameEditPart.VISUAL_ID;
			}
			return getUnrecognizedSensorNode_3006ChildNodeID(domainElement,
					semanticHint);
		case EndNodeEditPart.VISUAL_ID:
			return getUnrecognizedEndNode_3007ChildNodeID(domainElement,
					semanticHint);
		case GraphGraphCompartmentEditPart.VISUAL_ID:
			if ((semanticHint == null || SensorNodeEditPart.VISUAL_ID == nodeVisualID)
					&& SagPackage.eINSTANCE.getSensorNode().isSuperTypeOf(
							domainElementMetaclass)
					&& (domainElement == null || isNodeSensorNode_3006((SensorNode) domainElement))) {
				return SensorNodeEditPart.VISUAL_ID;
			}
			if ((semanticHint == null || EndNodeEditPart.VISUAL_ID == nodeVisualID)
					&& SagPackage.eINSTANCE.getEndNode().isSuperTypeOf(
							domainElementMetaclass)
					&& (domainElement == null || isNodeEndNode_3007((EndNode) domainElement))) {
				return EndNodeEditPart.VISUAL_ID;
			}
			return getUnrecognizedGraphGraphCompartment_7002ChildNodeID(
					domainElement, semanticHint);
		case ProjectEditPart.VISUAL_ID:
			if ((semanticHint == null || GraphEditPart.VISUAL_ID == nodeVisualID)
					&& SagPackage.eINSTANCE.getGraph().isSuperTypeOf(
							domainElementMetaclass)
					&& (domainElement == null || isNodeGraph_2010((Graph) domainElement))) {
				return GraphEditPart.VISUAL_ID;
			}
			return getUnrecognizedProject_1000ChildNodeID(domainElement,
					semanticHint);
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
		if (SagPackage.eINSTANCE.getZone()
				.isSuperTypeOf(domainElementMetaclass)
				&& (domainElement == null || isLinkWithClassZone_4010((Zone) domainElement))) {
			return ZoneEditPart.VISUAL_ID;
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
	private static boolean isDiagramProject_1000(Project element) {
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
	private static boolean isNodeGraph_2010(Graph element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isNodeSensorNode_3006(SensorNode element) {
		return true;
	}

	/**
	 * User can change implementation of this method to check some additional 
	 * conditions here.
	 *
	 * @generated
	 */
	private static boolean isNodeEndNode_3007(EndNode element) {
		return true;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedGraph_2010ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedSensorNode_3006ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedEndNode_3007ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedGraphGraphCompartment_7002ChildNodeID(
			EObject domainElement, String semanticHint) {
		return -1;
	}

	/**
	 * User can change implementation of this method to handle some specific
	 * situations not covered by default logic.
	 *
	 * @generated
	 */
	private static int getUnrecognizedProject_1000ChildNodeID(
			EObject domainElement, String semanticHint) {
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
	private static boolean isLinkWithClassZone_4010(Zone element) {
		return true;
	}
}
