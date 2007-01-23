/**
 * <copyright>
 * </copyright>
 *
 * $Id: SensorTest.java,v 1.2 2007-01-23 16:08:52 torda Exp $
 */
package org.supremica.external.sag.tests;

import junit.textui.TestRunner;

import org.supremica.external.sag.SagFactory;
import org.supremica.external.sag.Sensor;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Sensor</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following operations are tested:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Sensor#validateName(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map) <em>Validate Name</em>}</li>
 *   <li>{@link org.supremica.external.sag.Sensor#validateUniquenessOfName(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map) <em>Validate Uniqueness Of Name</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class SensorTest extends NamedTest {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(SensorTest.class);
	}

	/**
	 * Constructs a new Sensor test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensorTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Sensor test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private Sensor getFixture() {
		return (Sensor)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	protected void setUp() throws Exception {
		setFixture(SagFactory.eINSTANCE.createSensor());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	protected void tearDown() throws Exception {
		setFixture(null);
	}

	/**
	 * Tests the '{@link org.supremica.external.sag.Sensor#validateName(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map) <em>Validate Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.Sensor#validateName(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map)
	 * @generated
	 */
	public void testValidateName__DiagnosticChain_Map() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

	/**
	 * Tests the '{@link org.supremica.external.sag.Sensor#validateUniquenessOfName(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map) <em>Validate Uniqueness Of Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.Sensor#validateUniquenessOfName(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map)
	 * @generated
	 */
	public void testValidateUniquenessOfName__DiagnosticChain_Map() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

} //SensorTest
