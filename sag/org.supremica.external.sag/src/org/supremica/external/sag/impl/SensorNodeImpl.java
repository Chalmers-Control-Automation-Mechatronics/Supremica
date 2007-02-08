/**
 * <copyright>
 * </copyright>
 *
 * $Id: SensorNodeImpl.java,v 1.2 2007-02-08 16:36:08 torda Exp $
 */
package org.supremica.external.sag.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.supremica.external.sag.SagFactory;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Sensor;
import org.supremica.external.sag.SensorNode;

import static org.supremica.external.sag.util.OclHelper.evaluate;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.SensorNodeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.SensorNodeImpl#getSensor <em>Sensor</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SensorNodeImpl extends NodeImpl implements SensorNode {
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
	 * The cached value of the '{@link #getSensor() <em>Sensor</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensor()
	 * @generated
	 * @ordered
	 */
	protected Sensor sensor = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.SENSOR_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String getName() {
		return getSensor() != null ? getSensor().getName() : null;
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
		Sensor existingSensorWithThisName = (Sensor) evaluate(this,
				"graph.project.sensor->any(name='"+newName+"')");
		if (existingSensorWithThisName != null) {
			setSensor(existingSensorWithThisName);
		} else if (getSensor() != null) {
			getSensor().setName(newName);
		} else {
			Sensor newSensor = SagFactory.eINSTANCE.createSensor();
			newSensor.setName(newName);
			getGraph().getProject().getSensor().add(newSensor);
			setSensor(newSensor);
		}
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR_NODE__NAME, oldName, newName));

	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Sensor getSensor() {
		if (sensor != null && sensor.eIsProxy()) {
			InternalEObject oldSensor = (InternalEObject)sensor;
			sensor = (Sensor)eResolveProxy(oldSensor);
			if (sensor != oldSensor) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SagPackage.SENSOR_NODE__SENSOR, oldSensor, sensor));
			}
		}
		return sensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Sensor basicGetSensor() {
		return sensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSensor(Sensor newSensor, NotificationChain msgs) {
		Sensor oldSensor = sensor;
		sensor = newSensor;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR_NODE__SENSOR, oldSensor, newSensor);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSensor(Sensor newSensor) {
		if (newSensor != sensor) {
			NotificationChain msgs = null;
			if (sensor != null)
				msgs = ((InternalEObject)sensor).eInverseRemove(this, SagPackage.SENSOR__NODE, Sensor.class, msgs);
			if (newSensor != null)
				msgs = ((InternalEObject)newSensor).eInverseAdd(this, SagPackage.SENSOR__NODE, Sensor.class, msgs);
			msgs = basicSetSensor(newSensor, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR_NODE__SENSOR, newSensor, newSensor));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.SENSOR_NODE__SENSOR:
				if (sensor != null)
					msgs = ((InternalEObject)sensor).eInverseRemove(this, SagPackage.SENSOR__NODE, Sensor.class, msgs);
				return basicSetSensor((Sensor)otherEnd, msgs);
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
			case SagPackage.SENSOR_NODE__SENSOR:
				return basicSetSensor(null, msgs);
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
			case SagPackage.SENSOR_NODE__NAME:
				return getName();
			case SagPackage.SENSOR_NODE__SENSOR:
				if (resolve) return getSensor();
				return basicGetSensor();
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
			case SagPackage.SENSOR_NODE__NAME:
				setName((String)newValue);
				return;
			case SagPackage.SENSOR_NODE__SENSOR:
				setSensor((Sensor)newValue);
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
			case SagPackage.SENSOR_NODE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case SagPackage.SENSOR_NODE__SENSOR:
				setSensor((Sensor)null);
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
			case SagPackage.SENSOR_NODE__NAME:
				return NAME_EDEFAULT == null ? getName() != null : !NAME_EDEFAULT.equals(getName());
			case SagPackage.SENSOR_NODE__SENSOR:
				return sensor != null;
		}
		return super.eIsSet(featureID);
	}

} //SensorNodeImpl
