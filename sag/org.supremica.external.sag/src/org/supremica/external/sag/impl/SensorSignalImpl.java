
/**
 * <copyright>
 * </copyright>
 *
 * $Id$
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

import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.emf.ocl.expressions.OCLExpression;

import org.eclipse.emf.ocl.expressions.util.EvalEnvironment;
import org.eclipse.emf.ocl.expressions.util.ExpressionsUtil;

import org.eclipse.emf.ocl.parser.Environment;
import org.eclipse.emf.ocl.parser.ParserException;

import org.eclipse.emf.ocl.query.Query;
import org.eclipse.emf.ocl.query.QueryFactory;

import org.supremica.external.sag.Project;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Sensor;
import org.supremica.external.sag.SensorSignal;

import org.supremica.external.sag.util.SagValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Signal</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.SensorSignalImpl#getSensor <em>Sensor</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.SensorSignalImpl#getProject <em>Project</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SensorSignalImpl extends NamedImpl implements SensorSignal {
	/**
	 * The cached value of the '{@link #getSensor() <em>Sensor</em>}' reference list.
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
	
	/**
	 * The parsed OCL expression for the definition of the '{@link #validateUniquenessOfName <em>Validate Uniqueness Of Name</em>}' invariant constraint.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #validateUniquenessOfName
	 * @generated
	 */
	private static OCLExpression validateUniquenessOfNameInvOCL;
	
	private static final String OCL_ANNOTATION_SOURCE = "http://www.eclipse.org/OCL/examples/ocl";
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorSignalImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.SENSOR_SIGNAL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Sensor> getSensor() {
		if (sensor == null) {
			sensor = new EObjectWithInverseResolvingEList<Sensor>(Sensor.class, this, SagPackage.SENSOR_SIGNAL__SENSOR, SagPackage.SENSOR__SIGNAL);
		}
		return sensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Project getProject() {
		if (eContainerFeatureID != SagPackage.SENSOR_SIGNAL__PROJECT) return null;
		return (Project)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProject(Project newProject, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newProject, SagPackage.SENSOR_SIGNAL__PROJECT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProject(Project newProject) {
		if (newProject != eInternalContainer() || (eContainerFeatureID != SagPackage.SENSOR_SIGNAL__PROJECT && newProject != null)) {
			if (EcoreUtil.isAncestor(this, newProject))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newProject != null)
				msgs = ((InternalEObject)newProject).eInverseAdd(this, SagPackage.PROJECT__SENSOR_SIGNAL, Project.class, msgs);
			msgs = basicSetProject(newProject, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.SENSOR_SIGNAL__PROJECT, newProject, newProject));
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
						 SagValidator.SENSOR_SIGNAL__VALIDATE_NAME,
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
	public boolean validateUniquenessOfName(DiagnosticChain diagnostics, Map<?, ?> context) {
		if (validateUniquenessOfNameInvOCL == null) {
			EOperation eOperation = (EOperation) eClass().getEOperations().get(1);
			Environment env = ExpressionsUtil.createClassifierContext(eClass());
			EAnnotation ocl = eOperation.getEAnnotation(OCL_ANNOTATION_SOURCE);
			String body = (String) ocl.getDetails().get("invariant");
			
			try {
				validateUniquenessOfNameInvOCL = ExpressionsUtil.createInvariant(env, body, true);
			} catch (ParserException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage());
			}
		}
		
		Query query = QueryFactory.eINSTANCE.createQuery(validateUniquenessOfNameInvOCL);
		EvalEnvironment evalEnv = new EvalEnvironment();
		query.setEvaluationEnvironment(evalEnv);
		
		if (!query.check(this)) {
			if (diagnostics != null) {
				diagnostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 SagValidator.DIAGNOSTIC_SOURCE,
						 SagValidator.SENSOR_SIGNAL__VALIDATE_UNIQUENESS_OF_NAME,
						 EcorePlugin.INSTANCE.getString("_UI_GenericInvariant_diagnostic", new Object[] { "validateUniquenessOfName", EObjectValidator.getObjectLabel(this, (Map<Object,Object>) context) }),
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
			case SagPackage.SENSOR_SIGNAL__SENSOR:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getSensor()).basicAdd(otherEnd, msgs);
			case SagPackage.SENSOR_SIGNAL__PROJECT:
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
			case SagPackage.SENSOR_SIGNAL__SENSOR:
				return ((InternalEList<?>)getSensor()).basicRemove(otherEnd, msgs);
			case SagPackage.SENSOR_SIGNAL__PROJECT:
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
			case SagPackage.SENSOR_SIGNAL__PROJECT:
				return eInternalContainer().eInverseRemove(this, SagPackage.PROJECT__SENSOR_SIGNAL, Project.class, msgs);
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
			case SagPackage.SENSOR_SIGNAL__SENSOR:
				return getSensor();
			case SagPackage.SENSOR_SIGNAL__PROJECT:
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
			case SagPackage.SENSOR_SIGNAL__SENSOR:
				getSensor().clear();
				getSensor().addAll((Collection<? extends Sensor>)newValue);
				return;
			case SagPackage.SENSOR_SIGNAL__PROJECT:
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
			case SagPackage.SENSOR_SIGNAL__SENSOR:
				getSensor().clear();
				return;
			case SagPackage.SENSOR_SIGNAL__PROJECT:
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
			case SagPackage.SENSOR_SIGNAL__SENSOR:
				return sensor != null && !sensor.isEmpty();
			case SagPackage.SENSOR_SIGNAL__PROJECT:
				return getProject() != null;
		}
		return super.eIsSet(featureID);
	}

} //SensorSignalImpl
