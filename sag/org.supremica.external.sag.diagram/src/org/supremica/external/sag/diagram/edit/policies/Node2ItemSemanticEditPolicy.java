package org.supremica.external.sag.diagram.edit.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.gef.commands.UnexecutableCommand;

import org.eclipse.gmf.runtime.emf.type.core.commands.CreateRelationshipCommand;

import org.supremica.external.sag.BoundedZone;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.UnboundedZone;

import org.supremica.external.sag.diagram.providers.SagElementTypes;

/**
 * @generated
 */
public class Node2ItemSemanticEditPolicy extends SagBaseItemSemanticEditPolicy {

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
		if (SagElementTypes.BoundedZone_3001 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingBoundedZone3001Command(req)
					: getCreateCompleteIncomingBoundedZone3001Command(req);
		}
		if (SagElementTypes.BoundedZone_3002 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingBoundedZone3002Command(req)
					: getCreateCompleteIncomingBoundedZone3002Command(req);
		}
		if (SagElementTypes.UnboundedZone_3003 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingUnboundedZone3003Command(req)
					: getCreateCompleteIncomingUnboundedZone3003Command(req);
		}
		if (SagElementTypes.UnboundedZone_3004 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingUnboundedZone3004Command(req)
					: getCreateCompleteIncomingUnboundedZone3004Command(req);
		}
		if (SagElementTypes.UnboundedZone_3005 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingUnboundedZone3005Command(req)
					: getCreateCompleteIncomingUnboundedZone3005Command(req);
		}
		if (SagElementTypes.UnboundedZone_3006 == req.getElementType()) {
			return req.getTarget() == null ? getCreateStartOutgoingUnboundedZone3006Command(req)
					: getCreateCompleteIncomingUnboundedZone3006Command(req);
		}
		return super.getCreateRelationshipCommand(req);
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingBoundedZone3001Command(
			CreateRelationshipRequest req) {
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingBoundedZone3001Command(
			CreateRelationshipRequest req) {
		if (!(req.getSource() instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		final Graph element = (Graph) getRelationshipContainer(req.getSource(),
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (element == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new CreateIncomingBoundedZone3001Command(req) {

			protected EObject getElementToEdit() {
				return element;
			}
		});
	}

	/**
	 * @generated
	 */
	private static class CreateIncomingBoundedZone3001Command extends
			CreateRelationshipCommand {

		/**
		 * @generated
		 */
		public CreateIncomingBoundedZone3001Command(
				CreateRelationshipRequest req) {
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
		protected void setElementToEdit(EObject element) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @generated
		 */
		protected EObject doDefaultElementCreation() {
			BoundedZone newElement = (BoundedZone) super
					.doDefaultElementCreation();
			if (newElement != null) {
				newElement.setFront((Node) getTarget());
				newElement.setBack((Node) getSource());
				SagElementTypes.Initializers.BoundedZone_3001.init(newElement);
			}
			return newElement;
		}
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingBoundedZone3002Command(
			CreateRelationshipRequest req) {
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingBoundedZone3002Command(
			CreateRelationshipRequest req) {
		if (!(req.getSource() instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		final Graph element = (Graph) getRelationshipContainer(req.getSource(),
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (element == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new CreateIncomingBoundedZone3002Command(req) {

			protected EObject getElementToEdit() {
				return element;
			}
		});
	}

	/**
	 * @generated
	 */
	private static class CreateIncomingBoundedZone3002Command extends
			CreateRelationshipCommand {

		/**
		 * @generated
		 */
		public CreateIncomingBoundedZone3002Command(
				CreateRelationshipRequest req) {
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
		protected void setElementToEdit(EObject element) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @generated
		 */
		protected EObject doDefaultElementCreation() {
			BoundedZone newElement = (BoundedZone) super
					.doDefaultElementCreation();
			if (newElement != null) {
				newElement.setFront((Node) getTarget());
				newElement.setBack((Node) getSource());
			}
			return newElement;
		}
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingUnboundedZone3003Command(
			CreateRelationshipRequest req) {
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingUnboundedZone3003Command(
			CreateRelationshipRequest req) {
		if (!(req.getSource() instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		final Graph element = (Graph) getRelationshipContainer(req.getSource(),
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (element == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new CreateIncomingUnboundedZone3003Command(req) {

			protected EObject getElementToEdit() {
				return element;
			}
		});
	}

	/**
	 * @generated
	 */
	private static class CreateIncomingUnboundedZone3003Command extends
			CreateRelationshipCommand {

		/**
		 * @generated
		 */
		public CreateIncomingUnboundedZone3003Command(
				CreateRelationshipRequest req) {
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
		protected void setElementToEdit(EObject element) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @generated
		 */
		protected EObject doDefaultElementCreation() {
			UnboundedZone newElement = (UnboundedZone) super
					.doDefaultElementCreation();
			if (newElement != null) {
				newElement.setFront((Node) getTarget());
				newElement.setBack((Node) getSource());
				SagElementTypes.Initializers.UnboundedZone_3003
						.init(newElement);
			}
			return newElement;
		}
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingUnboundedZone3004Command(
			CreateRelationshipRequest req) {
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingUnboundedZone3004Command(
			CreateRelationshipRequest req) {
		if (!(req.getSource() instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		final Graph element = (Graph) getRelationshipContainer(req.getSource(),
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (element == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new CreateIncomingUnboundedZone3004Command(req) {

			protected EObject getElementToEdit() {
				return element;
			}
		});
	}

	/**
	 * @generated
	 */
	private static class CreateIncomingUnboundedZone3004Command extends
			CreateRelationshipCommand {

		/**
		 * @generated
		 */
		public CreateIncomingUnboundedZone3004Command(
				CreateRelationshipRequest req) {
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
		protected void setElementToEdit(EObject element) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @generated
		 */
		protected EObject doDefaultElementCreation() {
			UnboundedZone newElement = (UnboundedZone) super
					.doDefaultElementCreation();
			if (newElement != null) {
				newElement.setFront((Node) getTarget());
				newElement.setBack((Node) getSource());
			}
			return newElement;
		}
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingUnboundedZone3005Command(
			CreateRelationshipRequest req) {
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingUnboundedZone3005Command(
			CreateRelationshipRequest req) {
		if (!(req.getSource() instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		final Graph element = (Graph) getRelationshipContainer(req.getSource(),
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (element == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new CreateIncomingUnboundedZone3005Command(req) {

			protected EObject getElementToEdit() {
				return element;
			}
		});
	}

	/**
	 * @generated
	 */
	private static class CreateIncomingUnboundedZone3005Command extends
			CreateRelationshipCommand {

		/**
		 * @generated
		 */
		public CreateIncomingUnboundedZone3005Command(
				CreateRelationshipRequest req) {
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
		protected void setElementToEdit(EObject element) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @generated
		 */
		protected EObject doDefaultElementCreation() {
			UnboundedZone newElement = (UnboundedZone) super
					.doDefaultElementCreation();
			if (newElement != null) {
				newElement.setFront((Node) getTarget());
				newElement.setBack((Node) getSource());
				SagElementTypes.Initializers.UnboundedZone_3005
						.init(newElement);
			}
			return newElement;
		}
	}

	/**
	 * @generated
	 */
	protected Command getCreateStartOutgoingUnboundedZone3006Command(
			CreateRelationshipRequest req) {
		return new Command() {
		};
	}

	/**
	 * @generated
	 */
	protected Command getCreateCompleteIncomingUnboundedZone3006Command(
			CreateRelationshipRequest req) {
		if (!(req.getSource() instanceof Node)) {
			return UnexecutableCommand.INSTANCE;
		}
		final Graph element = (Graph) getRelationshipContainer(req.getSource(),
				SagPackage.eINSTANCE.getGraph(), req.getElementType());
		if (element == null) {
			return UnexecutableCommand.INSTANCE;
		}
		if (req.getContainmentFeature() == null) {
			req.setContainmentFeature(SagPackage.eINSTANCE.getGraph_Zone());
		}
		return getMSLWrapper(new CreateIncomingUnboundedZone3006Command(req) {

			protected EObject getElementToEdit() {
				return element;
			}
		});
	}

	/**
	 * @generated
	 */
	private static class CreateIncomingUnboundedZone3006Command extends
			CreateRelationshipCommand {

		/**
		 * @generated
		 */
		public CreateIncomingUnboundedZone3006Command(
				CreateRelationshipRequest req) {
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
		protected void setElementToEdit(EObject element) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @generated
		 */
		protected EObject doDefaultElementCreation() {
			UnboundedZone newElement = (UnboundedZone) super
					.doDefaultElementCreation();
			if (newElement != null) {
				newElement.setFront((Node) getTarget());
				newElement.setBack((Node) getSource());
				SagElementTypes.Initializers.UnboundedZone_3006
						.init(newElement);
			}
			return newElement;
		}
	}
}
