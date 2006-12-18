/**
 * <copyright>
 * </copyright>
 *
 * $Id: BoundedZone.java,v 1.1 2006-12-18 15:23:00 torda Exp $
 */
package org.supremica.external.sag;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bounded Zone</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.BoundedZone#getCapacity <em>Capacity</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getBoundedZone()
 * @model
 * @generated
 */
public interface BoundedZone extends Zone {
	/**
	 * Returns the value of the '<em><b>Capacity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Capacity</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Capacity</em>' attribute.
	 * @see #setCapacity(int)
	 * @see org.supremica.external.sag.SagPackage#getBoundedZone_Capacity()
	 * @model
	 * @generated
	 */
	int getCapacity();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.BoundedZone#getCapacity <em>Capacity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Capacity</em>' attribute.
	 * @see #getCapacity()
	 * @generated
	 */
	void setCapacity(int value);

} // BoundedZone
