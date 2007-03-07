package org.supremica.external.sag.diagram.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.supremica.external.sag.SagPackage;

import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNameEditPart;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class SagParserProvider extends AbstractProvider implements
		IParserProvider {

	/**
	 * @generated
	 */
	private IParser sensorSensorName_5005Parser;

	/**
	 * @generated
	 */
	private IParser getSensorSensorName_5005Parser() {
		if (sensorSensorName_5005Parser == null) {
			sensorSensorName_5005Parser = createSensorSensorName_5005Parser();
		}
		return sensorSensorName_5005Parser;
	}

	/**
	 * @generated
	 */
	protected IParser createSensorSensorName_5005Parser() {
		SagStructuralFeatureParser parser = new SagStructuralFeatureParser(
				SagPackage.eINSTANCE.getSensor_Name());
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
	protected IParser getParser(int visualID) {
		switch (visualID) {
		case SensorNameEditPart.VISUAL_ID:
			return getSensorSensorName_5005Parser();
		case GraphNameEditPart.VISUAL_ID:
			return getGraphGraphName_5004Parser();
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
