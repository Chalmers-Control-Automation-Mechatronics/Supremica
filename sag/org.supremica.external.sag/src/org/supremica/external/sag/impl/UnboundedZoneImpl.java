/**
 * <copyright>
 * </copyright>
 *
 * $Id: UnboundedZoneImpl.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.UnboundedZone;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Unbounded Zone</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.UnboundedZoneImpl#isIsOutside <em>Is Outside</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class UnboundedZoneImpl extends ZoneImpl implements UnboundedZone {
	/**
	 * The default value of the '{@link #isIsOutside() <em>Is Outside</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsOutside()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_OUTSIDE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsOutside() <em>Is Outside</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsOutside()
	 * @generated
	 * @ordered
	 */
	protected boolean isOutside = IS_OUTSIDE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UnboundedZoneImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.UNBOUNDED_ZONE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isIsOutside() {
		return isOutside;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsOutside(boolean newIsOutside) {
		boolean oldIsOutside = isOutside;
		isOutside = newIsOutside;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.UNBOUNDED_ZONE__IS_OUTSIDE, oldIsOutside, isOutside));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SagPackage.UNBOUNDED_ZONE__IS_OUTSIDE:
				return isIsOutside() ? Boolean.TRUE : Boolean.FALSE;
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
			case SagPackage.UNBOUNDED_ZONE__IS_OUTSIDE:
				setIsOutside(((Boolean)newValue).booleanValue());
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
			case SagPackage.UNBOUNDED_ZONE__IS_OUTSIDE:
				setIsOutside(IS_OUTSIDE_EDEFAULT);
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
			case SagPackage.UNBOUNDED_ZONE__IS_OUTSIDE:
				return isOutside != IS_OUTSIDE_EDEFAULT;
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
		result.append(" (isOutside: ");
		result.append(isOutside);
		result.append(')');
		return result.toString();
	}

} //UnboundedZoneImpl
