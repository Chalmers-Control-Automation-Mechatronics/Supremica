package org.supremica.external.sag.diagram.edit.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.gmf.runtime.notation.View;

import org.eclipse.gef.commands.UnexecutableCommand;

import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.diagram.edit.commands.ZoneTypeLinkCreateCommand;

import org.supremica.external.sag.diagram.providers.SagElementTypes;

/**
 * @generated
 */
public class SensorNodeItemSemanticEditPolicy extends
		SagBaseItemSemanticEditPolicy {

	/**
	 * @generated
	 */
	protected Command getDestroyElementCommand(DestroyElementRequest req) {
		return getMSLWrapper(new DestroyElementCommand(req) {

			protected EObject getElementToDestroy() {
				View view = (View) getHost().getModel();
				EAnnotation annotation = view.getEAnnotation("Shortcut"); //$NON-NLS-1$
				if (annotation != null) {
					return view;
				}
				return super.getElementToDestroy();
			}

		});
	}

	/**
	 * @generated
	 */
	protected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {
		if (SagElementTypes.Zone_4010 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingZone4010Command(req)
					: getCreateCompleteIncomingZone4010Command(req);
		}
		return super.getCreateRelationshipCommand(req);
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingZone4010Command(
			CreateRelationshipRequest req) {
		EObject sourceEObject = req.getSource();
		EObject targetEObject = req.getTarget();
		if (false == sourceEObject instanceof Node
				|| (targetEObject != null && false == targetEObject instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		Node source = (Node) sourceEObject;
		Node target = (Node) targetEObject;

		Graph container = (Graph) getRelationshipContainer(source,
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (container == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (!SagBaseItemSemanticEditPolicy.LinkConstraints.canCreateZone_4010(
				container, source, target)) {
			return UnexecutableCommand.INSTANCE;
		}
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingZone4010Command(
			CreateRelationshipRequest req) {
		EObject sourceEObject = req.getSource();
		EObject targetEObject = req.getTarget();
		if (false == sourceEObject instanceof Node
				|| false == targetEObject instanceof Node) {
			return UnexecutableCommand.INSTANCE;
		}
		Node source = (Node) sourceEObject;
		Node target = (Node) targetEObject;

		Graph container = (Graph) getRelationshipContainer(source,
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (container == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (!SagBaseItemSemanticEditPolicy.LinkConstraints.canCreateZone_4010(
				container, source, target)) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new ZoneTypeLinkCreateCommand(req, container,
				source, target));
	}
}
