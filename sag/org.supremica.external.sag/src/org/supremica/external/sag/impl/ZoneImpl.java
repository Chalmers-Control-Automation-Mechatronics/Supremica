/**
 * <copyright>
 * </copyright>
 *
 * $Id: ZoneImpl.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

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
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class ZoneImpl extends EObjectImpl implements Zone {
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
	protected static final boolean IS_ONEWAY_EDEFAULT = false;

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
	public void setFront(Node newFront) {
		Node oldFront = front;
		front = newFront;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__FRONT, oldFront, front));
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
	public void setBack(Node newBack) {
		Node oldBack = back;
		back = newBack;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__BACK, oldBack, back));
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
