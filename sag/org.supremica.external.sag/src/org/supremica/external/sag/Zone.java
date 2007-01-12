/**
 * <copyright>
 * </copyright>
 *
 * $Id: Zone.java,v 1.3 2007-01-12 14:23:46 torda Exp $
 */
package org.supremica.external.sag;

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
 *   <li>{@link org.supremica.external.sag.Zone#isIsOneway <em>Is Oneway</em>}</li>
 *   <li>{@link org.supremica.external.sag.Zone#getGraph <em>Graph</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getZone()
 * @model abstract="true"
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
	 * Returns the value of the '<em><b>Is Oneway</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Oneway</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Oneway</em>' attribute.
	 * @see #setIsOneway(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_IsOneway()
	 * @model default="true"
	 * @generated
	 */
	boolean isIsOneway();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.Zone#isIsOneway <em>Is Oneway</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Oneway</em>' attribute.
	 * @see #isIsOneway()
	 * @generated
	 */
	void setIsOneway(boolean value);

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

} // Zone
