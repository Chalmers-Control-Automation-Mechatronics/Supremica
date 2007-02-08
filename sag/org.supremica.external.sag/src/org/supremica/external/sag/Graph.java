/**
 * <copyright>
 * </copyright>
 *
 * $Id: Graph.java,v 1.5 2007-02-08 16:36:08 torda Exp $
 */
package org.supremica.external.sag;

import java.util.Map;
import org.eclipse.emf.common.util.DiagnosticChain;
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
 *   <li>{@link org.supremica.external.sag.Graph#getMaxNrOfObjects <em>Max Nr Of Objects</em>}</li>
 *   <li>{@link org.supremica.external.sag.Graph#getNode <em>Node</em>}</li>
 *   <li>{@link org.supremica.external.sag.Graph#getProject <em>Project</em>}</li>
 *   <li>{@link org.supremica.external.sag.Graph#isNrOfObjectsIsUnbounded <em>Nr Of Objects Is Unbounded</em>}</li>
 *   <li>{@link org.supremica.external.sag.Graph#getSensor <em>Sensor</em>}</li>
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
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Zone#getGraph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Zone</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Zone</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getGraph_Zone()
	 * @see org.supremica.external.sag.Zone#getGraph
	 * @model type="org.supremica.external.sag.Zone" opposite="graph" containment="true"
	 * @generated
	 */
	EList<Zone> getZone();

	/**
	 * Returns the value of the '<em><b>Max Nr Of Objects</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max Nr Of Objects</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Nr Of Objects</em>' attribute.
	 * @see #setMaxNrOfObjects(int)
	 * @see org.supremica.external.sag.SagPackage#getGraph_MaxNrOfObjects()
	 * @model default="-1"
	 * @generated
	 */
	int getMaxNrOfObjects();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Graph#getMaxNrOfObjects <em>Max Nr Of Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Nr Of Objects</em>' attribute.
	 * @see #getMaxNrOfObjects()
	 * @generated
	 */
	void setMaxNrOfObjects(int value);

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

	/**
	 * Returns the value of the '<em><b>Project</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Project#getGraph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Project</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Project</em>' container reference.
	 * @see #setProject(Project)
	 * @see org.supremica.external.sag.SagPackage#getGraph_Project()
	 * @see org.supremica.external.sag.Project#getGraph
	 * @model opposite="graph"
	 * @generated
	 */
	Project getProject();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Graph#getProject <em>Project</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Project</em>' container reference.
	 * @see #getProject()
	 * @generated
	 */
	void setProject(Project value);

	/**
	 * Returns the value of the '<em><b>Nr Of Objects Is Unbounded</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Nr Of Objects Is Unbounded</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nr Of Objects Is Unbounded</em>' attribute.
	 * @see #setNrOfObjectsIsUnbounded(boolean)
	 * @see org.supremica.external.sag.SagPackage#getGraph_NrOfObjectsIsUnbounded()
	 * @model transient="true" volatile="true" derived="true"
	 * @generated
	 */
	boolean isNrOfObjectsIsUnbounded();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Graph#isNrOfObjectsIsUnbounded <em>Nr Of Objects Is Unbounded</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nr Of Objects Is Unbounded</em>' attribute.
	 * @see #isNrOfObjectsIsUnbounded()
	 * @generated
	 */
	void setNrOfObjectsIsUnbounded(boolean value);

	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' containment reference list.
	 * The list contents are of type {@link org.supremica.external.sag.SensorNode}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sensor</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' containment reference list.
	 * @see org.supremica.external.sag.SagPackage#getGraph_Sensor()
	 * @model type="org.supremica.external.sag.SensorNode" containment="true" transient="true" changeable="false" volatile="true" derived="true"
	 *        annotation="http://www.eclipse.org/OCL/examples/ocl derive='node->select(oclIsKindOf(SensorNode))'"
	 * @generated
	 */
	EList<SensorNode> getSensor();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='nrOfObjectsIsUnbounded implies zone->forAll(oclIsKindOf(UnboundedZone) implies oclAsType(UnboundedZone).isOutside)'"
	 * @generated
	 */
	boolean validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded(DiagnosticChain diagnostics, Map<?, ?> context);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='name <> \'\' and name <> null'"
	 * @generated
	 */
	boolean validateName(DiagnosticChain diagnostics, Map<?, ?> context);

} // Graph
