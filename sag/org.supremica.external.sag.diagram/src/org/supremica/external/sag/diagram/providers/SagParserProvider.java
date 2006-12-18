package org.supremica.external.sag.diagram.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.supremica.external.sag.SagPackage;

import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacity2EditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacityEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeSensorEditPart;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class SagParserProvider extends AbstractProvider implements
		IParserProvider {

	/**
	 * @generated
	 */
	private IParser nodeNodeSensor_4001Parser;

	/**
	 * @generated
	 */
	private IParser getNodeNodeSensor_4001Parser() {
		if (nodeNodeSensor_4001Parser == null) {
			nodeNodeSensor_4001Parser = createNodeNodeSensor_4001Parser();
		}
		return nodeNodeSensor_4001Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createNodeNodeSensor_4001Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getNode_Sensor());
		return parser;
	}

	/**
	 * @generated
	 */
	private IParser graphGraphName_4002Parser;

	/**
	 * @generated
	 */
	private IParser getGraphGraphName_4002Parser() {
		if (graphGraphName_4002Parser == null) {
			graphGraphName_4002Parser = createGraphGraphName_4002Parser();
		}
		return graphGraphName_4002Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createGraphGraphName_4002Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getNamed_Name());
		return parser;
	}

	/**
	 * @generated
	 */
	private IParser boundedZoneBoundedZoneCapacity_4003Parser;

	/**
	 * @generated
	 */
	private IParser getBoundedZoneBoundedZoneCapacity_4003Parser() {
		if (boundedZoneBoundedZoneCapacity_4003Parser == null) {
			boundedZoneBoundedZoneCapacity_4003Parser = createBoundedZoneBoundedZoneCapacity_4003Parser();
		}
		return boundedZoneBoundedZoneCapacity_4003Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createBoundedZoneBoundedZoneCapacity_4003Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getBoundedZone_Capacity());
		return parser;
	}

	/**
	 * @generated
	 */
	private IParser boundedZoneBoundedZoneCapacity_4004Parser;

	/**
	 * @generated
	 */
	private IParser getBoundedZoneBoundedZoneCapacity_4004Parser() {
		if (boundedZoneBoundedZoneCapacity_4004Parser == null) {
			boundedZoneBoundedZoneCapacity_4004Parser = createBoundedZoneBoundedZoneCapacity_4004Parser();
		}
		return boundedZoneBoundedZoneCapacity_4004Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createBoundedZoneBoundedZoneCapacity_4004Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getBoundedZone_Capacity());
		return parser;
	}

	/**
	 * @generated
	 */
	protected IParser getParser(int visualID) {
		switch (visualID) {
		case NodeSensorEditPart.VISUAL_ID:
			return getNodeNodeSensor_4001Parser();
		case GraphNameEditPart.VISUAL_ID:
			return getGraphGraphName_4002Parser();
		case BoundedZoneCapacityEditPart.VISUAL_ID:
			return getBoundedZoneBoundedZoneCapacity_4003Parser();
		case BoundedZoneCapacity2EditPart.VISUAL_ID:
			return getBoundedZoneBoundedZoneCapacity_4004Parser();
		}
		return null;
	}

	/**
	 * @generated
	 */
	public IParser getParser(IAdaptable hint) {
		String vid = (String) hint.getAdapter(String.class);
		if (vid != null) {
			return getParser(SagVisualIDRegistry.getVisualID(vid));
		}
		View view = (View) hint.getAdapter(View.class);
		if (view != null) {
			return getParser(SagVisualIDRegistry.getVisualID(view));
		}
		return null;
	}

	/**
	 * @generated
	 */
	public boolean provides(IOperation operation) {
		if (operation instanceof GetParserOperation) {
			IAdaptable hint = ((GetParserOperation) operation).getHint();
			if (SagElementTypes.getElement(hint) == null) {
				return false;
			}
			return getParser(hint) != null;
		}
		return false;
	}
}
