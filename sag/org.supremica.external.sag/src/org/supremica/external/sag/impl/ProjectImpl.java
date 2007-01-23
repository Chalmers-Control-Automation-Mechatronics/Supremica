/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProjectImpl.java,v 1.3 2007-01-23 09:55:48 torda Exp $
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

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.emf.ocl.expressions.OCLExpression;
import org.eclipse.emf.ocl.expressions.util.EvalEnvironment;
import org.eclipse.emf.ocl.expressions.util.ExpressionsUtil;
import org.eclipse.emf.ocl.parser.Environment;
import org.eclipse.emf.ocl.parser.ParserException;
import org.eclipse.emf.ocl.query.Query;
import org.eclipse.emf.ocl.query.QueryFactory;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Project;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Sensor;
import org.supremica.external.sag.util.SagValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Project</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.ProjectImpl#getGraph <em>Graph</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ProjectImpl#getSensor <em>Sensor</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ProjectImpl extends NamedImpl implements Project {
	/**
	 * The cached value of the '{@link #getGraph() <em>Graph</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGraph()
	 * @generated
	 * @ordered
	 */
	protected EList<Graph> graph = null;

	/**
	 * The cached value of the '{@link #getSensor() <em>Sensor</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensor()
	 * @generated
	 * @ordered
	 */
	protected EList<Sensor> sensor = null;

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
	protected ProjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.PROJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Graph> getGraph() {
		if (graph == null) {
			graph = new EObjectContainmentWithInverseEList<Graph>(Graph.class, this, SagPackage.PROJECT__GRAPH, SagPackage.GRAPH__PROJECT);
		}
		return graph;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Sensor> getSensor() {
		if (sensor == null) {
			sensor = new EObjectContainmentWithInverseEList<Sensor>(Sensor.class, this, SagPackage.PROJECT__SENSOR, SagPackage.SENSOR__PROJECT);
		}
		return sensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateName(DiagnosticChain diagnostics, Map<?, ?> context) {
		if (validateNameInvOCL == null) {
			EOperation eOperation = (EOperation) eClass().getEOperations().get(0);
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
						 SagValidator.PROJECT__VALIDATE_NAME,
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
			case SagPackage.PROJECT__GRAPH:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getGraph()).basicAdd(otherEnd, msgs);
			case SagPackage.PROJECT__SENSOR:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getSensor()).basicAdd(otherEnd, msgs);
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
			case SagPackage.PROJECT__GRAPH:
				return ((InternalEList<?>)getGraph()).basicRemove(otherEnd, msgs);
			case SagPackage.PROJECT__SENSOR:
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
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SagPackage.PROJECT__GRAPH:
				return getGraph();
			case SagPackage.PROJECT__SENSOR:
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
			case SagPackage.PROJECT__GRAPH:
				getGraph().clear();
				getGraph().addAll((Collection<? extends Graph>)newValue);
				return;
			case SagPackage.PROJECT__SENSOR:
				getSensor().clear();
				getSensor().addAll((Collection<? extends Sensor>)newValue);
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
			case SagPackage.PROJECT__GRAPH:
				getGraph().clear();
				return;
			case SagPackage.PROJECT__SENSOR:
				getSensor().clear();
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
			case SagPackage.PROJECT__GRAPH:
				return graph != null && !graph.isEmpty();
			case SagPackage.PROJECT__SENSOR:
				return sensor != null && !sensor.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //ProjectImpl
