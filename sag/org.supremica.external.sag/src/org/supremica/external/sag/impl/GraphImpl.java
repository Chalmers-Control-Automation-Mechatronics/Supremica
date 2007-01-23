/**
 * <copyright>
 * </copyright>
 *
 * $Id: GraphImpl.java,v 1.4 2007-01-23 09:55:48 torda Exp $
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
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectValidator;
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
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#isMultipleObjects <em>Multiple Objects</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getNode <em>Node</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.GraphImpl#getProject <em>Project</em>}</li>
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
	 * The default value of the '{@link #isMultipleObjects() <em>Multiple Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultipleObjects()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MULTIPLE_OBJECTS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isMultipleObjects() <em>Multiple Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultipleObjects()
	 * @generated
	 * @ordered
	 */
	protected boolean multipleObjects = MULTIPLE_OBJECTS_EDEFAULT;

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
	 * The parsed OCL expression for the definition of the '{@link #validateAllUnboundedZonesAreOutsideIfObjectsAreIdentityless <em>Validate All Unbounded Zones Are Outside If Objects Are Identityless</em>}' invariant constraint.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #validateAllUnboundedZonesAreOutsideIfObjectsAreIdentityless
	 * @generated
	 */
	private static OCLExpression validateAllUnboundedZonesAreOutsideIfObjectsAreIdentitylessInvOCL;

	/**
	 * The parsed OCL expression for the definition of the '{@link #validateName <em>Validate Name</em>}' invariant constraint.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #validateName
	 * @generated
	 */
	private static OCLExpression validateNameInvOCL;

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
	public boolean isMultipleObjects() {
		return multipleObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMultipleObjects(boolean newMultipleObjects) {
		boolean oldMultipleObjects = multipleObjects;
		multipleObjects = newMultipleObjects;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.GRAPH__MULTIPLE_OBJECTS, oldMultipleObjects, multipleObjects));
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
	 * @generated
	 */
	public boolean validateAllUnboundedZonesAreOutsideIfObjectsAreIdentityless(DiagnosticChain diagnostics, Map<?, ?> context) {
		if (validateAllUnboundedZonesAreOutsideIfObjectsAreIdentitylessInvOCL == null) {
			EOperation eOperation = (EOperation) eClass().getEOperations().get(0);
			Environment env = ExpressionsUtil.createClassifierContext(eClass());
			EAnnotation ocl = eOperation.getEAnnotation(OCL_ANNOTATION_SOURCE);
			String body = (String) ocl.getDetails().get("invariant");
			
			try {
				validateAllUnboundedZonesAreOutsideIfObjectsAreIdentitylessInvOCL = ExpressionsUtil.createInvariant(env, body, true);
			} catch (ParserException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage());
			}
		}
		
		Query query = QueryFactory.eINSTANCE.createQuery(validateAllUnboundedZonesAreOutsideIfObjectsAreIdentitylessInvOCL);
		EvalEnvironment evalEnv = new EvalEnvironment();
		query.setEvaluationEnvironment(evalEnv);
		
		if (!query.check(this)) {
			if (diagnostics != null) {
				diagnostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 SagValidator.DIAGNOSTIC_SOURCE,
						 SagValidator.GRAPH__VALIDATE_ALL_UNBOUNDED_ZONES_ARE_OUTSIDE_IF_OBJECTS_ARE_IDENTITYLESS,
						 EcorePlugin.INSTANCE.getString("_UI_GenericInvariant_diagnostic", new Object[] { "validateAllUnboundedZonesAreOutsideIfObjectsAreIdentityless", EObjectValidator.getObjectLabel(this, (Map<Object,Object>) context) }),
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
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				return isMultipleObjects() ? Boolean.TRUE : Boolean.FALSE;
			case SagPackage.GRAPH__NODE:
				return getNode();
			case SagPackage.GRAPH__PROJECT:
				return getProject();
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
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				setMultipleObjects(((Boolean)newValue).booleanValue());
				return;
			case SagPackage.GRAPH__NODE:
				getNode().clear();
				getNode().addAll((Collection<? extends Node>)newValue);
				return;
			case SagPackage.GRAPH__PROJECT:
				setProject((Project)newValue);
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
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				setMultipleObjects(MULTIPLE_OBJECTS_EDEFAULT);
				return;
			case SagPackage.GRAPH__NODE:
				getNode().clear();
				return;
			case SagPackage.GRAPH__PROJECT:
				setProject((Project)null);
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
			case SagPackage.GRAPH__MULTIPLE_OBJECTS:
				return multipleObjects != MULTIPLE_OBJECTS_EDEFAULT;
			case SagPackage.GRAPH__NODE:
				return node != null && !node.isEmpty();
			case SagPackage.GRAPH__PROJECT:
				return getProject() != null;
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
		result.append(" (multipleObjects: ");
		result.append(multipleObjects);
		result.append(')');
		return result.toString();
	}

} //GraphImpl
