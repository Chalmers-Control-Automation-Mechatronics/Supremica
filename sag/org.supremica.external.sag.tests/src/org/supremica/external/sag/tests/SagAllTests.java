/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test suite for the '<em><b>Sag</b></em>' model.
 * <!-- end-user-doc -->
 * @generated
 */
public class SagAllTests extends TestSuite {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static Test suite() {
		TestSuite suite = new SagAllTests("Sag Tests");
		suite.addTest(SagTests.suite());
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SagAllTests(String name) {
		super(name);
	}

} //SagAllTests
