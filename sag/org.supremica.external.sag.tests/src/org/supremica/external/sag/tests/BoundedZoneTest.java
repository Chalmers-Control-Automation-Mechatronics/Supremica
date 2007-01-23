/**
 * <copyright>
 * </copyright>
 *
 * $Id: BoundedZoneTest.java,v 1.2 2007-01-23 16:08:52 torda Exp $
 */
package org.supremica.external.sag.tests;

import junit.textui.TestRunner;

import org.supremica.external.sag.BoundedZone;
import org.supremica.external.sag.SagFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Bounded Zone</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following operations are tested:
 * <ul>
 *   <li>{@link org.supremica.external.sag.BoundedZone#validateCapacityIsPositiveNumber(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map) <em>Validate Capacity Is Positive Number</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class BoundedZoneTest extends ZoneTest {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(BoundedZoneTest.class);
	}

	/**
	 * Constructs a new Bounded Zone test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BoundedZoneTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Bounded Zone test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private BoundedZone getFixture() {
		return (BoundedZone)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	protected void setUp() throws Exception {
		setFixture(SagFactory.eINSTANCE.createBoundedZone());
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
	 * Tests the '{@link org.supremica.external.sag.BoundedZone#validateCapacityIsPositiveNumber(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map) <em>Validate Capacity Is Positive Number</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.supremica.external.sag.BoundedZone#validateCapacityIsPositiveNumber(org.eclipse.emf.common.util.DiagnosticChain, java.util.Map)
	 * @generated
	 */
	public void testValidateCapacityIsPositiveNumber__DiagnosticChain_Map() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

} //BoundedZoneTest
