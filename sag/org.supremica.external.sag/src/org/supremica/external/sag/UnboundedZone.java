/**
 * <copyright>
 * </copyright>
 *
 * $Id: UnboundedZone.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Unbounded Zone</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.UnboundedZone#isIsOutside <em>Is Outside</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getUnboundedZone()
 * @model
 * @generated
 */
public interface UnboundedZone extends Zone {
	/**
	 * Returns the value of the '<em><b>Is Outside</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Outside</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Outside</em>' attribute.
	 * @see #setIsOutside(boolean)
	 * @see org.supremica.external.sag.SagPackage#getUnboundedZone_IsOutside()
	 * @model
	 * @generated
	 */
	boolean isIsOutside();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.UnboundedZone#isIsOutside <em>Is Outside</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Outside</em>' attribute.
	 * @see #isIsOutside()
	 * @generated
	 */
	void setIsOutside(boolean value);

} // UnboundedZone
