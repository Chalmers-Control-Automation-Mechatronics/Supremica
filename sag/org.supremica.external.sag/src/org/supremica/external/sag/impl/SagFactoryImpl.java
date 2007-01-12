/**
 * <copyright>
 * </copyright>
 *
 * $Id: SagFactoryImpl.java,v 1.3 2007-01-12 14:23:10 torda Exp $
 */
package org.supremica.external.sag.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.supremica.external.sag.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SagFactoryImpl extends EFactoryImpl implements SagFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SagFactory init() {
		try {
			SagFactory theSagFactory = (SagFactory)EPackage.Registry.INSTANCE.getEFactory("http://supremica.org/external/sag"); 
			if (theSagFactory != null) {
				return theSagFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SagFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SagFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case SagPackage.GRAPH: return createGraph();
			case SagPackage.BOUNDED_ZONE: return createBoundedZone();
			case SagPackage.UNBOUNDED_ZONE: return createUnboundedZone();
			case SagPackage.PROJECT: return createProject();
			case SagPackage.SENSOR: return createSensor();
			case SagPackage.END_NODE: return createEndNode();
			case SagPackage.SENSOR_NODE: return createSensorNode();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Graph createGraph() {
		GraphImpl graph = new GraphImpl();
		return graph;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BoundedZone createBoundedZone() {
		BoundedZoneImpl boundedZone = new BoundedZoneImpl();
		return boundedZone;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UnboundedZone createUnboundedZone() {
		UnboundedZoneImpl unboundedZone = new UnboundedZoneImpl();
		return unboundedZone;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Project createProject() {
		ProjectImpl project = new ProjectImpl();
		return project;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Sensor createSensor() {
		SensorImpl sensor = new SensorImpl();
		return sensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EndNode createEndNode() {
		EndNodeImpl endNode = new EndNodeImpl();
		return endNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensorNode createSensorNode() {
		SensorNodeImpl sensorNode = new SensorNodeImpl();
		return sensorNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SagPackage getSagPackage() {
		return (SagPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SagPackage getPackage() {
		return SagPackage.eINSTANCE;
	}

} //SagFactoryImpl
