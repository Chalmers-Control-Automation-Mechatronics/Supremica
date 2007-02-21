/**
 * <copyright>
 * </copyright>
 *
 * $Id: Zone.java,v 1.7 2007-02-21 08:38:53 torda Exp $
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
 *   <li>{@link org.supremica.external.sag.Zone#getForwardCondition <em>Forward Condition</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getBackwardCondition <em>Backward Condition</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getFrontEntryCondition <em>Front Entry Condition</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getFrontExitCondition <em>Front Exit Condition</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getBackEntryCondition <em>Back Entry Condition</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getBackExitCondition <em>Back Exit Condition</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getInitialNrOfObjects <em>Initial Nr Of Objects</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#isOverlapped <em>Overlapped</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#isOrdered <em>Ordered</em>}</li>
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
	 * @see #setCapacity(int)
	 * @see org.supremica.external.sag.SagPackage#getZone_Capacity()
	 * @model default="-1"
	 * @generated
	 */
	int getCapacity();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getCapacity <em>Capacity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Capacity</em>' attribute.
	 * @see #getCapacity()
	 * @generated
	 */
	void setCapacity(int value);

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
	 * Returns the value of the '<em><b>Forward Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Forward Condition</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Forward Condition</em>' attribute.
	 * @see #setForwardCondition(String)
	 * @see org.supremica.external.sag.SagPackage#getZone_ForwardCondition()
	 * @model
	 * @generated
	 */
	String getForwardCondition();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getForwardCondition <em>Forward Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Forward Condition</em>' attribute.
	 * @see #getForwardCondition()
	 * @generated
	 */
	void setForwardCondition(String value);

	/**
	 * Returns the value of the '<em><b>Backward Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Backward Condition</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Backward Condition</em>' attribute.
	 * @see #setBackwardCondition(String)
	 * @see org.supremica.external.sag.SagPackage#getZone_BackwardCondition()
	 * @model
	 * @generated
	 */
	String getBackwardCondition();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getBackwardCondition <em>Backward Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Backward Condition</em>' attribute.
	 * @see #getBackwardCondition()
	 * @generated
	 */
	void setBackwardCondition(String value);

	/**
	 * Returns the value of the '<em><b>Front Entry Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Front Entry Condition</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Front Entry Condition</em>' attribute.
	 * @see #setFrontEntryCondition(String)
	 * @see org.supremica.external.sag.SagPackage#getZone_FrontEntryCondition()
	 * @model
	 * @generated
	 */
	String getFrontEntryCondition();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getFrontEntryCondition <em>Front Entry Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Front Entry Condition</em>' attribute.
	 * @see #getFrontEntryCondition()
	 * @generated
	 */
	void setFrontEntryCondition(String value);

	/**
	 * Returns the value of the '<em><b>Front Exit Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Front Exit Condition</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Front Exit Condition</em>' attribute.
	 * @see #setFrontExitCondition(String)
	 * @see org.supremica.external.sag.SagPackage#getZone_FrontExitCondition()
	 * @model
	 * @generated
	 */
	String getFrontExitCondition();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getFrontExitCondition <em>Front Exit Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Front Exit Condition</em>' attribute.
	 * @see #getFrontExitCondition()
	 * @generated
	 */
	void setFrontExitCondition(String value);

	/**
	 * Returns the value of the '<em><b>Back Entry Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Back Entry Condition</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Back Entry Condition</em>' attribute.
	 * @see #setBackEntryCondition(String)
	 * @see org.supremica.external.sag.SagPackage#getZone_BackEntryCondition()
	 * @model
	 * @generated
	 */
	String getBackEntryCondition();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getBackEntryCondition <em>Back Entry Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Back Entry Condition</em>' attribute.
	 * @see #getBackEntryCondition()
	 * @generated
	 */
	void setBackEntryCondition(String value);

	/**
	 * Returns the value of the '<em><b>Back Exit Condition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Back Exit Condition</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Back Exit Condition</em>' attribute.
	 * @see #setBackExitCondition(String)
	 * @see org.supremica.external.sag.SagPackage#getZone_BackExitCondition()
	 * @model
	 * @generated
	 */
	String getBackExitCondition();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getBackExitCondition <em>Back Exit Condition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Back Exit Condition</em>' attribute.
	 * @see #getBackExitCondition()
	 * @generated
	 */
	void setBackExitCondition(String value);

	/**
	 * Returns the value of the '<em><b>Initial Nr Of Objects</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Initial Nr Of Objects</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Initial Nr Of Objects</em>' attribute.
	 * @see #setInitialNrOfObjects(int)
	 * @see org.supremica.external.sag.SagPackage#getZone_InitialNrOfObjects()
	 * @model
	 * @generated
	 */
	int getInitialNrOfObjects();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#getInitialNrOfObjects <em>Initial Nr Of Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Initial Nr Of Objects</em>' attribute.
	 * @see #getInitialNrOfObjects()
	 * @generated
	 */
	void setInitialNrOfObjects(int value);

	/**
	 * Returns the value of the '<em><b>Overlapped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Overlapped</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Overlapped</em>' attribute.
	 * @see #setOverlapped(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_Overlapped()
	 * @model
	 * @generated
	 */
	boolean isOverlapped();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#isOverlapped <em>Overlapped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Overlapped</em>' attribute.
	 * @see #isOverlapped()
	 * @generated
	 */
	void setOverlapped(boolean value);

	/**
	 * Returns the value of the '<em><b>Ordered</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ordered</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ordered</em>' attribute.
	 * @see #setOrdered(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_Ordered()
	 * @model default="true"
	 * @generated
	 */
	boolean isOrdered();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#isOrdered <em>Ordered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ordered</em>' attribute.
	 * @see #isOrdered()
	 * @generated
	 */
	void setOrdered(boolean value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model annotation="http://www.eclipse.org/OCL/examples/ocl invariant='bounded implies capacity > 0'"
	 * @generated
	 */
	boolean validateCapacityIsPositiveNumber(DiagnosticChain diagnostics, Map<?, ?> context);

} // Zone
