/**
 * <copyright>
 * </copyright>
 *
 * $Id: Zone.java,v 1.2 2007-01-05 13:29:13 torda Exp $
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
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getZone()
 * @model abstract="true"
 * @generated
 */
public interface Zone extends EObject {
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
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Oneway</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Oneway</em>' attribute.
	 * @see #setIsOneway(boolean)
	 * @see org.supremica.external.sag.SagPackage#getZone_IsOneway()
	 * @model
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

} // Zone
