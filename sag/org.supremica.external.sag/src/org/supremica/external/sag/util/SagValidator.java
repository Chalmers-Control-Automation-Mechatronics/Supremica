/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag.util;

import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import org.supremica.external.sag.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see org.supremica.external.sag.SagPackage
 * @generated
 */
public class SagValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final SagValidator INSTANCE = new SagValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "org.supremica.external.sag";

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate All Unbounded Zones Are Outside If Nr Of Objects Are Unbounded' of 'Graph'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int GRAPH__VALIDATE_ALL_UNBOUNDED_ZONES_ARE_OUTSIDE_IF_NR_OF_OBJECTS_ARE_UNBOUNDED = 1;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Name' of 'Graph'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int GRAPH__VALIDATE_NAME = 2;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Capacity Is Positive Number' of 'Zone'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int ZONE__VALIDATE_CAPACITY_IS_POSITIVE_NUMBER = 3;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Name' of 'Project'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int PROJECT__VALIDATE_NAME = 4;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Name' of 'Sensor Signal'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int SENSOR_SIGNAL__VALIDATE_NAME = 5;

	/**
	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Uniqueness Of Name' of 'Sensor Signal'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final int SENSOR_SIGNAL__VALIDATE_UNIQUENESS_OF_NAME = 6;

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 6;

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SagValidator() {
		super();
	}

	/**
	 * Returns the package of this validator switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EPackage getEPackage() {
	  return SagPackage.eINSTANCE;
	}

	/**
	 * Calls <code>validateXXX</code> for the corresonding classifier of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
		switch (classifierID) {
			case SagPackage.GRAPH:
				return validateGraph((Graph)value, diagnostics, context);
			case SagPackage.ZONE:
				return validateZone((Zone)value, diagnostics, context);
			case SagPackage.NAMED:
				return validateNamed((Named)value, diagnostics, context);
			case SagPackage.NODE:
				return validateNode((Node)value, diagnostics, context);
			case SagPackage.PROJECT:
				return validateProject((Project)value, diagnostics, context);
			case SagPackage.SENSOR_SIGNAL:
				return validateSensorSignal((SensorSignal)value, diagnostics, context);
			case SagPackage.END_NODE:
				return validateEndNode((EndNode)value, diagnostics, context);
			case SagPackage.SENSOR:
				return validateSensor((Sensor)value, diagnostics, context);
			case SagPackage.CONTROL_SIGNAL:
				return validateControlSignal((ControlSignal)value, diagnostics, context);
			default: 
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateGraph(Graph graph, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validate_EveryMultiplicityConforms(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validateGraph_validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded(graph, diagnostics, context);
		if (result || diagnostics != null) result &= validateGraph_validateName(graph, diagnostics, context);
		return result;
	}

	/**
	 * Validates the validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded constraint of '<em>Graph</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateGraph_validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded(Graph graph, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return graph.validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded(diagnostics, context);
	}

	/**
	 * Validates the validateName constraint of '<em>Graph</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateGraph_validateName(Graph graph, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return graph.validateName(diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateZone(Zone zone, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validate_EveryMultiplicityConforms(zone, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(zone, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(zone, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(zone, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(zone, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(zone, diagnostics, context);
		if (result || diagnostics != null) result &= validateZone_validateCapacityIsPositiveNumber(zone, diagnostics, context);
		return result;
	}

	/**
	 * Validates the validateCapacityIsPositiveNumber constraint of '<em>Zone</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateZone_validateCapacityIsPositiveNumber(Zone zone, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return zone.validateCapacityIsPositiveNumber(diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateNamed(Named named, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(named, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateNode(Node node, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(node, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateProject(Project project, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validate_EveryMultiplicityConforms(project, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(project, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(project, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(project, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(project, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(project, diagnostics, context);
		if (result || diagnostics != null) result &= validateProject_validateName(project, diagnostics, context);
		return result;
	}

	/**
	 * Validates the validateName constraint of '<em>Project</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateProject_validateName(Project project, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return project.validateName(diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateSensorSignal(SensorSignal sensorSignal, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validate_EveryMultiplicityConforms(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validateSensorSignal_validateName(sensorSignal, diagnostics, context);
		if (result || diagnostics != null) result &= validateSensorSignal_validateUniquenessOfName(sensorSignal, diagnostics, context);
		return result;
	}

	/**
	 * Validates the validateName constraint of '<em>Sensor Signal</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateSensorSignal_validateName(SensorSignal sensorSignal, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return sensorSignal.validateName(diagnostics, context);
	}

	/**
	 * Validates the validateUniquenessOfName constraint of '<em>Sensor Signal</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateSensorSignal_validateUniquenessOfName(SensorSignal sensorSignal, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return sensorSignal.validateUniquenessOfName(diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateSensor(Sensor sensor, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(sensor, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateControlSignal(ControlSignal controlSignal, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(controlSignal, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateEndNode(EndNode endNode, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(endNode, diagnostics, context);
	}

} //SagValidator
