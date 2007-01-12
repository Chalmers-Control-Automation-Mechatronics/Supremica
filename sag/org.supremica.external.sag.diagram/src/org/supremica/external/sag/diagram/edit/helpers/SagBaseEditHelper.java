package org.supremica.external.sag.diagram.edit.helpers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.AbstractEditHelper;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyDependentsRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.supremica.external.sag.EndNode;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.Zone;

/**
 * @generated
 */
public class SagBaseEditHelper extends AbstractEditHelper {

	/**
	 * @generated
	 */
	public static final String EDIT_POLICY_COMMAND = "edit policy command"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	protected ICommand getInsteadCommand(IEditCommandRequest req) {
		ICommand epCommand = (ICommand) req.getParameter(EDIT_POLICY_COMMAND);
		req.setParameter(EDIT_POLICY_COMMAND, null);
		ICommand ehCommand = super.getInsteadCommand(req);
		if (epCommand == null) {
			return ehCommand;
		}
		if (ehCommand == null) {
			return epCommand;
		}
		CompositeCommand command = new CompositeCommand(null);
		command.add(epCommand);
		command.add(ehCommand);
		return command;
	}

	/**
	 * @generated
	 */
	protected ICommand getCreateCommand(CreateElementRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected ICommand getCreateRelationshipCommand(
			CreateRelationshipRequest req) {
		return null;
	}

	/**
	 * @generated NOT
	 */
	protected ICommand getDestroyElementCommand(DestroyElementRequest req) {
		return null;
	}

	//For some reason this is not called since these helpers are never created/Tord
	protected ICommand getDestroyDependentsCommand(DestroyDependentsRequest req) {
		if (req.getElementToDestroy() instanceof Zone) {
			Zone zone = (Zone) req.getElementToDestroy();
			Set<Node> endNodesToDestroy = new HashSet<Node>();
			if (zone.getBack() instanceof EndNode) {
				endNodesToDestroy.add(zone.getBack());
			}
			if (zone.getFront() instanceof EndNode) {
				endNodesToDestroy.add(zone.getFront());
			}
			return req.getDestroyDependentsCommand(endNodesToDestroy);
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected ICommand getDestroyReferenceCommand(DestroyReferenceRequest req) {
		return null;
	}
}