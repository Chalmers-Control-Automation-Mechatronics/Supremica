/**
 * <copyright>
 * </copyright>
 *
 * $Id: ZoneImpl.java,v 1.3 2007-01-12 14:23:10 torda Exp $
 */
package org.supremica.external.sag.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Zone;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Zone</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getFront <em>Front</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getBack <em>Back</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#isIsOneway <em>Is Oneway</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getGraph <em>Graph</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class ZoneImpl extends NamedImpl implements Zone {
	/**
	 * The cached value of the '{@link #getFront() <em>Front</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFront()
	 * @generated
	 * @ordered
	 */
	protected Node front = null;

	/**
	 * The cached value of the '{@link #getBack() <em>Back</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBack()
	 * @generated
	 * @ordered
	 */
	protected Node back = null;

	/**
	 * The default value of the '{@link #isIsOneway() <em>Is Oneway</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsOneway()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_ONEWAY_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isIsOneway() <em>Is Oneway</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsOneway()
	 * @generated
	 * @ordered
	 */
	protected boolean isOneway = IS_ONEWAY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ZoneImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.ZONE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node getFront() {
		if (front != null && front.eIsProxy()) {
			InternalEObject oldFront = (InternalEObject)front;
			front = (Node)eResolveProxy(oldFront);
			if (front != oldFront) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SagPackage.ZONE__FRONT, oldFront, front));
			}
		}
		return front;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node basicGetFront() {
		return front;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFront(Node newFront, NotificationChain msgs) {
		Node oldFront = front;
		front = newFront;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__FRONT, oldFront, newFront);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFront(Node newFront) {
		if (newFront != front) {
			NotificationChain msgs = null;
			if (front != null)
				msgs = ((InternalEObject)front).eInverseRemove(this, SagPackage.NODE__INCOMING, Node.class, msgs);
			if (newFront != null)
				msgs = ((InternalEObject)newFront).eInverseAdd(this, SagPackage.NODE__INCOMING, Node.class, msgs);
			msgs = basicSetFront(newFront, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__FRONT, newFront, newFront));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node getBack() {
		if (back != null && back.eIsProxy()) {
			InternalEObject oldBack = (InternalEObject)back;
			back = (Node)eResolveProxy(oldBack);
			if (back != oldBack) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SagPackage.ZONE__BACK, oldBack, back));
			}
		}
		return back;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node basicGetBack() {
		return back;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBack(Node newBack, NotificationChain msgs) {
		Node oldBack = back;
		back = newBack;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__BACK, oldBack, newBack);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBack(Node newBack) {
		if (newBack != back) {
			NotificationChain msgs = null;
			if (back != null)
				msgs = ((InternalEObject)back).eInverseRemove(this, SagPackage.NODE__OUTGOING, Node.class, msgs);
			if (newBack != null)
				msgs = ((InternalEObject)newBack).eInverseAdd(this, SagPackage.NODE__OUTGOING, Node.class, msgs);
			msgs = basicSetBack(newBack, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__BACK, newBack, newBack));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isIsOneway() {
		return isOneway;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsOneway(boolean newIsOneway) {
		boolean oldIsOneway = isOneway;
		isOneway = newIsOneway;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__IS_ONEWAY, oldIsOneway, isOneway));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Graph getGraph() {
		if (eContainerFeatureID != SagPackage.ZONE__GRAPH) return null;
		return (Graph)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetGraph(Graph newGraph, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newGraph, SagPackage.ZONE__GRAPH, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGraph(Graph newGraph) {
		if (newGraph != eInternalContainer() || (eContainerFeatureID != SagPackage.ZONE__GRAPH && newGraph != null)) {
			if (EcoreUtil.isAncestor(this, newGraph))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newGraph != null)
				msgs = ((InternalEObject)newGraph).eInverseAdd(this, SagPackage.GRAPH__ZONE, Graph.class, msgs);
			msgs = basicSetGraph(newGraph, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__GRAPH, newGraph, newGraph));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.ZONE__FRONT:
				if (front != null)
					msgs = ((InternalEObject)front).eInverseRemove(this, SagPackage.NODE__INCOMING, Node.class, msgs);
				return basicSetFront((Node)otherEnd, msgs);
			case SagPackage.ZONE__BACK:
				if (back != null)
					msgs = ((InternalEObject)back).eInverseRemove(this, SagPackage.NODE__OUTGOING, Node.class, msgs);
				return basicSetBack((Node)otherEnd, msgs);
			case SagPackage.ZONE__GRAPH:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetGraph((Graph)otherEnd, msgs);
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
			case SagPackage.ZONE__FRONT:
				return basicSetFront(null, msgs);
			case SagPackage.ZONE__BACK:
				return basicSetBack(null, msgs);
			case SagPackage.ZONE__GRAPH:
				return basicSetGraph(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID) {
			case SagPackage.ZONE__GRAPH:
				return eInternalContainer().eInverseRemove(this, SagPackage.GRAPH__ZONE, Graph.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SagPackage.ZONE__FRONT:
				if (resolve) return getFront();
				return basicGetFront();
			case SagPackage.ZONE__BACK:
				if (resolve) return getBack();
				return basicGetBack();
			case SagPackage.ZONE__IS_ONEWAY:
				return isIsOneway() ? Boolean.TRUE : Boolean.FALSE;
			case SagPackage.ZONE__GRAPH:
				return getGraph();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SagPackage.ZONE__FRONT:
				setFront((Node)newValue);
				return;
			case SagPackage.ZONE__BACK:
				setBack((Node)newValue);
				return;
			case SagPackage.ZONE__IS_ONEWAY:
				setIsOneway(((Boolean)newValue).booleanValue());
				return;
			case SagPackage.ZONE__GRAPH:
				setGraph((Graph)newValue);
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
			case SagPackage.ZONE__FRONT:
				setFront((Node)null);
				return;
			case SagPackage.ZONE__BACK:
				setBack((Node)null);
				return;
			case SagPackage.ZONE__IS_ONEWAY:
				setIsOneway(IS_ONEWAY_EDEFAULT);
				return;
			case SagPackage.ZONE__GRAPH:
				setGraph((Graph)null);
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
			case SagPackage.ZONE__FRONT:
				return front != null;
			case SagPackage.ZONE__BACK:
				return back != null;
			case SagPackage.ZONE__IS_ONEWAY:
				return isOneway != IS_ONEWAY_EDEFAULT;
			case SagPackage.ZONE__GRAPH:
				return getGraph() != null;
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
		result.append(" (isOneway: ");
		result.append(isOneway);
		result.append(')');
		return result.toString();
	}

} //ZoneImpl
