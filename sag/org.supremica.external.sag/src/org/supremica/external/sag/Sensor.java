/**
 * <copyright>
 * </copyright>
 *
 * $Id: Sensor.java,v 1.4 2007-02-13 16:50:51 torda Exp $
 */
package org.supremica.external.sag;

import java.util.Map;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Sensor#getName <em>Name</em>}</li>
 *   <li>{@link org.supremica.external.sag.Sensor#getSignal <em>Signal</em>}</li>
 *   <li>{@link org.supremica.external.sag.Sensor#isInitiallyActivated <em>Initially Activated</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getSensor()
 * @model
 * @generated
 */
public interface Sensor extends Node {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.supremica.external.sag.SagPackage#getSensor_Name()
	 * @model transient="true" volatile="true" derived="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Sensor#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Signal</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.SensorSignal#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Signal</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Signal</em>' reference.
	 * @see #setSignal(SensorSignal)
	 * @see org.supremica.external.sag.SagPackage#getSensor_Signal()
	 * @see org.supremica.external.sag.SensorSignal#getSensor
	 * @model opposite="sensor" required="true"
	 * @generated
	 */
	SensorSignal getSignal();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Sensor#getSignal <em>Signal</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Signal</em>' reference.
	 * @see #getSignal()
	 * @generated
	 */
	void setSignal(SensorSignal value);

	/**
	 * Returns the value of the '<em><b>Initially Activated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Initially Activated</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Initially Activated</em>' attribute.
	 * @see #setInitiallyActivated(boolean)
	 * @see org.supremica.external.sag.SagPackage#getSensor_InitiallyActivated()
	 * @model
	 * @generated
	 */
	boolean isInitiallyActivated();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Sensor#isInitiallyActivated <em>Initially Activated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Initially Activated</em>' attribute.
	 * @see #isInitiallyActivated()
	 * @generated
	 */
	void setInitiallyActivated(boolean value);

} // Sensor
