/**
 * <copyright>
 * </copyright>
 *
 * $Id: UnboundedZoneTest.java,v 1.1 2006-12-18 15:26:16 torda Exp $
 */
package org.supremica.external.sag.tests;

import junit.textui.TestRunner;

import org.supremica.external.sag.SagFactory;
import org.supremica.external.sag.UnboundedZone;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Unbounded Zone</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class UnboundedZoneTest extends ZoneTest {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(UnboundedZoneTest.class);
	}

	/**
	 * Constructs a new Unbounded Zone test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UnboundedZoneTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Unbounded Zone test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private UnboundedZone getFixture() {
		return (UnboundedZone)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	protected void setUp() throws Exception {
		setFixture(SagFactory.eINSTANCE.createUnboundedZone());
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

} //UnboundedZoneTest
