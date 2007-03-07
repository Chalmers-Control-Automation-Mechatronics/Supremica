/**
 * <copyright>
 * </copyright>
 *
 * $Id: ControlSignalTest.java,v 1.1 2007-03-07 10:29:34 torda Exp $
 */
package org.supremica.external.sag.tests;

import junit.textui.TestRunner;

import org.supremica.external.sag.ControlSignal;
import org.supremica.external.sag.SagFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Control Signal</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ControlSignalTest extends NamedTest {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ControlSignalTest.class);
	}

	/**
	 * Constructs a new Control Signal test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ControlSignalTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Control Signal test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ControlSignal getFixture() {
		return (ControlSignal)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	protected void setUp() throws Exception {
		setFixture(SagFactory.eINSTANCE.createControlSignal());
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

} //ControlSignalTest
