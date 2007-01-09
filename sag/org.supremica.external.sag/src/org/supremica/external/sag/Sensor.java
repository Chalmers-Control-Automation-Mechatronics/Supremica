/**
 * <copyright>
 * </copyright>
 *
 * $Id: Sensor.java,v 1.1 2007-01-09 15:31:07 torda Exp $
 */
package org.supremica.external.sag;

import org.eclipse.emf.common.util.EList;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.supremica.external.sag.Sensor#getNode <em>Node</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.supremica.external.sag.SagPackage#getSensor()
 * @model
 * @generated
 */
public interface Sensor extends Named {
	/**
	 * Returns the value of the '<em><b>Node</b></em>' reference list.
	 * The list contents are of type {@link org.supremica.external.sag.Node}.
	 * It is bidirectional and its opposite is '{@link org.supremica.external.sag.Node#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Node</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node</em>' reference list.
	 * @see org.supremica.external.sag.SagPackage#getSensor_Node()
	 * @see org.supremica.external.sag.Node#getSensor
	 * @model type="org.supremica.external.sag.Node" opposite="sensor" transient="true"
	 * @generated
	 */
	EList<Node> getNode();

} // Sensor
