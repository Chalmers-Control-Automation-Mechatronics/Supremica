/**
 * <copyright>
 * </copyright>
 *
 * $Id: BoundedZoneTest.java,v 1.1 2006-12-18 15:26:16 torda Exp $
 */
package org.supremica.external.sag.tests;

import junit.textui.TestRunner;

import org.supremica.external.sag.BoundedZone;
import org.supremica.external.sag.SagFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Bounded Zone</b></em>'.
 * <!-- end-user-doc -->
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

} //BoundedZoneTest
