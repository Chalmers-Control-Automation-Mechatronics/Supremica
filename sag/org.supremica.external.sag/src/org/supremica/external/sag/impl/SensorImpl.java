/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag.impl;

import static org.supremica.external.sag.util.OclHelper.evaluate;

import java.util.Collection;
import java.util.Map;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ocl.expressions.OCLExpression;
import org.eclipse.emf.ocl.expressions.util.EvalEnvironment;
import org.eclipse.emf.ocl.expressions.util.ExpressionsUtil;
import org.eclipse.emf.ocl.parser.Environment;
import org.eclipse.emf.ocl.parser.ParserException;
import org.eclipse.emf.ocl.query.Query;
import org.eclipse.emf.ocl.query.QueryFactory;
import org.supremica.external.sag.Project;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagFactory;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Sensor;
import org.supremica.external.sag.SensorSignal;
import org.supremica.external.sag.util.SagValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.SensorImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.SensorImpl#getSignal <em>Signal</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.SensorImpl#isInitiallyActivated <em>Initially Activated</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SensorImpl extends NodeImpl implements Sensor {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSignal() <em>Signal</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSignal()
	 * @generated
	 * @ordered
	 */
	protected SensorSignal signal = null;

	/**
	 * The default value of the '{@link #isInitiallyActivated() <em>Initially Activated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isInitiallyActivated()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INITIALLY_ACTIVATED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isInitiallyActivated() <em>Initially Activated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isInitiallyActivated()
	 * @generated
	 * @ordered
	 */
	protected boolean initiallyActivated = INITIALLY_ACTIVATED_EDEFAULT;

	private static final String OCL_ANNOTATION_SOURCE = "http://www.eclipse.org/OCL/examples/ocl";

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.SENSOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String getName() {
		return getSignal() != null ? getSignal().getName() : null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setName(String newName) {
		if (newName == null || newName.equals("")) {
			return;
		}
		String oldName = getName();
		SensorSignal existingSignalWithThisName = (SensorSignal) evaluate(this,
				"graph.project.sensorSignal->any(name='"+newName+"')");
		if (existingSignalWithThisName != null) {
			setSignal(existingSignalWithThisName);
		} else if (getSignal() != null) {
			getSignal().setName(newName);
		} else {
			SensorSignal newSignal = SagFactory.eINSTANCE.createSensorSignal();
			newSignal.setName(newName);
			getGraph().getProject().getSensorSignal().add(newSignal);
			setSignal(newSignal);
		}
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR__NAME, oldName, newName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensorSignal getSignal() {
		if (signal != null && signal.eIsProxy()) {
			InternalEObject oldSignal = (InternalEObject)signal;
			signal = (SensorSignal)eResolveProxy(oldSignal);
			if (signal != oldSignal) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SagPackage.SENSOR__SIGNAL, oldSignal, signal));
			}
		}
		return signal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensorSignal basicGetSignal() {
		return signal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSignal(SensorSignal newSignal, NotificationChain msgs) {
		SensorSignal oldSignal = signal;
		signal = newSignal;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR__SIGNAL, oldSignal, newSignal);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSignal(SensorSignal newSignal) {
		if (newSignal != signal) {
			NotificationChain msgs = null;
			if (signal != null)
				msgs = ((InternalEObject)signal).eInverseRemove(this, SagPackage.SENSOR_SIGNAL__SENSOR, SensorSignal.class, msgs);
			if (newSignal != null)
				msgs = ((InternalEObject)newSignal).eInverseAdd(this, SagPackage.SENSOR_SIGNAL__SENSOR, SensorSignal.class, msgs);
			msgs = basicSetSignal(newSignal, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR__SIGNAL, newSignal, newSignal));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isInitiallyActivated() {
		return initiallyActivated;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInitiallyActivated(boolean newInitiallyActivated) {
		boolean oldInitiallyActivated = initiallyActivated;
		initiallyActivated = newInitiallyActivated;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR__INITIALLY_ACTIVATED, oldInitiallyActivated, initiallyActivated));
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
			case SagPackage.SENSOR__SIGNAL:
				if (signal != null)
					msgs = ((InternalEObject)signal).eInverseRemove(this, SagPackage.SENSOR_SIGNAL__SENSOR, SensorSignal.class, msgs);
				return basicSetSignal((SensorSignal)otherEnd, msgs);
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
			case SagPackage.SENSOR__SIGNAL:
				return basicSetSignal(null, msgs);
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
			case SagPackage.SENSOR__NAME:
				return getName();
			case SagPackage.SENSOR__SIGNAL:
				if (resolve) return getSignal();
				return basicGetSignal();
			case SagPackage.SENSOR__INITIALLY_ACTIVATED:
				return isInitiallyActivated() ? Boolean.TRUE : Boolean.FALSE;
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
			case SagPackage.SENSOR__NAME:
				setName((String)newValue);
				return;
			case SagPackage.SENSOR__SIGNAL:
				setSignal((SensorSignal)newValue);
				return;
			case SagPackage.SENSOR__INITIALLY_ACTIVATED:
				setInitiallyActivated(((Boolean)newValue).booleanValue());
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
			case SagPackage.SENSOR__NAME:
				setName(NAME_EDEFAULT);
				return;
			case SagPackage.SENSOR__SIGNAL:
				setSignal((SensorSignal)null);
				return;
			case SagPackage.SENSOR__INITIALLY_ACTIVATED:
				setInitiallyActivated(INITIALLY_ACTIVATED_EDEFAULT);
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
			case SagPackage.SENSOR__NAME:
				return NAME_EDEFAULT == null ? getName() != null : !NAME_EDEFAULT.equals(getName());
			case SagPackage.SENSOR__SIGNAL:
				return signal != null;
			case SagPackage.SENSOR__INITIALLY_ACTIVATED:
				return initiallyActivated != INITIALLY_ACTIVATED_EDEFAULT;
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
		result.append(" (initiallyActivated: ");
		result.append(initiallyActivated);
		result.append(')');
		return result.toString();
	}

} //SensorImpl
