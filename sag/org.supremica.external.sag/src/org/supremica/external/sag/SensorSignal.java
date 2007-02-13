
/**
 * <copyright>
 * </copyright>
 *
 * $Id: SensorSignal.java,v 1.1 2007-02-13 16:50:51 torda Exp $
 */
package org.supremica.external.sag;

import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Signal</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.SensorSignal#getSensor <em>Sensor</em>}</li>
 *   <li>{@link org.supremica.external.sag.SensorSignal#getProject <em>Project</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getSensorSignal()
 * @model
 * @generated
 */
public interface SensorSignal extends Named {
	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Sensor}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Sensor#getSignal <em>Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' reference list.
	 * @see org.supremica.external.sag.SagPackage#getSensorSignal_Sensor()
	 * @see org.supremica.external.sag.Sensor#getSignal
	 * @model type="org.supremica.external.sag.Sensor" opposite="signal" transient="true"
	 * @generated
	 */
	EList<Sensor> getSensor();

	/**
	 * Returns the value of the '<em><b>Project</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Project#getSensorSignal <em>Sensor Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Project</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Project</em>' container reference.
	 * @see #setProject(Project)
	 * @see org.supremica.external.sag.SagPackage#getSensorSignal_Project()
	 * @see org.supremica.external.sag.Project#getSensorSignal
	 * @model opposite="sensorSignal" required="true"
	 * @generated
	 */
	Project getProject();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.SensorSignal#getProject <em>Project</em>}' container reference.
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
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='project.sensorSignal->forAll(s | s = self or s.name <> self.name)'"
	 * @generated
	 */
	boolean validateUniquenessOfName(DiagnosticChain diagnostics, Map<?, ?> context);

} // SensorSignal
