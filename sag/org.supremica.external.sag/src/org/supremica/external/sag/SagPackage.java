/**
 * <copyright>
 * </copyright>
 *
 * $Id: SagPackage.java,v 1.3 2007-01-09 15:31:07 torda Exp $
 */
package org.supremica.external.sag;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.supremica.external.sag.SagFactory
 * @model kind="package"
 * @generated
 */
public interface SagPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "sag";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://supremica.org/external/sag";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "org.supremica.external";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SagPackage eINSTANCE = org.supremica.external.sag.impl.SagPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.NamedImpl <em>Named</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.NamedImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getNamed()
	 * @generated
	 */
	int NAMED = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED__NAME = 0;

	/**
	 * The number of structural features of the '<em>Named</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.GraphImpl <em>Graph</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.GraphImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getGraph()
	 * @generated
	 */
	int GRAPH = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__NAME = NAMED__NAME;

	/**
	 * The feature id for the '<em><b>Zone</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__ZONE = NAMED_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Multiple Objects</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__MULTIPLE_OBJECTS = NAMED_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__NODE = NAMED_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Project</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__PROJECT = NAMED_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Graph</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH_FEATURE_COUNT = NAMED_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.ZoneImpl <em>Zone</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.ZoneImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getZone()
	 * @generated
	 */
	int ZONE = 1;

	/**
	 * The feature id for the '<em><b>Front</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__FRONT = 0;

	/**
	 * The feature id for the '<em><b>Back</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__BACK = 1;

	/**
	 * The feature id for the '<em><b>Is Oneway</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__IS_ONEWAY = 2;

	/**
	 * The number of structural features of the '<em>Zone</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.BoundedZoneImpl <em>Bounded Zone</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.BoundedZoneImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getBoundedZone()
	 * @generated
	 */
	int BOUNDED_ZONE = 2;

	/**
	 * The feature id for the '<em><b>Front</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOUNDED_ZONE__FRONT = ZONE__FRONT;

	/**
	 * The feature id for the '<em><b>Back</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOUNDED_ZONE__BACK = ZONE__BACK;

	/**
	 * The feature id for the '<em><b>Is Oneway</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOUNDED_ZONE__IS_ONEWAY = ZONE__IS_ONEWAY;

	/**
	 * The feature id for the '<em><b>Capacity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOUNDED_ZONE__CAPACITY = ZONE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Bounded Zone</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOUNDED_ZONE_FEATURE_COUNT = ZONE_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.UnboundedZoneImpl <em>Unbounded Zone</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.UnboundedZoneImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getUnboundedZone()
	 * @generated
	 */
	int UNBOUNDED_ZONE = 4;

	/**
	 * The feature id for the '<em><b>Front</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNBOUNDED_ZONE__FRONT = ZONE__FRONT;

	/**
	 * The feature id for the '<em><b>Back</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNBOUNDED_ZONE__BACK = ZONE__BACK;

	/**
	 * The feature id for the '<em><b>Is Oneway</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNBOUNDED_ZONE__IS_ONEWAY = ZONE__IS_ONEWAY;

	/**
	 * The feature id for the '<em><b>Is Outside</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNBOUNDED_ZONE__IS_OUTSIDE = ZONE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Unbounded Zone</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNBOUNDED_ZONE_FEATURE_COUNT = ZONE_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.NodeImpl <em>Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.NodeImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getNode()
	 * @generated
	 */
	int NODE = 5;

	/**
	 * The feature id for the '<em><b>Graph</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__GRAPH = 0;

	/**
	 * The feature id for the '<em><b>Incoming</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__INCOMING = 1;

	/**
	 * The feature id for the '<em><b>Outgoing</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__OUTGOING = 2;

	/**
	 * The feature id for the '<em><b>Sensor</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__SENSOR = 3;

	/**
	 * The feature id for the '<em><b>Sensor Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE__SENSOR_NAME = 4;

	/**
	 * The number of structural features of the '<em>Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.ProjectImpl <em>Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.ProjectImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getProject()
	 * @generated
	 */
	int PROJECT = 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__NAME = NAMED__NAME;

	/**
	 * The feature id for the '<em><b>Graph</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__GRAPH = NAMED_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Sensor</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__SENSOR = NAMED_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT_FEATURE_COUNT = NAMED_FEATURE_COUNT + 2;


	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.SensorImpl <em>Sensor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.SensorImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getSensor()
	 * @generated
	 */
	int SENSOR = 7;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__NAME = NAMED__NAME;

	/**
	 * The feature id for the '<em><b>Node</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__NODE = NAMED_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Sensor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_FEATURE_COUNT = NAMED_FEATURE_COUNT + 1;


	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.Graph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Graph</em>'.
	 * @see org.supremica.external.sag.Graph
	 * @generated
	 */
	EClass getGraph();

	/**
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Graph#getZone <em>Zone</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Zone</em>'.
	 * @see org.supremica.external.sag.Graph#getZone()
	 * @see #getGraph()
	 * @generated
	 */
	EReference getGraph_Zone();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Graph#isMultipleObjects <em>Multiple Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Multiple Objects</em>'.
	 * @see org.supremica.external.sag.Graph#isMultipleObjects()
	 * @see #getGraph()
	 * @generated
	 */
	EAttribute getGraph_MultipleObjects();

	/**
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Graph#getNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Node</em>'.
	 * @see org.supremica.external.sag.Graph#getNode()
	 * @see #getGraph()
	 * @generated
	 */
	EReference getGraph_Node();

	/**
	 * Returns the meta object for the container reference '{@link org.supremica.external.sag.Graph#getProject <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Project</em>'.
	 * @see org.supremica.external.sag.Graph#getProject()
	 * @see #getGraph()
	 * @generated
	 */
	EReference getGraph_Project();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.Zone <em>Zone</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Zone</em>'.
	 * @see org.supremica.external.sag.Zone
	 * @generated
	 */
	EClass getZone();

	/**
	 * Returns the meta object for the reference '{@link org.supremica.external.sag.Zone#getFront <em>Front</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Front</em>'.
	 * @see org.supremica.external.sag.Zone#getFront()
	 * @see #getZone()
	 * @generated
	 */
	EReference getZone_Front();

	/**
	 * Returns the meta object for the reference '{@link org.supremica.external.sag.Zone#getBack <em>Back</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Back</em>'.
	 * @see org.supremica.external.sag.Zone#getBack()
	 * @see #getZone()
	 * @generated
	 */
	EReference getZone_Back();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#isIsOneway <em>Is Oneway</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Oneway</em>'.
	 * @see org.supremica.external.sag.Zone#isIsOneway()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_IsOneway();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.BoundedZone <em>Bounded Zone</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bounded Zone</em>'.
	 * @see org.supremica.external.sag.BoundedZone
	 * @generated
	 */
	EClass getBoundedZone();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.BoundedZone#getCapacity <em>Capacity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Capacity</em>'.
	 * @see org.supremica.external.sag.BoundedZone#getCapacity()
	 * @see #getBoundedZone()
	 * @generated
	 */
	EAttribute getBoundedZone_Capacity();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.Named <em>Named</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Named</em>'.
	 * @see org.supremica.external.sag.Named
	 * @generated
	 */
	EClass getNamed();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Named#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.supremica.external.sag.Named#getName()
	 * @see #getNamed()
	 * @generated
	 */
	EAttribute getNamed_Name();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.UnboundedZone <em>Unbounded Zone</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Unbounded Zone</em>'.
	 * @see org.supremica.external.sag.UnboundedZone
	 * @generated
	 */
	EClass getUnboundedZone();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.UnboundedZone#isIsOutside <em>Is Outside</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Outside</em>'.
	 * @see org.supremica.external.sag.UnboundedZone#isIsOutside()
	 * @see #getUnboundedZone()
	 * @generated
	 */
	EAttribute getUnboundedZone_IsOutside();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.Node <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Node</em>'.
	 * @see org.supremica.external.sag.Node
	 * @generated
	 */
	EClass getNode();

	/**
	 * Returns the meta object for the reference '{@link org.supremica.external.sag.Node#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Sensor</em>'.
	 * @see org.supremica.external.sag.Node#getSensor()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Sensor();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Node#getSensorName <em>Sensor Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Name</em>'.
	 * @see org.supremica.external.sag.Node#getSensorName()
	 * @see #getNode()
	 * @generated
	 */
	EAttribute getNode_SensorName();

	/**
	 * Returns the meta object for the container reference '{@link org.supremica.external.sag.Node#getGraph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Graph</em>'.
	 * @see org.supremica.external.sag.Node#getGraph()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Graph();

	/**
	 * Returns the meta object for the reference list '{@link org.supremica.external.sag.Node#getIncoming <em>Incoming</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Incoming</em>'.
	 * @see org.supremica.external.sag.Node#getIncoming()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Incoming();

	/**
	 * Returns the meta object for the reference list '{@link org.supremica.external.sag.Node#getOutgoing <em>Outgoing</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Outgoing</em>'.
	 * @see org.supremica.external.sag.Node#getOutgoing()
	 * @see #getNode()
	 * @generated
	 */
	EReference getNode_Outgoing();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.Project <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Project</em>'.
	 * @see org.supremica.external.sag.Project
	 * @generated
	 */
	EClass getProject();

	/**
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Project#getGraph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Graph</em>'.
	 * @see org.supremica.external.sag.Project#getGraph()
	 * @see #getProject()
	 * @generated
	 */
	EReference getProject_Graph();

	/**
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Project#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Sensor</em>'.
	 * @see org.supremica.external.sag.Project#getSensor()
	 * @see #getProject()
	 * @generated
	 */
	EReference getProject_Sensor();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.Sensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor</em>'.
	 * @see org.supremica.external.sag.Sensor
	 * @generated
	 */
	EClass getSensor();

	/**
	 * Returns the meta object for the reference list '{@link org.supremica.external.sag.Sensor#getNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Node</em>'.
	 * @see org.supremica.external.sag.Sensor#getNode()
	 * @see #getSensor()
	 * @generated
	 */
	EReference getSensor_Node();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SagFactory getSagFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.GraphImpl <em>Graph</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.GraphImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getGraph()
		 * @generated
		 */
		EClass GRAPH = eINSTANCE.getGraph();

		/**
		 * The meta object literal for the '<em><b>Zone</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GRAPH__ZONE = eINSTANCE.getGraph_Zone();

		/**
		 * The meta object literal for the '<em><b>Multiple Objects</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRAPH__MULTIPLE_OBJECTS = eINSTANCE.getGraph_MultipleObjects();

		/**
		 * The meta object literal for the '<em><b>Node</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GRAPH__NODE = eINSTANCE.getGraph_Node();

		/**
		 * The meta object literal for the '<em><b>Project</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GRAPH__PROJECT = eINSTANCE.getGraph_Project();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.ZoneImpl <em>Zone</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.ZoneImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getZone()
		 * @generated
		 */
		EClass ZONE = eINSTANCE.getZone();

		/**
		 * The meta object literal for the '<em><b>Front</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ZONE__FRONT = eINSTANCE.getZone_Front();

		/**
		 * The meta object literal for the '<em><b>Back</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ZONE__BACK = eINSTANCE.getZone_Back();

		/**
		 * The meta object literal for the '<em><b>Is Oneway</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__IS_ONEWAY = eINSTANCE.getZone_IsOneway();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.BoundedZoneImpl <em>Bounded Zone</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.BoundedZoneImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getBoundedZone()
		 * @generated
		 */
		EClass BOUNDED_ZONE = eINSTANCE.getBoundedZone();

		/**
		 * The meta object literal for the '<em><b>Capacity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BOUNDED_ZONE__CAPACITY = eINSTANCE.getBoundedZone_Capacity();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.NamedImpl <em>Named</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.NamedImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getNamed()
		 * @generated
		 */
		EClass NAMED = eINSTANCE.getNamed();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NAMED__NAME = eINSTANCE.getNamed_Name();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.UnboundedZoneImpl <em>Unbounded Zone</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.UnboundedZoneImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getUnboundedZone()
		 * @generated
		 */
		EClass UNBOUNDED_ZONE = eINSTANCE.getUnboundedZone();

		/**
		 * The meta object literal for the '<em><b>Is Outside</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UNBOUNDED_ZONE__IS_OUTSIDE = eINSTANCE.getUnboundedZone_IsOutside();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.NodeImpl <em>Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.NodeImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getNode()
		 * @generated
		 */
		EClass NODE = eINSTANCE.getNode();

		/**
		 * The meta object literal for the '<em><b>Sensor</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__SENSOR = eINSTANCE.getNode_Sensor();

		/**
		 * The meta object literal for the '<em><b>Sensor Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NODE__SENSOR_NAME = eINSTANCE.getNode_SensorName();

		/**
		 * The meta object literal for the '<em><b>Graph</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__GRAPH = eINSTANCE.getNode_Graph();

		/**
		 * The meta object literal for the '<em><b>Incoming</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__INCOMING = eINSTANCE.getNode_Incoming();

		/**
		 * The meta object literal for the '<em><b>Outgoing</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference NODE__OUTGOING = eINSTANCE.getNode_Outgoing();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.ProjectImpl <em>Project</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.ProjectImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getProject()
		 * @generated
		 */
		EClass PROJECT = eINSTANCE.getProject();

		/**
		 * The meta object literal for the '<em><b>Graph</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROJECT__GRAPH = eINSTANCE.getProject_Graph();

		/**
		 * The meta object literal for the '<em><b>Sensor</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROJECT__SENSOR = eINSTANCE.getProject_Sensor();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.SensorImpl <em>Sensor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.SensorImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getSensor()
		 * @generated
		 */
		EClass SENSOR = eINSTANCE.getSensor();

		/**
		 * The meta object literal for the '<em><b>Node</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR__NODE = eINSTANCE.getSensor_Node();

	}

} //SagPackage
