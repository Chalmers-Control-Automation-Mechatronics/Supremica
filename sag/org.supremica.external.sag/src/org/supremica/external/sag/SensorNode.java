/**
 * <copyright>
 * </copyright>
 *
 * $Id: SensorNode.java,v 1.1 2007-01-12 14:23:46 torda Exp $
 */
package org.supremica.external.sag;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.SensorNode#getSensorName <em>Sensor Name</em>}</li>
 *   <li>{@link org.supremica.external.sag.SensorNode#getSensor <em>Sensor</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getSensorNode()
 * @model
 * @generated
 */
public interface SensorNode extends Node {
	/**
	 * Returns the value of the '<em><b>Sensor Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Name</em>' attribute.
	 * @see #setSensorName(String)
	 * @see org.supremica.external.sag.SagPackage#getSensorNode_SensorName()
	 * @model transient="true" volatile="true" derived="true"
	 * @generated
	 */
	String getSensorName();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.SensorNode#getSensorName <em>Sensor Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Name</em>' attribute.
	 * @see #getSensorName()
	 * @generated
	 */
	void setSensorName(String value);

	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Sensor#getNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' reference.
	 * @see #setSensor(Sensor)
	 * @see org.supremica.external.sag.SagPackage#getSensorNode_Sensor()
	 * @see org.supremica.external.sag.Sensor#getNode
	 * @model opposite="node" required="true"
	 * @generated
	 */
	Sensor getSensor();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.SensorNode#getSensor <em>Sensor</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor</em>' reference.
	 * @see #getSensor()
	 * @generated
	 */
	void setSensor(Sensor value);

} // SensorNode
