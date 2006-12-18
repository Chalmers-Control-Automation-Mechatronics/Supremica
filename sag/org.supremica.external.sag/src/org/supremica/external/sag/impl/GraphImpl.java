/**
 * <copyright>
 * </copyright>
 *
 * $Id: GraphImpl.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Zone;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Graph</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getZone <em>Zone</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#isMultipleObjects <em>Multiple Objects</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getNode <em>Node</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GraphImpl extends NamedImpl implements Graph {
	/**
	 * The cached value of the '{@link #getZone() <em>Zone</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZone()
	 * @generated
	 * @ordered
	 */
	protected EList<Zone> zone = null;

	/**
	 * The default value of the '{@link #isMultipleObjects() <em>Multiple Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultipleObjects()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MULTIPLE_OBJECTS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isMultipleObjects() <em>Multiple Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultipleObjects()
	 * @generated
	 * @ordered
	 */
	protected boolean multipleObjects = MULTIPLE_OBJECTS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getNode() <em>Node</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNode()
	 * @generated
	 * @ordered
	 */
	protected EList<Node> node = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GraphImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.GRAPH;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Zone> getZone() {
		if (zone == null) {
			zone = new EObjectContainmentEList<Zone>(Zone.class, this, SagPackage.GRAPH__ZONE);
		}
		return zone;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isMultipleObjects() {
		return multipleObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMultipleObjects(boolean newMultipleObjects) {
		boolean oldMultipleObjects = multipleObjects;
		multipleObjects = newMultipleObjects;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.GRAPH__MULTIPLE_OBJECTS, oldMultipleObjects, multipleObjects));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Node> getNode() {
		if (node == null) {
			node = new EObjectContainmentWithInverseEList<Node>(Node.class, this, SagPackage.GRAPH__NODE, SagPackage.NODE__GRAPH);
		}
		return node;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.GRAPH__NODE:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getNode()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return ((InternalEList<?>)getZone()).basicRemove(otherEnd, msgs);
			case SagPackage.GRAPH__NODE:
				return ((InternalEList<?>)getNode()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return getZone();
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				return isMultipleObjects() ? Boolean.TRUE : Boolean.FALSE;
			case SagPackage.GRAPH__NODE:
				return getNode();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				getZone().clear();
				getZone().addAll((Collection<? extends Zone>)newValue);
				return;
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				setMultipleObjects(((Boolean)newValue).booleanValue());
				return;
			case SagPackage.GRAPH__NODE:
				getNode().clear();
				getNode().addAll((Collection<? extends Node>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				getZone().clear();
				return;
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				setMultipleObjects(MULTIPLE_OBJECTS_EDEFAULT);
				return;
			case SagPackage.GRAPH__NODE:
				getNode().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return zone != null && !zone.isEmpty();
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				return multipleObjects != MULTIPLE_OBJECTS_EDEFAULT;
			case SagPackage.GRAPH__NODE:
				return node != null && !node.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (multipleObjects: ");
		result.append(multipleObjects);
		result.append(')');
		return result.toString();
	}

} //GraphImpl
