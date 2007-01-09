/**
 * <copyright>
 * </copyright>
 *
 * $Id: Project.java,v 1.2 2007-01-09 15:31:07 torda Exp $
 */
package org.supremica.external.sag;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Project</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Project#getGraph <em>Graph</em>}</li>
 *   <li>{@link org.supremica.external.sag.Project#getSensor <em>Sensor</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getProject()
 * @model
 * @generated
 */
public interface Project extends Named {
	/**
	 * Returns the value of the '<em><b>Graph</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Graph}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Graph#getProject <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Graph</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Graph</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getProject_Graph()
	 * @see org.supremica.external.sag.Graph#getProject
	 * @model type="org.supremica.external.sag.Graph" opposite="project" containment="true"
	 * @generated
	 */
	EList<Graph> getGraph();

	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Sensor}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getProject_Sensor()
	 * @model type="org.supremica.external.sag.Sensor" containment="true"
	 * @generated
	 */
	EList<Sensor> getSensor();

} // Project
