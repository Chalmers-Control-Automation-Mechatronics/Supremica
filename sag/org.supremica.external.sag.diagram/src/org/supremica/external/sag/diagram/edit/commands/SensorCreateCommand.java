package org.supremica.external.sag.diagram.edit.commands;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;

import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;

import org.eclipse.gmf.runtime.notation.View;

import org.supremica.external.sag.SagPackage;

/**
 * @generated
 */
public class SensorCreateCommand extends CreateElementCommand {

	/**
	 * @generated
	 */
	public SensorCreateCommand(CreateElementRequest req) {
		super(req);
	}

	/**
	 * @generated
	 */
	protected EClass getEClassToEdit() {
		return SagPackage.eINSTANCE.getGraph();
	};

	/**
	 * @generated
	 */
	protected EObject getElementToEdit() {
		EObject container = ((CreateElementRequest) getRequest())
				.getContainer();
		if (container instanceof View) {
			container = ((View) container).getElement();
		}
		return container;
	}

}
