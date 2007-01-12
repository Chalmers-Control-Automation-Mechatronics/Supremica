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
import org.supremica.external.sag.diagram.edit.commands.BoundedZoneTypeLinkCreateCommand;
import org.supremica.external.sag.diagram.edit.commands.UnboundedZoneTypeLinkCreateCommand;

import org.supremica.external.sag.diagram.providers.SagElementTypes;

/**
 * @generated
 */
public class EndNodeItemSemanticEditPolicy extends
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
		if (SagElementTypes.BoundedZone_4007 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingBoundedZone4007Command(req)
					: getCreateCompleteIncomingBoundedZone4007Command(req);
		}
		if (SagElementTypes.UnboundedZone_4009 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingUnboundedZone4009Command(req)
					: getCreateCompleteIncomingUnboundedZone4009Command(req);
		}
		return super.getCreateRelationshipCommand(req);
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingBoundedZone4007Command(
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
		if (!SagBaseItemSemanticEditPolicy.LinkConstraints
				.canCreateBoundedZone_4007(container, source, target)) {
			return UnexecutableCommand.INSTANCE;
		}
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingBoundedZone4007Command(
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
		if (!SagBaseItemSemanticEditPolicy.LinkConstraints
				.canCreateBoundedZone_4007(container, source, target)) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new BoundedZoneTypeLinkCreateCommand(req,
				container, source, target));
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingUnboundedZone4009Command(
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
		if (!SagBaseItemSemanticEditPolicy.LinkConstraints
				.canCreateUnboundedZone_4009(container, source, target)) {
			return UnexecutableCommand.INSTANCE;
		}
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingUnboundedZone4009Command(
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
		if (!SagBaseItemSemanticEditPolicy.LinkConstraints
				.canCreateUnboundedZone_4009(container, source, target)) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new UnboundedZoneTypeLinkCreateCommand(req,
				container, source, target));
	}
}
