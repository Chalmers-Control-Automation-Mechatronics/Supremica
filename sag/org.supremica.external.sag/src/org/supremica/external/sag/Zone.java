/**
 * <copyright>
 * </copyright>
 *
 * $Id: Zone.java,v 1.4 2007-02-08 16:36:08 torda Exp $
 */
package org.supremica.external.sag;

import java.util.Map;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Zone</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Zone#getFront <em>Front</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getBack <em>Back</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#isOneway <em>Oneway</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getGraph <em>Graph</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getCapacity <em>Capacity</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#isOutsideSystemBoundry <em>Outside System Boundry</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#isBounded <em>Bounded</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getZone()
 * @model
 * @generated
 */
public interface Zone extends Named {
	/**
	 * Returns the value of the '<em><b>Front</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Node#getIncoming <em>Incoming</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Front</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Front</em>' reference.
	 * @see #setFront(Node)
	 * @see org.supremica.external.sag.SagPackage#getZone_Front()
	 * @see org.supremica.external.sag.Node#getIncoming
	 * @model opposite="incoming"
	 * @generated
	 */
	Node getFront();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getFront <em>Front</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Front</em>' reference.
	 * @see #getFront()
	 * @generated
	 */
	void setFront(Node value);

	/**
	 * Returns the value of the '<em><b>Back</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Node#getOutgoing <em>Outgoing</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Back</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Back</em>' reference.
	 * @see #setBack(Node)
	 * @see org.supremica.external.sag.SagPackage#getZone_Back()
	 * @see org.supremica.external.sag.Node#getOutgoing
	 * @model opposite="outgoing"
	 * @generated
	 */
	Node getBack();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getBack <em>Back</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Back</em>' reference.
	 * @see #getBack()
	 * @generated
	 */
	void setBack(Node value);

	/**
	 * Returns the value of the '<em><b>Oneway</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Oneway</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Oneway</em>' attribute.
	 * @see #setOneway(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_Oneway()
	 * @model default="true"
	 * @generated
	 */
	boolean isOneway();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#isOneway <em>Oneway</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Oneway</em>' attribute.
	 * @see #isOneway()
	 * @generated
	 */
	void setOneway(boolean value);

	/**
	 * Returns the value of the '<em><b>Graph</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Graph#getZone <em>Zone</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Graph</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Graph</em>' container reference.
	 * @see #setGraph(Graph)
	 * @see org.supremica.external.sag.SagPackage#getZone_Graph()
	 * @see org.supremica.external.sag.Graph#getZone
	 * @model opposite="zone" required="true"
	 * @generated
	 */
	Graph getGraph();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getGraph <em>Graph</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Graph</em>' container reference.
	 * @see #getGraph()
	 * @generated
	 */
	void setGraph(Graph value);

	/**
	 * Returns the value of the '<em><b>Capacity</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Capacity</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Capacity</em>' attribute.
	 * @see #isSetCapacity()
	 * @see #unsetCapacity()
	 * @see #setCapacity(int)
	 * @see org.supremica.external.sag.SagPackage#getZone_Capacity()
	 * @model default="-1" unsettable="true"
	 * @generated
	 */
	int getCapacity();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getCapacity <em>Capacity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Capacity</em>' attribute.
	 * @see #isSetCapacity()
	 * @see #unsetCapacity()
	 * @see #getCapacity()
	 * @generated
	 */
	void setCapacity(int value);

	/**
	 * Unsets the value of the '{@link org.supremica.external.sag.Zone#getCapacity <em>Capacity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetCapacity()
	 * @see #getCapacity()
	 * @see #setCapacity(int)
	 * @generated
	 */
	void unsetCapacity();

	/**
	 * Returns whether the value of the '{@link org.supremica.external.sag.Zone#getCapacity <em>Capacity</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Capacity</em>' attribute is set.
	 * @see #unsetCapacity()
	 * @see #getCapacity()
	 * @see #setCapacity(int)
	 * @generated
	 */
	boolean isSetCapacity();

	/**
	 * Returns the value of the '<em><b>Outside System Boundry</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outside System Boundry</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outside System Boundry</em>' attribute.
	 * @see #setOutsideSystemBoundry(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_OutsideSystemBoundry()
	 * @model default="false"
	 * @generated
	 */
	boolean isOutsideSystemBoundry();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#isOutsideSystemBoundry <em>Outside System Boundry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Outside System Boundry</em>' attribute.
	 * @see #isOutsideSystemBoundry()
	 * @generated
	 */
	void setOutsideSystemBoundry(boolean value);

	/**
	 * Returns the value of the '<em><b>Bounded</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Bounded</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bounded</em>' attribute.
	 * @see #setBounded(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_Bounded()
	 * @model default="false" transient="true" volatile="true" derived="true"
	 * @generated
	 */
	boolean isBounded();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#isBounded <em>Bounded</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bounded</em>' attribute.
	 * @see #isBounded()
	 * @generated
	 */
	void setBounded(boolean value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='bounded implies capacity > 0'"
	 * @generated
	 */
	boolean validateCapacityIsPositiveNumber(DiagnosticChain diagnostics, Map<?, ?> context);

} // Zone
