/**
 * <copyright>
 * </copyright>
 *
 * $Id: Project.java,v 1.4 2007-02-13 16:50:51 torda Exp $
 */
package org.supremica.external.sag;

import java.util.Map;
import org.eclipse.emf.common.util.DiagnosticChain;
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
 *   <li>{@link org.supremica.external.sag.Project#getSensorSignal <em>Sensor Signal</em>}</li>
 *   <li>{@link org.supremica.external.sag.Project#getControlSignal <em>Control Signal</em>}</li>
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
	 * Returns the value of the '<em><b>Sensor Signal</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.SensorSignal}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.SensorSignal#getProject <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor Signal</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Signal</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getProject_SensorSignal()
	 * @see org.supremica.external.sag.SensorSignal#getProject
	 * @model type="org.supremica.external.sag.SensorSignal" opposite="project" containment="true"
	 * @generated
	 */
	EList<SensorSignal> getSensorSignal();

	/**
	 * Returns the value of the '<em><b>Control Signal</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.ControlSignal}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Control Signal</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Control Signal</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getProject_ControlSignal()
	 * @model type="org.supremica.external.sag.ControlSignal" containment="true"
	 * @generated
	 */
	EList<ControlSignal> getControlSignal();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='name <> \'\' and name <> null'"
	 * @generated
	 */
	boolean validateName(DiagnosticChain diagnostics, Map<?, ?> context);

} // Project
