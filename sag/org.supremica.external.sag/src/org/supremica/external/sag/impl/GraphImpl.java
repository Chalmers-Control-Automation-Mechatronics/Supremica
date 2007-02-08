/**
 * <copyright>
 * </copyright>
 *
 * $Id: GraphImpl.java,v 1.5 2007-02-08 16:36:08 torda Exp $
 */
package org.supremica.external.sag.impl;

import java.util.Collection;

import java.util.Map;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EcoreEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.emf.ocl.expressions.OCLExpression;
import org.eclipse.emf.ocl.expressions.util.EvalEnvironment;
import org.eclipse.emf.ocl.expressions.util.ExpressionsUtil;
import org.eclipse.emf.ocl.parser.Environment;
import org.eclipse.emf.ocl.parser.ParserException;
import org.eclipse.emf.ocl.query.Query;
import org.eclipse.emf.ocl.query.QueryFactory;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.Project;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.SensorNode;
import org.supremica.external.sag.Zone;
import org.supremica.external.sag.util.SagValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Graph</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getZone <em>Zone</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getMaxNrOfObjects <em>Max Nr Of Objects</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getNode <em>Node</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getProject <em>Project</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#isNrOfObjectsIsUnbounded <em>Nr Of Objects Is Unbounded</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getSensor <em>Sensor</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GraphImpl extends NamedImpl implements Graph {
	/**
	 * The cached value of the '{@link #getZone() <em>Zone</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZone()
	 * @generated
	 * @ordered
	 */
	protected EList<Zone> zone = null;

	/**
	 * The default value of the '{@link #getMaxNrOfObjects() <em>Max Nr Of Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxNrOfObjects()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_NR_OF_OBJECTS_EDEFAULT = -1;

	/**
	 * The cached value of the '{@link #getMaxNrOfObjects() <em>Max Nr Of Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxNrOfObjects()
	 * @generated
	 * @ordered
	 */
	protected int maxNrOfObjects = MAX_NR_OF_OBJECTS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getNode() <em>Node</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNode()
	 * @generated
	 * @ordered
	 */
	protected EList<Node> node = null;

	/**
	 * The default value of the '{@link #isNrOfObjectsIsUnbounded() <em>Nr Of Objects Is Unbounded</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNrOfObjectsIsUnbounded()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NR_OF_OBJECTS_IS_UNBOUNDED_EDEFAULT = false;

	/**
	 * The parsed OCL expression for the definition of the '{@link #validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded <em>Validate All Unbounded Zones Are Outside If Nr Of Objects Are Unbounded</em>}' invariant constraint.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded
	 * @generated
	 */
	private static OCLExpression validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnboundedInvOCL;

	/**
	 * The parsed OCL expression for the definition of the '{@link #validateName <em>Validate Name</em>}' invariant constraint.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #validateName
	 * @generated
	 */
	private static OCLExpression validateNameInvOCL;

	/**
	 * The parsed OCL expression for the derivation of '{@link #getSensor <em>Sensor</em>}' property.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensor
	 * @generated
	 */
	private static OCLExpression sensorDeriveOCL;

	private static final String OCL_ANNOTATION_SOURCE = "http://www.eclipse.org/OCL/examples/ocl";

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GraphImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.GRAPH;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Zone> getZone() {
		if (zone == null) {
			zone = new EObjectContainmentWithInverseEList<Zone>(Zone.class, this, SagPackage.GRAPH__ZONE, SagPackage.ZONE__GRAPH);
		}
		return zone;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getMaxNrOfObjects() {
		return maxNrOfObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMaxNrOfObjects(int newMaxNrOfObjects) {
		int oldMaxNrOfObjects = maxNrOfObjects;
		maxNrOfObjects = newMaxNrOfObjects;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.GRAPH__MAX_NR_OF_OBJECTS, oldMaxNrOfObjects, maxNrOfObjects));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Node> getNode() {
		if (node == null) {
			node = new EObjectContainmentWithInverseEList<Node>(Node.class, this, SagPackage.GRAPH__NODE, SagPackage.NODE__GRAPH);
		}
		return node;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Project getProject() {
		if (eContainerFeatureID != SagPackage.GRAPH__PROJECT) return null;
		return (Project)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProject(Project newProject, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newProject, SagPackage.GRAPH__PROJECT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProject(Project newProject) {
		if (newProject != eInternalContainer() || (eContainerFeatureID != SagPackage.GRAPH__PROJECT && newProject != null)) {
			if (EcoreUtil.isAncestor(this, newProject))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newProject != null)
				msgs = ((InternalEObject)newProject).eInverseAdd(this, SagPackage.PROJECT__GRAPH, Project.class, msgs);
			msgs = basicSetProject(newProject, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.GRAPH__PROJECT, newProject, newProject));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean isNrOfObjectsIsUnbounded() {
		return getMaxNrOfObjects() < 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setNrOfObjectsIsUnbounded(boolean newNrOfObjectsIsUnbounded) {
		if (newNrOfObjectsIsUnbounded != isNrOfObjectsIsUnbounded()) {
			setMaxNrOfObjects(newNrOfObjectsIsUnbounded ? -1 : 1);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<SensorNode> getSensor() {

		EStructuralFeature eFeature = (EStructuralFeature) eClass().getEStructuralFeatures().get(5);
	
		if (sensorDeriveOCL == null) { 
			Environment env = ExpressionsUtil.createPropertyContext(eClass(), eFeature);
			EAnnotation ocl = eFeature.getEAnnotation(OCL_ANNOTATION_SOURCE);
			String derive = (String) ocl.getDetails().get("derive");
			
			try {
				sensorDeriveOCL = ExpressionsUtil.createQuery(env, derive, true);
			} catch (ParserException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage());
			}
		}
		
		Query query = QueryFactory.eINSTANCE.createQuery(sensorDeriveOCL);
		EvalEnvironment evalEnv = new EvalEnvironment();
		query.setEvaluationEnvironment(evalEnv);
	
		Collection result = (Collection) query.evaluate(this);
		return new EcoreEList.UnmodifiableEList(this, eFeature, result.size(), result.toArray());
	
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded(DiagnosticChain diagnostics, Map<?, ?> context) {
		if (validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnboundedInvOCL == null) {
			EOperation eOperation = (EOperation) eClass().getEOperations().get(0);
			Environment env = ExpressionsUtil.createClassifierContext(eClass());
			EAnnotation ocl = eOperation.getEAnnotation(OCL_ANNOTATION_SOURCE);
			String body = (String) ocl.getDetails().get("invariant");
			
			try {
				validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnboundedInvOCL = ExpressionsUtil.createInvariant(env, body, true);
			} catch (ParserException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage());
			}
		}
		
		Query query = QueryFactory.eINSTANCE.createQuery(validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnboundedInvOCL);
		EvalEnvironment evalEnv = new EvalEnvironment();
		query.setEvaluationEnvironment(evalEnv);
		
		if (!query.check(this)) {
			if (diagnostics != null) {
				diagnostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 SagValidator.DIAGNOSTIC_SOURCE,
						 SagValidator.GRAPH__VALIDATE_ALL_UNBOUNDED_ZONES_ARE_OUTSIDE_IF_NR_OF_OBJECTS_ARE_UNBOUNDED,
						 EcorePlugin.INSTANCE.getString("_UI_GenericInvariant_diagnostic", new Object[] { "validateAllUnboundedZonesAreOutsideIfNrOfObjectsAreUnbounded", EObjectValidator.getObjectLabel(this, (Map<Object,Object>) context) }),
						 new Object [] { this }));
			}
			return false;
		}
		return true;
		
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateName(DiagnosticChain diagnostics, Map<?, ?> context) {
		if (validateNameInvOCL == null) {
			EOperation eOperation = (EOperation) eClass().getEOperations().get(1);
			Environment env = ExpressionsUtil.createClassifierContext(eClass());
			EAnnotation ocl = eOperation.getEAnnotation(OCL_ANNOTATION_SOURCE);
			String body = (String) ocl.getDetails().get("invariant");
			
			try {
				validateNameInvOCL = ExpressionsUtil.createInvariant(env, body, true);
			} catch (ParserException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage());
			}
		}
		
		Query query = QueryFactory.eINSTANCE.createQuery(validateNameInvOCL);
		EvalEnvironment evalEnv = new EvalEnvironment();
		query.setEvaluationEnvironment(evalEnv);
		
		if (!query.check(this)) {
			if (diagnostics != null) {
				diagnostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 SagValidator.DIAGNOSTIC_SOURCE,
						 SagValidator.GRAPH__VALIDATE_NAME,
						 EcorePlugin.INSTANCE.getString("_UI_GenericInvariant_diagnostic", new Object[] { "validateName", EObjectValidator.getObjectLabel(this, (Map<Object,Object>) context) }),
						 new Object [] { this }));
			}
			return false;
		}
		return true;
		
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getZone()).basicAdd(otherEnd, msgs);
			case SagPackage.GRAPH__NODE:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getNode()).basicAdd(otherEnd, msgs);
			case SagPackage.GRAPH__PROJECT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetProject((Project)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return ((InternalEList<?>)getZone()).basicRemove(otherEnd, msgs);
			case SagPackage.GRAPH__NODE:
				return ((InternalEList<?>)getNode()).basicRemove(otherEnd, msgs);
			case SagPackage.GRAPH__PROJECT:
				return basicSetProject(null, msgs);
			case SagPackage.GRAPH__SENSOR:
				return ((InternalEList<?>)getSensor()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID) {
			case SagPackage.GRAPH__PROJECT:
				return eInternalContainer().eInverseRemove(this, SagPackage.PROJECT__GRAPH, Project.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return getZone();
			case SagPackage.GRAPH__MAX_NR_OF_OBJECTS:
				return new Integer(getMaxNrOfObjects());
			case SagPackage.GRAPH__NODE:
				return getNode();
			case SagPackage.GRAPH__PROJECT:
				return getProject();
			case SagPackage.GRAPH__NR_OF_OBJECTS_IS_UNBOUNDED:
				return isNrOfObjectsIsUnbounded() ? Boolean.TRUE : Boolean.FALSE;
			case SagPackage.GRAPH__SENSOR:
				return getSensor();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				getZone().clear();
				getZone().addAll((Collection<? extends Zone>)newValue);
				return;
			case SagPackage.GRAPH__MAX_NR_OF_OBJECTS:
				setMaxNrOfObjects(((Integer)newValue).intValue());
				return;
			case SagPackage.GRAPH__NODE:
				getNode().clear();
				getNode().addAll((Collection<? extends Node>)newValue);
				return;
			case SagPackage.GRAPH__PROJECT:
				setProject((Project)newValue);
				return;
			case SagPackage.GRAPH__NR_OF_OBJECTS_IS_UNBOUNDED:
				setNrOfObjectsIsUnbounded(((Boolean)newValue).booleanValue());
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				getZone().clear();
				return;
			case SagPackage.GRAPH__MAX_NR_OF_OBJECTS:
				setMaxNrOfObjects(MAX_NR_OF_OBJECTS_EDEFAULT);
				return;
			case SagPackage.GRAPH__NODE:
				getNode().clear();
				return;
			case SagPackage.GRAPH__PROJECT:
				setProject((Project)null);
				return;
			case SagPackage.GRAPH__NR_OF_OBJECTS_IS_UNBOUNDED:
				setNrOfObjectsIsUnbounded(NR_OF_OBJECTS_IS_UNBOUNDED_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case SagPackage.GRAPH__ZONE:
				return zone != null && !zone.isEmpty();
			case SagPackage.GRAPH__MAX_NR_OF_OBJECTS:
				return maxNrOfObjects != MAX_NR_OF_OBJECTS_EDEFAULT;
			case SagPackage.GRAPH__NODE:
				return node != null && !node.isEmpty();
			case SagPackage.GRAPH__PROJECT:
				return getProject() != null;
			case SagPackage.GRAPH__NR_OF_OBJECTS_IS_UNBOUNDED:
				return isNrOfObjectsIsUnbounded() != NR_OF_OBJECTS_IS_UNBOUNDED_EDEFAULT;
			case SagPackage.GRAPH__SENSOR:
				return !getSensor().isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (maxNrOfObjects: ");
		result.append(maxNrOfObjects);
		result.append(')');
		return result.toString();
	}

} //GraphImpl
