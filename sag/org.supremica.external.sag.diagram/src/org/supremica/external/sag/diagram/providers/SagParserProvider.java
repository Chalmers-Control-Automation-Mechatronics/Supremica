package org.supremica.external.sag.diagram.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.supremica.external.sag.SagPackage;

import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacityEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeSensorNameEditPart;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class SagParserProvider extends AbstractProvider implements
		IParserProvider {

	/**
	 * @generated
	 */
	private IParser sensorNodeSensorNodeSensorName_5003Parser;

	/**
	 * @generated
	 */
	private IParser getSensorNodeSensorNodeSensorName_5003Parser() {
		if (sensorNodeSensorNodeSensorName_5003Parser == null) {
			sensorNodeSensorNodeSensorName_5003Parser = createSensorNodeSensorNodeSensorName_5003Parser();
		}
		return sensorNodeSensorNodeSensorName_5003Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createSensorNodeSensorNodeSensorName_5003Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getSensorNode_SensorName());
		return parser;
	}

	/**
	 * @generated
	 */
	private IParser graphGraphName_5004Parser;

	/**
	 * @generated
	 */
	private IParser getGraphGraphName_5004Parser() {
		if (graphGraphName_5004Parser == null) {
			graphGraphName_5004Parser = createGraphGraphName_5004Parser();
		}
		return graphGraphName_5004Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createGraphGraphName_5004Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getNamed_Name());
		return parser;
	}

	/**
	 * @generated
	 */
	private IParser boundedZoneBoundedZoneCapacity_6003Parser;

	/**
	 * @generated
	 */
	private IParser getBoundedZoneBoundedZoneCapacity_6003Parser() {
		if (boundedZoneBoundedZoneCapacity_6003Parser == null) {
			boundedZoneBoundedZoneCapacity_6003Parser = createBoundedZoneBoundedZoneCapacity_6003Parser();
		}
		return boundedZoneBoundedZoneCapacity_6003Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createBoundedZoneBoundedZoneCapacity_6003Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getBoundedZone_Capacity());
		return parser;
	}

	/**
	 * @generated
	 */
	protected IParser getParser(int visualID) {
		switch (visualID) {
		case SensorNodeSensorNameEditPart.VISUAL_ID:
			return getSensorNodeSensorNodeSensorName_5003Parser();
		case GraphNameEditPart.VISUAL_ID:
			return getGraphGraphName_5004Parser();
		case BoundedZoneCapacityEditPart.VISUAL_ID:
			return getBoundedZoneBoundedZoneCapacity_6003Parser();
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
