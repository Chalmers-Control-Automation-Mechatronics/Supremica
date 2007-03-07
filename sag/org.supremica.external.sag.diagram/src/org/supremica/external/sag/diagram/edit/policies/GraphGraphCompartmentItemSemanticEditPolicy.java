package org.supremica.external.sag.diagram.edit.policies;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.gef.commands.Command;

import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;

import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;

import org.eclipse.gmf.runtime.notation.View;

import org.supremica.external.sag.SagPackage;

import org.supremica.external.sag.diagram.edit.commands.EndNodeCreateCommand;
import org.supremica.external.sag.diagram.edit.commands.SensorCreateCommand;
import org.supremica.external.sag.diagram.edit.commands.SensorNodeCreateCommand;
import org.supremica.external.sag.diagram.providers.SagElementTypes;

/**
 * @generated
 */
public class GraphGraphCompartmentItemSemanticEditPolicy extends
		SagBaseItemSemanticEditPolicy {

	/**
	 * @generated
	 */
	protected Command getCreateCommand(CreateElementRequest req) {
		if (SagElementTypes.Sensor_3008 == req.getElementType()) {
			if (req.getContainmentFeature() == null) {
				req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Node());
			}
			return getMSLWrapper(new SensorCreateCommand(req));
		}
		if (SagElementTypes.EndNode_3007 == req.getElementType()) {
			if (req.getContainmentFeature() == null) {
				req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Node());
			}
			return getMSLWrapper(new EndNodeCreateCommand(req));
		}
		return super.getCreateCommand(req);
	}

}
