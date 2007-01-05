/**
 * <copyright>
 * </copyright>
 *
 * $Id: Node.java,v 1.2 2007-01-05 13:29:13 torda Exp $
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
 *   <li>{@link org.supremica.external.sag.Node#getIncoming <em>Incoming</em>}</li>
 *   <li>{@link org.supremica.external.sag.Node#getOutgoing <em>Outgoing</em>}</li>
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

	/**
	 * Returns the value of the '<em><b>Incoming</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Zone#getFront <em>Front</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Incoming</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Incoming</em>' reference.
	 * @see #setIncoming(Zone)
	 * @see org.supremica.external.sag.SagPackage#getNode_Incoming()
	 * @see org.supremica.external.sag.Zone#getFront
	 * @model opposite="front" transient="true"
	 * @generated
	 */
	Zone getIncoming();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Node#getIncoming <em>Incoming</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Incoming</em>' reference.
	 * @see #getIncoming()
	 * @generated
	 */
	void setIncoming(Zone value);

	/**
	 * Returns the value of the '<em><b>Outgoing</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Zone#getBack <em>Back</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outgoing</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outgoing</em>' reference.
	 * @see #setOutgoing(Zone)
	 * @see org.supremica.external.sag.SagPackage#getNode_Outgoing()
	 * @see org.supremica.external.sag.Zone#getBack
	 * @model opposite="back" transient="true"
	 * @generated
	 */
	Zone getOutgoing();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Node#getOutgoing <em>Outgoing</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Outgoing</em>' reference.
	 * @see #getOutgoing()
	 * @generated
	 */
	void setOutgoing(Zone value);

} // Node
