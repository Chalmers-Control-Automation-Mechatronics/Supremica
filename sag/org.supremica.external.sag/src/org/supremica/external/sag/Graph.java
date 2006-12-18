/**
 * <copyright>
 * </copyright>
 *
 * $Id: Graph.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Graph</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Graph#getZone <em>Zone</em>}</li>
 *   <li>{@link org.supremica.external.sag.Graph#isMultipleObjects <em>Multiple Objects</em>}</li>
 *   <li>{@link org.supremica.external.sag.Graph#getNode <em>Node</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getGraph()
 * @model
 * @generated
 */
public interface Graph extends Named {
	/**
	 * Returns the value of the '<em><b>Zone</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Zone}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Zone</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Zone</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getGraph_Zone()
	 * @model type="org.supremica.external.sag.Zone" containment="true"
	 * @generated
	 */
	EList<Zone> getZone();

	/**
	 * Returns the value of the '<em><b>Multiple Objects</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Multiple Objects</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Multiple Objects</em>' attribute.
	 * @see #setMultipleObjects(boolean)
	 * @see org.supremica.external.sag.SagPackage#getGraph_MultipleObjects()
	 * @model
	 * @generated
	 */
	boolean isMultipleObjects();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Graph#isMultipleObjects <em>Multiple Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Multiple Objects</em>' attribute.
	 * @see #isMultipleObjects()
	 * @generated
	 */
	void setMultipleObjects(boolean value);

	/**
	 * Returns the value of the '<em><b>Node</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Node}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Node#getGraph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Node</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getGraph_Node()
	 * @see org.supremica.external.sag.Node#getGraph
	 * @model type="org.supremica.external.sag.Node" opposite="graph" containment="true"
	 * @generated
	 */
	EList<Node> getNode();

} // Graph
