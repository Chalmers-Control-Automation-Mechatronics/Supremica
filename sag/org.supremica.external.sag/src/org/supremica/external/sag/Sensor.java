/**
 * <copyright>
 * </copyright>
 *
 * $Id: Sensor.java,v 1.3 2007-01-23 09:55:48 torda Exp $
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
 *   <li>{@link org.supremica.external.sag.Sensor#getNode <em>Node</em>}</li>
 *   <li>{@link org.supremica.external.sag.Sensor#getProject <em>Project</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getSensor()
 * @model
 * @generated
 */
public interface Sensor extends Named {
	/**
	 * Returns the value of the '<em><b>Node</b></em>' reference list.
	 * The list contents are of type {@link org.supremica.external.sag.SensorNode}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.SensorNode#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Node</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node</em>' reference list.
	 * @see org.supremica.external.sag.SagPackage#getSensor_Node()
	 * @see org.supremica.external.sag.SensorNode#getSensor
	 * @model type="org.supremica.external.sag.SensorNode" opposite="sensor" transient="true"
	 * @generated
	 */
	EList<SensorNode> getNode();

	/**
	 * Returns the value of the '<em><b>Project</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Project#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Project</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Project</em>' container reference.
	 * @see #setProject(Project)
	 * @see org.supremica.external.sag.SagPackage#getSensor_Project()
	 * @see org.supremica.external.sag.Project#getSensor
	 * @model opposite="sensor" required="true"
	 * @generated
	 */
	Project getProject();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Sensor#getProject <em>Project</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Project</em>' container reference.
	 * @see #getProject()
	 * @generated
	 */
	void setProject(Project value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='name <> \'\' and name <> null'"
	 * @generated
	 */
	boolean validateName(DiagnosticChain diagnostics, Map<?, ?> context);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='project.sensor->forAll(s | s = self or s.name <> self.name)'"
	 * @generated
	 */
	boolean validateUniquenessOfName(DiagnosticChain diagnostics, Map<?, ?> context);

} // Sensor
