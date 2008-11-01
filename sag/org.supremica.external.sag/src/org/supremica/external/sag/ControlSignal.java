
/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Control Signal</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.ControlSignal#isSynthesize <em>Synthesize</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getControlSignal()
 * @model
 * @generated
 */
public interface ControlSignal extends Named {

	/**
	 * Returns the value of the '<em><b>Synthesize</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Synthesize</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Synthesize</em>' attribute.
	 * @see #setSynthesize(boolean)
	 * @see org.supremica.external.sag.SagPackage#getControlSignal_Synthesize()
	 * @model default="true"
	 * @generated
	 */
	boolean isSynthesize();

	/**
	 * Sets the value of the '{@link org.supremica.external.sag.ControlSignal#isSynthesize <em>Synthesize</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Synthesize</em>' attribute.
	 * @see #isSynthesize()
	 * @generated
	 */
	void setSynthesize(boolean value);
} // ControlSignal
