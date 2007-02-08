/**
 * <copyright>
 * </copyright>
 *
 * $Id: ZoneImpl.java,v 1.4 2007-02-08 16:36:08 torda Exp $
 */
package org.supremica.external.sag.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.Map;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ocl.expressions.OCLExpression;
import org.eclipse.emf.ocl.expressions.util.EvalEnvironment;
import org.eclipse.emf.ocl.expressions.util.ExpressionsUtil;
import org.eclipse.emf.ocl.parser.Environment;
import org.eclipse.emf.ocl.parser.ParserException;
import org.eclipse.emf.ocl.query.Query;
import org.eclipse.emf.ocl.query.QueryFactory;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Zone;
import org.supremica.external.sag.util.SagValidator;
import static org.supremica.external.sag.util.OclHelper.*;
/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Zone</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getFront <em>Front</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getBack <em>Back</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#isOneway <em>Oneway</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getGraph <em>Graph</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#getCapacity <em>Capacity</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#isOutsideSystemBoundry <em>Outside System Boundry</em>}</li>
 *   <li>{@link org.supremica.external.sag.impl.ZoneImpl#isBounded <em>Bounded</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ZoneImpl extends NamedImpl implements Zone {
	/**
	 * The cached value of the '{@link #getFront() <em>Front</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFront()
	 * @generated
	 * @ordered
	 */
	protected Node front = null;

	protected static final String ZONE_NAME_PREFIX = "zone";

	/**
	 * The cached value of the '{@link #getBack() <em>Back</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBack()
	 * @generated
	 * @ordered
	 */
	protected Node back = null;

	/**
	 * The default value of the '{@link #isOneway() <em>Oneway</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOneway()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ONEWAY_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isOneway() <em>Oneway</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOneway()
	 * @generated
	 * @ordered
	 */
	protected boolean oneway = ONEWAY_EDEFAULT;

	/**
	 * The default value of the '{@link #getCapacity() <em>Capacity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCapacity()
	 * @generated
	 * @ordered
	 */
	protected static final int CAPACITY_EDEFAULT = -1; // TODO The default value literal "null" is not valid.

	protected static final int CAPACITY_UNSET_VALUE = -1;
	/**
	 * The cached value of the '{@link #getCapacity() <em>Capacity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCapacity()
	 * @generated
	 * @ordered
	 */
	protected int capacity = CAPACITY_EDEFAULT;

	/**
	 * This is true if the Capacity attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean capacityESet = false;

	/**
	 * The default value of the '{@link #isOutsideSystemBoundry() <em>Outside System Boundry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOutsideSystemBoundry()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OUTSIDE_SYSTEM_BOUNDRY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOutsideSystemBoundry() <em>Outside System Boundry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOutsideSystemBoundry()
	 * @generated
	 * @ordered
	 */
	protected boolean outsideSystemBoundry = OUTSIDE_SYSTEM_BOUNDRY_EDEFAULT;

	/**
	 * The default value of the '{@link #isBounded() <em>Bounded</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isBounded()
	 * @generated
	 * @ordered
	 */
	protected static final boolean BOUNDED_EDEFAULT = false;

	/**
	 * The parsed OCL expression for the definition of the '{@link #validateCapacityIsPositiveNumber <em>Validate Capacity Is Positive Number</em>}' invariant constraint.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #validateCapacityIsPositiveNumber
	 * @generated
	 */
	private static OCLExpression validateCapacityIsPositiveNumberInvOCL;

	private static final String OCL_ANNOTATION_SOURCE = "http://www.eclipse.org/OCL/examples/ocl";

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ZoneImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.ZONE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node getFront() {
		if (front != null && front.eIsProxy()) {
			InternalEObject oldFront = (InternalEObject)front;
			front = (Node)eResolveProxy(oldFront);
			if (front != oldFront) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SagPackage.ZONE__FRONT, oldFront, front));
			}
		}
		return front;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node basicGetFront() {
		return front;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFront(Node newFront, NotificationChain msgs) {
		Node oldFront = front;
		front = newFront;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__FRONT, oldFront, newFront);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFront(Node newFront) {
		if (newFront != front) {
			NotificationChain msgs = null;
			if (front != null)
				msgs = ((InternalEObject)front).eInverseRemove(this, SagPackage.NODE__INCOMING, Node.class, msgs);
			if (newFront != null)
				msgs = ((InternalEObject)newFront).eInverseAdd(this, SagPackage.NODE__INCOMING, Node.class, msgs);
			msgs = basicSetFront(newFront, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__FRONT, newFront, newFront));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node getBack() {
		if (back != null && back.eIsProxy()) {
			InternalEObject oldBack = (InternalEObject)back;
			back = (Node)eResolveProxy(oldBack);
			if (back != oldBack) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SagPackage.ZONE__BACK, oldBack, back));
			}
		}
		return back;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node basicGetBack() {
		return back;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBack(Node newBack, NotificationChain msgs) {
		Node oldBack = back;
		back = newBack;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__BACK, oldBack, newBack);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBack(Node newBack) {
		if (newBack != back) {
			NotificationChain msgs = null;
			if (back != null)
				msgs = ((InternalEObject)back).eInverseRemove(this, SagPackage.NODE__OUTGOING, Node.class, msgs);
			if (newBack != null)
				msgs = ((InternalEObject)newBack).eInverseAdd(this, SagPackage.NODE__OUTGOING, Node.class, msgs);
			msgs = basicSetBack(newBack, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__BACK, newBack, newBack));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOneway() {
		return oneway;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOneway(boolean newOneway) {
		boolean oldOneway = oneway;
		oneway = newOneway;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__ONEWAY, oldOneway, oneway));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Graph getGraph() {
		if (eContainerFeatureID != SagPackage.ZONE__GRAPH) return null;
		return (Graph)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public NotificationChain basicSetGraph(Graph newGraph, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newGraph, SagPackage.ZONE__GRAPH, msgs);
		if (getName() == null || getName().trim().length() == 0) {
			int zoneIndex = 0;
			while (check(this, "graph.zone->exists(name = '" + ZONE_NAME_PREFIX + Integer.toString(zoneIndex) + "')")) {
				++zoneIndex;
			}
			setName(ZONE_NAME_PREFIX + Integer.toString(zoneIndex));
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGraph(Graph newGraph) {
		if (newGraph != eInternalContainer() || (eContainerFeatureID != SagPackage.ZONE__GRAPH && newGraph != null)) {
			if (EcoreUtil.isAncestor(this, newGraph))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newGraph != null)
				msgs = ((InternalEObject)newGraph).eInverseAdd(this, SagPackage.GRAPH__ZONE, Graph.class, msgs);
			msgs = basicSetGraph(newGraph, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__GRAPH, newGraph, newGraph));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setCapacity(int newCapacity) {
		if (newCapacity < 1) {
			unsetCapacity();
			return;
		}
		int oldCapacity = capacity;
		capacity = newCapacity;
		boolean oldCapacityESet = capacityESet;
		capacityESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__CAPACITY, oldCapacity, capacity, !oldCapacityESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void unsetCapacity() {
		int oldCapacity = capacity;
		boolean oldCapacityESet = capacityESet;
		capacity = CAPACITY_UNSET_VALUE;
		capacityESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, SagPackage.ZONE__CAPACITY, oldCapacity, CAPACITY_EDEFAULT, oldCapacityESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCapacity() {
		return capacityESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOutsideSystemBoundry() {
		return outsideSystemBoundry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutsideSystemBoundry(boolean newOutsideSystemBoundry) {
		boolean oldOutsideSystemBoundry = outsideSystemBoundry;
		outsideSystemBoundry = newOutsideSystemBoundry;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SagPackage.ZONE__OUTSIDE_SYSTEM_BOUNDRY, oldOutsideSystemBoundry, outsideSystemBoundry));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public boolean isBounded() {
		return isSetCapacity() || getCapacity() > 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setBounded(boolean newBounded) {
		if (newBounded) {
			setCapacity(CAPACITY_EDEFAULT);
		} else {
			unsetCapacity();
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCapacityIsPositiveNumber(DiagnosticChain diagnostics, Map<?, ?> context) {
		if (validateCapacityIsPositiveNumberInvOCL == null) {
			EOperation eOperation = (EOperation) eClass().getEOperations().get(0);
			Environment env = ExpressionsUtil.createClassifierContext(eClass());
			EAnnotation ocl = eOperation.getEAnnotation(OCL_ANNOTATION_SOURCE);
			String body = (String) ocl.getDetails().get("invariant");
			
			try {
				validateCapacityIsPositiveNumberInvOCL = ExpressionsUtil.createInvariant(env, body, true);
			} catch (ParserException e) {
				throw new UnsupportedOperationException(e.getLocalizedMessage());
			}
		}
		
		Query query = QueryFactory.eINSTANCE.createQuery(validateCapacityIsPositiveNumberInvOCL);
		EvalEnvironment evalEnv = new EvalEnvironment();
		query.setEvaluationEnvironment(evalEnv);
		
		if (!query.check(this)) {
			if (diagnostics != null) {
				diagnostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 SagValidator.DIAGNOSTIC_SOURCE,
						 SagValidator.ZONE__VALIDATE_CAPACITY_IS_POSITIVE_NUMBER,
						 EcorePlugin.INSTANCE.getString("_UI_GenericInvariant_diagnostic", new Object[] { "validateCapacityIsPositiveNumber", EObjectValidator.getObjectLabel(this, (Map<Object,Object>) context) }),
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
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SagPackage.ZONE__FRONT:
				if (front != null)
					msgs = ((InternalEObject)front).eInverseRemove(this, SagPackage.NODE__INCOMING, Node.class, msgs);
				return basicSetFront((Node)otherEnd, msgs);
			case SagPackage.ZONE__BACK:
				if (back != null)
					msgs = ((InternalEObject)back).eInverseRemove(this, SagPackage.NODE__OUTGOING, Node.class, msgs);
				return basicSetBack((Node)otherEnd, msgs);
			case SagPackage.ZONE__GRAPH:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetGraph((Graph)otherEnd, msgs);
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
			case SagPackage.ZONE__FRONT:
				return basicSetFront(null, msgs);
			case SagPackage.ZONE__BACK:
				return basicSetBack(null, msgs);
			case SagPackage.ZONE__GRAPH:
				return basicSetGraph(null, msgs);
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
			case SagPackage.ZONE__GRAPH:
				return eInternalContainer().eInverseRemove(this, SagPackage.GRAPH__ZONE, Graph.class, msgs);
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
			case SagPackage.ZONE__FRONT:
				if (resolve) return getFront();
				return basicGetFront();
			case SagPackage.ZONE__BACK:
				if (resolve) return getBack();
				return basicGetBack();
			case SagPackage.ZONE__ONEWAY:
				return isOneway() ? Boolean.TRUE : Boolean.FALSE;
			case SagPackage.ZONE__GRAPH:
				return getGraph();
			case SagPackage.ZONE__CAPACITY:
				return new Integer(getCapacity());
			case SagPackage.ZONE__OUTSIDE_SYSTEM_BOUNDRY:
				return isOutsideSystemBoundry() ? Boolean.TRUE : Boolean.FALSE;
			case SagPackage.ZONE__BOUNDED:
				return isBounded() ? Boolean.TRUE : Boolean.FALSE;
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SagPackage.ZONE__FRONT:
				setFront((Node)newValue);
				return;
			case SagPackage.ZONE__BACK:
				setBack((Node)newValue);
				return;
			case SagPackage.ZONE__ONEWAY:
				setOneway(((Boolean)newValue).booleanValue());
				return;
			case SagPackage.ZONE__GRAPH:
				setGraph((Graph)newValue);
				return;
			case SagPackage.ZONE__CAPACITY:
				setCapacity(((Integer)newValue).intValue());
				return;
			case SagPackage.ZONE__OUTSIDE_SYSTEM_BOUNDRY:
				setOutsideSystemBoundry(((Boolean)newValue).booleanValue());
				return;
			case SagPackage.ZONE__BOUNDED:
				setBounded(((Boolean)newValue).booleanValue());
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
			case SagPackage.ZONE__FRONT:
				setFront((Node)null);
				return;
			case SagPackage.ZONE__BACK:
				setBack((Node)null);
				return;
			case SagPackage.ZONE__ONEWAY:
				setOneway(ONEWAY_EDEFAULT);
				return;
			case SagPackage.ZONE__GRAPH:
				setGraph((Graph)null);
				return;
			case SagPackage.ZONE__CAPACITY:
				unsetCapacity();
				return;
			case SagPackage.ZONE__OUTSIDE_SYSTEM_BOUNDRY:
				setOutsideSystemBoundry(OUTSIDE_SYSTEM_BOUNDRY_EDEFAULT);
				return;
			case SagPackage.ZONE__BOUNDED:
				setBounded(BOUNDED_EDEFAULT);
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
			case SagPackage.ZONE__FRONT:
				return front != null;
			case SagPackage.ZONE__BACK:
				return back != null;
			case SagPackage.ZONE__ONEWAY:
				return oneway != ONEWAY_EDEFAULT;
			case SagPackage.ZONE__GRAPH:
				return getGraph() != null;
			case SagPackage.ZONE__CAPACITY:
				return isSetCapacity();
			case SagPackage.ZONE__OUTSIDE_SYSTEM_BOUNDRY:
				return outsideSystemBoundry != OUTSIDE_SYSTEM_BOUNDRY_EDEFAULT;
			case SagPackage.ZONE__BOUNDED:
				return isBounded() != BOUNDED_EDEFAULT;
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
		result.append(" (oneway: ");
		result.append(oneway);
		result.append(", capacity: ");
		if (capacityESet) result.append(capacity); else result.append("<unset>");
		result.append(", outsideSystemBoundry: ");
		result.append(outsideSystemBoundry);
		result.append(')');
		return result.toString();
	}

} //ZoneImpl
