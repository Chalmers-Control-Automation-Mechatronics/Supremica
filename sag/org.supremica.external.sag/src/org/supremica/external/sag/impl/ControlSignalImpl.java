
/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.supremica.external.sag.ControlSignal;
import org.supremica.external.sag.SagPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Control Signal</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.ControlSignalImpl#isSynthesize <em>Synthesize</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ControlSignalImpl extends NamedImpl implements ControlSignal {
	/**
	 * The default value of the '{@link #isSynthesize() <em>Synthesize</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSynthesize()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SYNTHESIZE_EDEFAULT = true;
	/**
	 * The cached value of the '{@link #isSynthesize() <em>Synthesize</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSynthesize()
	 * @generated
	 * @ordered
	 */
	protected boolean synthesize = SYNTHESIZE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ControlSignalImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.CONTROL_SIGNAL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSynthesize() {
		return synthesize;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSynthesize(boolean newSynthesize) {
		boolean oldSynthesize = synthesize;
		synthesize = newSynthesize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.CONTROL_SIGNAL__SYNTHESIZE, oldSynthesize, synthesize));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SagPackage.CONTROL_SIGNAL__SYNTHESIZE:
				return isSynthesize() ? Boolean.TRUE : Boolean.FALSE;
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
			case SagPackage.CONTROL_SIGNAL__SYNTHESIZE:
				setSynthesize(((Boolean)newValue).booleanValue());
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
			case SagPackage.CONTROL_SIGNAL__SYNTHESIZE:
				setSynthesize(SYNTHESIZE_EDEFAULT);
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
			case SagPackage.CONTROL_SIGNAL__SYNTHESIZE:
				return synthesize != SYNTHESIZE_EDEFAULT;
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
		result.append(" (synthesize: ");
		result.append(synthesize);
		result.append(')');
		return result.toString();
	}

} //ControlSignalImpl
