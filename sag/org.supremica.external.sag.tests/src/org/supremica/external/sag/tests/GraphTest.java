/**
 * <copyright>
 * </copyright>
 *
 * $Id: GraphTest.java,v 1.1 2006-12-18 15:26:16 torda Exp $
 */
package org.supremica.external.sag.tests;

import junit.textui.TestRunner;

import org.supremica.external.sag.Graph;
import org.supremica.external.sag.SagFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Graph</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class GraphTest extends NamedTest {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(GraphTest.class);
	}

	/**
	 * Constructs a new Graph test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GraphTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Graph test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private Graph getFixture() {
		return (Graph)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	protected void setUp() throws Exception {
		setFixture(SagFactory.eINSTANCE.createGraph());
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

} //GraphTest
