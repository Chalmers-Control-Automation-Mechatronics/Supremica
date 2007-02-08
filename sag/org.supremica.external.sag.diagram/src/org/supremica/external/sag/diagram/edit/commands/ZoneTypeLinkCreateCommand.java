package org.supremica.external.sag.diagram.edit.commands;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.gmf.runtime.emf.type.core.commands.CreateRelationshipCommand;

import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;

import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Zone;

/**
 * @generated
 */
public class ZoneTypeLinkCreateCommand extends CreateRelationshipCommand {

	/**
	 * @generated
	 */
	private Graph myContainer;

	/**
	 * @generated
	 */
	private Node mySource;

	/**
	 * @generated
	 */
	private Node myTarget;

	/**
	 * @generated
	 */
	public ZoneTypeLinkCreateCommand(CreateRelationshipRequest req,
			Graph container, Node source, Node target) {
		super(req);
		super.setElementToEdit(container);
		myContainer = container;
		mySource = source;
		myTarget = target;
	}

	/**
	 * @generated
	 */
	public Graph getContainer() {
		return myContainer;
	}

	/**
	 * @generated
	 */
	public EObject getSource() {
		return mySource;
	}

	/**
	 * @generated
	 */
	public EObject getTarget() {
		return myTarget;
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
		Zone newElement = (Zone) super.doDefaultElementCreation();
		if (newElement != null) {
			newElement.setFront(myTarget);
			newElement.setBack(mySource);
		}
		return newElement;
	}

}
