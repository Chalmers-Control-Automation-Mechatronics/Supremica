/**
 * <copyright>
 * </copyright>
 *
 * $Id: Node.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Node#getSensor <em>Sensor</em>}</li>
 *   <li>{@link org.supremica.external.sag.Node#getGraph <em>Graph</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getNode()
 * @model
 * @generated
 */
public interface Node extends EObject {
	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' attribute.
	 * @see #setSensor(String)
	 * @see org.supremica.external.sag.SagPackage#getNode_Sensor()
	 * @model
	 * @generated
	 */
	String getSensor();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Node#getSensor <em>Sensor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor</em>' attribute.
	 * @see #getSensor()
	 * @generated
	 */
	void setSensor(String value);

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

} // Node
