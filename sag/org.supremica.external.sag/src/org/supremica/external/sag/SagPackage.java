/**
 * <copyright>
 * </copyright>
 *
 * $Id: SagPackage.java,v 1.8 2007-02-16 16:32:26 torda Exp $
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
	int NAMED = 2;

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
	 * The feature id for the '<em><b>Max Nr Of Objects</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__MAX_NR_OF_OBJECTS = NAMED_FEATURE_COUNT + 1;

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
	 * The feature id for the '<em><b>Nr Of Objects Is Unbounded</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__NR_OF_OBJECTS_IS_UNBOUNDED = NAMED_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Sensor</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH__SENSOR = NAMED_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Graph</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRAPH_FEATURE_COUNT = NAMED_FEATURE_COUNT + 6;

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
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__NAME = NAMED__NAME;

	/**
	 * The feature id for the '<em><b>Front</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__FRONT = NAMED_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Back</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__BACK = NAMED_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Oneway</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__ONEWAY = NAMED_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Graph</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__GRAPH = NAMED_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Capacity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__CAPACITY = NAMED_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Outside System Boundry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__OUTSIDE_SYSTEM_BOUNDRY = NAMED_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Bounded</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__BOUNDED = NAMED_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Forward Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__FORWARD_CONDITION = NAMED_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Backward Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__BACKWARD_CONDITION = NAMED_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Front Entry Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__FRONT_ENTRY_CONDITION = NAMED_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Front Exit Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__FRONT_EXIT_CONDITION = NAMED_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Back Entry Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__BACK_ENTRY_CONDITION = NAMED_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Back Exit Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__BACK_EXIT_CONDITION = NAMED_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Initial Nr Of Objects</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__INITIAL_NR_OF_OBJECTS = NAMED_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Overlapped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE__OVERLAPPED = NAMED_FEATURE_COUNT + 14;

	/**
	 * The number of structural features of the '<em>Zone</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ZONE_FEATURE_COUNT = NAMED_FEATURE_COUNT + 15;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.NodeImpl <em>Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.NodeImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getNode()
	 * @generated
	 */
	int NODE = 3;

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
	 * The number of structural features of the '<em>Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NODE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.ProjectImpl <em>Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.ProjectImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getProject()
	 * @generated
	 */
	int PROJECT = 4;

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
	 * The feature id for the '<em><b>Sensor Signal</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__SENSOR_SIGNAL = NAMED_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Control Signal</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__CONTROL_SIGNAL = NAMED_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT_FEATURE_COUNT = NAMED_FEATURE_COUNT + 3;


	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.SensorSignalImpl <em>Sensor Signal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.SensorSignalImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getSensorSignal()
	 * @generated
	 */
	int SENSOR_SIGNAL = 5;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_SIGNAL__NAME = NAMED__NAME;

	/**
	 * The feature id for the '<em><b>Sensor</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_SIGNAL__SENSOR = NAMED_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Project</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_SIGNAL__PROJECT = NAMED_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Sensor Signal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_SIGNAL_FEATURE_COUNT = NAMED_FEATURE_COUNT + 2;

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
	 * The meta object id for the '{@link org.supremica.external.sag.impl.ControlSignalImpl <em>Control Signal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.ControlSignalImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getControlSignal()
	 * @generated
	 */
	int CONTROL_SIGNAL = 8;

	/**
	 * The meta object id for the '{@link org.supremica.external.sag.impl.EndNodeImpl <em>End Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.impl.EndNodeImpl
	 * @see org.supremica.external.sag.impl.SagPackageImpl#getEndNode()
	 * @generated
	 */
	int END_NODE = 6;

	/**
	 * The feature id for the '<em><b>Graph</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int END_NODE__GRAPH = NODE__GRAPH;

	/**
	 * The feature id for the '<em><b>Incoming</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int END_NODE__INCOMING = NODE__INCOMING;

	/**
	 * The feature id for the '<em><b>Outgoing</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int END_NODE__OUTGOING = NODE__OUTGOING;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int END_NODE__NAME = NODE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>End Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int END_NODE_FEATURE_COUNT = NODE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Graph</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__GRAPH = NODE__GRAPH;

	/**
	 * The feature id for the '<em><b>Incoming</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__INCOMING = NODE__INCOMING;

	/**
	 * The feature id for the '<em><b>Outgoing</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__OUTGOING = NODE__OUTGOING;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__NAME = NODE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Signal</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__SIGNAL = NODE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Initially Activated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR__INITIALLY_ACTIVATED = NODE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Sensor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_FEATURE_COUNT = NODE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTROL_SIGNAL__NAME = NAMED__NAME;

	/**
	 * The number of structural features of the '<em>Control Signal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTROL_SIGNAL_FEATURE_COUNT = NAMED_FEATURE_COUNT + 0;

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
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Graph#getMaxNrOfObjects <em>Max Nr Of Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Nr Of Objects</em>'.
	 * @see org.supremica.external.sag.Graph#getMaxNrOfObjects()
	 * @see #getGraph()
	 * @generated
	 */
	EAttribute getGraph_MaxNrOfObjects();

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
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Graph#isNrOfObjectsIsUnbounded <em>Nr Of Objects Is Unbounded</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nr Of Objects Is Unbounded</em>'.
	 * @see org.supremica.external.sag.Graph#isNrOfObjectsIsUnbounded()
	 * @see #getGraph()
	 * @generated
	 */
	EAttribute getGraph_NrOfObjectsIsUnbounded();

	/**
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Graph#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Sensor</em>'.
	 * @see org.supremica.external.sag.Graph#getSensor()
	 * @see #getGraph()
	 * @generated
	 */
	EReference getGraph_Sensor();

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
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#isOneway <em>Oneway</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Oneway</em>'.
	 * @see org.supremica.external.sag.Zone#isOneway()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_Oneway();

	/**
	 * Returns the meta object for the container reference '{@link org.supremica.external.sag.Zone#getGraph <em>Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Graph</em>'.
	 * @see org.supremica.external.sag.Zone#getGraph()
	 * @see #getZone()
	 * @generated
	 */
	EReference getZone_Graph();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getCapacity <em>Capacity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Capacity</em>'.
	 * @see org.supremica.external.sag.Zone#getCapacity()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_Capacity();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#isOutsideSystemBoundry <em>Outside System Boundry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Outside System Boundry</em>'.
	 * @see org.supremica.external.sag.Zone#isOutsideSystemBoundry()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_OutsideSystemBoundry();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#isBounded <em>Bounded</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bounded</em>'.
	 * @see org.supremica.external.sag.Zone#isBounded()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_Bounded();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getForwardCondition <em>Forward Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Forward Condition</em>'.
	 * @see org.supremica.external.sag.Zone#getForwardCondition()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_ForwardCondition();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getBackwardCondition <em>Backward Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Backward Condition</em>'.
	 * @see org.supremica.external.sag.Zone#getBackwardCondition()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_BackwardCondition();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getFrontEntryCondition <em>Front Entry Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Front Entry Condition</em>'.
	 * @see org.supremica.external.sag.Zone#getFrontEntryCondition()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_FrontEntryCondition();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getFrontExitCondition <em>Front Exit Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Front Exit Condition</em>'.
	 * @see org.supremica.external.sag.Zone#getFrontExitCondition()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_FrontExitCondition();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getBackEntryCondition <em>Back Entry Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Back Entry Condition</em>'.
	 * @see org.supremica.external.sag.Zone#getBackEntryCondition()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_BackEntryCondition();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getBackExitCondition <em>Back Exit Condition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Back Exit Condition</em>'.
	 * @see org.supremica.external.sag.Zone#getBackExitCondition()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_BackExitCondition();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#getInitialNrOfObjects <em>Initial Nr Of Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Initial Nr Of Objects</em>'.
	 * @see org.supremica.external.sag.Zone#getInitialNrOfObjects()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_InitialNrOfObjects();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Zone#isOverlapped <em>Overlapped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Overlapped</em>'.
	 * @see org.supremica.external.sag.Zone#isOverlapped()
	 * @see #getZone()
	 * @generated
	 */
	EAttribute getZone_Overlapped();

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
	 * Returns the meta object for class '{@link org.supremica.external.sag.Node <em>Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Node</em>'.
	 * @see org.supremica.external.sag.Node
	 * @generated
	 */
	EClass getNode();

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
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Project#getSensorSignal <em>Sensor Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Sensor Signal</em>'.
	 * @see org.supremica.external.sag.Project#getSensorSignal()
	 * @see #getProject()
	 * @generated
	 */
	EReference getProject_SensorSignal();

	/**
	 * Returns the meta object for the containment reference list '{@link org.supremica.external.sag.Project#getControlSignal <em>Control Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Control Signal</em>'.
	 * @see org.supremica.external.sag.Project#getControlSignal()
	 * @see #getProject()
	 * @generated
	 */
	EReference getProject_ControlSignal();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.SensorSignal <em>Sensor Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Signal</em>'.
	 * @see org.supremica.external.sag.SensorSignal
	 * @generated
	 */
	EClass getSensorSignal();

	/**
	 * Returns the meta object for the reference list '{@link org.supremica.external.sag.SensorSignal#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Sensor</em>'.
	 * @see org.supremica.external.sag.SensorSignal#getSensor()
	 * @see #getSensorSignal()
	 * @generated
	 */
	EReference getSensorSignal_Sensor();

	/**
	 * Returns the meta object for the container reference '{@link org.supremica.external.sag.SensorSignal#getProject <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Project</em>'.
	 * @see org.supremica.external.sag.SensorSignal#getProject()
	 * @see #getSensorSignal()
	 * @generated
	 */
	EReference getSensorSignal_Project();

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
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Sensor#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.supremica.external.sag.Sensor#getName()
	 * @see #getSensor()
	 * @generated
	 */
	EAttribute getSensor_Name();

	/**
	 * Returns the meta object for the reference '{@link org.supremica.external.sag.Sensor#getSignal <em>Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Signal</em>'.
	 * @see org.supremica.external.sag.Sensor#getSignal()
	 * @see #getSensor()
	 * @generated
	 */
	EReference getSensor_Signal();

	/**
	 * Returns the meta object for the attribute '{@link org.supremica.external.sag.Sensor#isInitiallyActivated <em>Initially Activated</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Initially Activated</em>'.
	 * @see org.supremica.external.sag.Sensor#isInitiallyActivated()
	 * @see #getSensor()
	 * @generated
	 */
	EAttribute getSensor_InitiallyActivated();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.ControlSignal <em>Control Signal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Control Signal</em>'.
	 * @see org.supremica.external.sag.ControlSignal
	 * @generated
	 */
	EClass getControlSignal();

	/**
	 * Returns the meta object for class '{@link org.supremica.external.sag.EndNode <em>End Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>End Node</em>'.
	 * @see org.supremica.external.sag.EndNode
	 * @generated
	 */
	EClass getEndNode();

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
		 * The meta object literal for the '<em><b>Max Nr Of Objects</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRAPH__MAX_NR_OF_OBJECTS = eINSTANCE.getGraph_MaxNrOfObjects();

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
		 * The meta object literal for the '<em><b>Nr Of Objects Is Unbounded</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRAPH__NR_OF_OBJECTS_IS_UNBOUNDED = eINSTANCE.getGraph_NrOfObjectsIsUnbounded();

		/**
		 * The meta object literal for the '<em><b>Sensor</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GRAPH__SENSOR = eINSTANCE.getGraph_Sensor();

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
		 * The meta object literal for the '<em><b>Oneway</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__ONEWAY = eINSTANCE.getZone_Oneway();

		/**
		 * The meta object literal for the '<em><b>Graph</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ZONE__GRAPH = eINSTANCE.getZone_Graph();

		/**
		 * The meta object literal for the '<em><b>Capacity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__CAPACITY = eINSTANCE.getZone_Capacity();

		/**
		 * The meta object literal for the '<em><b>Outside System Boundry</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__OUTSIDE_SYSTEM_BOUNDRY = eINSTANCE.getZone_OutsideSystemBoundry();

		/**
		 * The meta object literal for the '<em><b>Bounded</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__BOUNDED = eINSTANCE.getZone_Bounded();

		/**
		 * The meta object literal for the '<em><b>Forward Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__FORWARD_CONDITION = eINSTANCE.getZone_ForwardCondition();

		/**
		 * The meta object literal for the '<em><b>Backward Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__BACKWARD_CONDITION = eINSTANCE.getZone_BackwardCondition();

		/**
		 * The meta object literal for the '<em><b>Front Entry Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__FRONT_ENTRY_CONDITION = eINSTANCE.getZone_FrontEntryCondition();

		/**
		 * The meta object literal for the '<em><b>Front Exit Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__FRONT_EXIT_CONDITION = eINSTANCE.getZone_FrontExitCondition();

		/**
		 * The meta object literal for the '<em><b>Back Entry Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__BACK_ENTRY_CONDITION = eINSTANCE.getZone_BackEntryCondition();

		/**
		 * The meta object literal for the '<em><b>Back Exit Condition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__BACK_EXIT_CONDITION = eINSTANCE.getZone_BackExitCondition();

		/**
		 * The meta object literal for the '<em><b>Initial Nr Of Objects</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__INITIAL_NR_OF_OBJECTS = eINSTANCE.getZone_InitialNrOfObjects();

		/**
		 * The meta object literal for the '<em><b>Overlapped</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ZONE__OVERLAPPED = eINSTANCE.getZone_Overlapped();

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
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.NodeImpl <em>Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.NodeImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getNode()
		 * @generated
		 */
		EClass NODE = eINSTANCE.getNode();

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
		 * The meta object literal for the '<em><b>Sensor Signal</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROJECT__SENSOR_SIGNAL = eINSTANCE.getProject_SensorSignal();

		/**
		 * The meta object literal for the '<em><b>Control Signal</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROJECT__CONTROL_SIGNAL = eINSTANCE.getProject_ControlSignal();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.SensorSignalImpl <em>Sensor Signal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.SensorSignalImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getSensorSignal()
		 * @generated
		 */
		EClass SENSOR_SIGNAL = eINSTANCE.getSensorSignal();

		/**
		 * The meta object literal for the '<em><b>Sensor</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR_SIGNAL__SENSOR = eINSTANCE.getSensorSignal_Sensor();

		/**
		 * The meta object literal for the '<em><b>Project</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR_SIGNAL__PROJECT = eINSTANCE.getSensorSignal_Project();

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
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR__NAME = eINSTANCE.getSensor_Name();

		/**
		 * The meta object literal for the '<em><b>Signal</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR__SIGNAL = eINSTANCE.getSensor_Signal();

		/**
		 * The meta object literal for the '<em><b>Initially Activated</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR__INITIALLY_ACTIVATED = eINSTANCE.getSensor_InitiallyActivated();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.ControlSignalImpl <em>Control Signal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.ControlSignalImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getControlSignal()
		 * @generated
		 */
		EClass CONTROL_SIGNAL = eINSTANCE.getControlSignal();

		/**
		 * The meta object literal for the '{@link org.supremica.external.sag.impl.EndNodeImpl <em>End Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.supremica.external.sag.impl.EndNodeImpl
		 * @see org.supremica.external.sag.impl.SagPackageImpl#getEndNode()
		 * @generated
		 */
		EClass END_NODE = eINSTANCE.getEndNode();

	}

} //SagPackage
