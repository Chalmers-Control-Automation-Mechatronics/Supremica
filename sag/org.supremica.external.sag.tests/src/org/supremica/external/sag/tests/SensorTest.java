/**
 * <copyright>
 * </copyright>
 *
 * $Id: SensorTest.java,v 1.3 2007-03-07 10:29:34 torda Exp $
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
 * The following features are tested:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Sensor#getName() <em>Name</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class SensorTest extends NodeTest {
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
	 * Tests the '{@link org.supremica.external.sag.Sensor#getName() <em>Name</em>}' feature getter.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.Sensor#getName()
	 * @generated
	 */
	public void testGetName() {
		// TODO: implement this feature getter test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

	/**
	 * Tests the '{@link org.supremica.external.sag.Sensor#setName(java.lang.String) <em>Name</em>}' feature setter.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.Sensor#setName(java.lang.String)
	 * @generated
	 */
	public void testSetName() {
		// TODO: implement this feature setter test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

} //SensorTest
