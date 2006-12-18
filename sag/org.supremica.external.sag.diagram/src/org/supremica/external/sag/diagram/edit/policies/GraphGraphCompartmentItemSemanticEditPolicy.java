package org.supremica.external.sag.diagram.edit.policies;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.gef.commands.Command;

import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;

import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;

import org.eclipse.gmf.runtime.notation.View;

import org.supremica.external.sag.SagPackage;

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
		if (SagElementTypes.Node_2001 == req.getElementType()) {
			if (req.getContainmentFeature() == null) {
				req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Node());
			}
			return getMSLWrapper(new CreateNode_2001Command(req));
		}
		if (SagElementTypes.Node_2002 == req.getElementType()) {
			if (req.getContainmentFeature() == null) {
				req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Node());
			}
			return getMSLWrapper(new CreateNode_2002Command(req));
		}
		return super.getCreateCommand(req);
	}

	/**
	 * @generated
	 */
	private static class CreateNode_2001Command extends CreateElementCommand {

		/**
		 * @generated
		 */
		public CreateNode_2001Command(CreateElementRequest req) {
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

	/**
	 * @generated
	 */
	private static class CreateNode_2002Command extends CreateElementCommand {

		/**
		 * @generated
		 */
		public CreateNode_2002Command(CreateElementRequest req) {
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

}
