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
 * A test suite for the '<em><b>sag</b></em>' package.
 * <!-- end-user-doc -->
 * @generated
 */
public class SagTests extends TestSuite {
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
		TestSuite suite = new SagTests("sag Tests");
		suite.addTestSuite(GraphTest.class);
		suite.addTestSuite(ZoneTest.class);
		suite.addTestSuite(ProjectTest.class);
		suite.addTestSuite(SensorSignalTest.class);
		suite.addTestSuite(SensorTest.class);
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SagTests(String name) {
		super(name);
	}

} //SagTests
