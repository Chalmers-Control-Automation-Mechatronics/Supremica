/**
 * <copyright>
 * </copyright>
 *
 * $Id: Node.java,v 1.3 2007-01-09 15:31:07 torda Exp $
 */
package org.supremica.external.sag;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Node#getGraph <em>Graph</em>}</li>
 *   <li>{@link org.supremica.external.sag.Node#getIncoming <em>Incoming</em>}</li>
 *   <li>{@link org.supremica.external.sag.Node#getOutgoing <em>Outgoing</em>}</li>
 *   <li>{@link org.supremica.external.sag.Node#getSensor <em>Sensor</em>}</li>
 *   <li>{@link org.supremica.external.sag.Node#getSensorName <em>Sensor Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getNode()
 * @model
 * @generated
 */
public interface Node extends EObject {
	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Sensor#getNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' reference.
	 * @see #setSensor(Sensor)
	 * @see org.supremica.external.sag.SagPackage#getNode_Sensor()
	 * @see org.supremica.external.sag.Sensor#getNode
	 * @model opposite="node"
	 * @generated
	 */
	Sensor getSensor();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Node#getSensor <em>Sensor</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor</em>' reference.
	 * @see #getSensor()
	 * @generated
	 */
	void setSensor(Sensor value);

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
	 * @see org.supremica.external.sag.SagPackage#getNode_SensorName()
	 * @model transient="true" volatile="true" derived="true"
	 * @generated
	 */
	String getSensorName();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Node#getSensorName <em>Sensor Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Name</em>' attribute.
	 * @see #getSensorName()
	 * @generated
	 */
	void setSensorName(String value);

	/**
	 * Returns the value of the '<em><b>Graph</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Graph#getNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Graph</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Graph</em>' container reference.
	 * @see #setGraph(Graph)
	 * @see org.supremica.external.sag.SagPackage#getNode_Graph()
	 * @see org.supremica.external.sag.Graph#getNode
	 * @model opposite="node"
	 * @generated
	 */
	Graph getGraph();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Node#getGraph <em>Graph</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Graph</em>' container reference.
	 * @see #getGraph()
	 * @generated
	 */
	void setGraph(Graph value);

	/**
	 * Returns the value of the '<em><b>Incoming</b></em>' reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Zone}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Zone#getFront <em>Front</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Incoming</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Incoming</em>' reference list.
	 * @see org.supremica.external.sag.SagPackage#getNode_Incoming()
	 * @see org.supremica.external.sag.Zone#getFront
	 * @model type="org.supremica.external.sag.Zone" opposite="front" transient="true"
	 * @generated
	 */
	EList<Zone> getIncoming();

	/**
	 * Returns the value of the '<em><b>Outgoing</b></em>' reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Zone}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Zone#getBack <em>Back</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outgoing</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outgoing</em>' reference list.
	 * @see org.supremica.external.sag.SagPackage#getNode_Outgoing()
	 * @see org.supremica.external.sag.Zone#getBack
	 * @model type="org.supremica.external.sag.Zone" opposite="back" transient="true"
	 * @generated
	 */
	EList<Zone> getOutgoing();

} // Node
