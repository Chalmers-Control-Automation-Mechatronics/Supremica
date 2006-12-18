/**
 * <copyright>
 * </copyright>
 *
 * $Id: Project.java,v 1.1 2006-12-18 15:23:00 torda Exp $
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
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Graph</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Graph</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getProject_Graph()
	 * @model type="org.supremica.external.sag.Graph" containment="true"
	 * @generated
	 */
	EList<Graph> getGraph();

} // Project
