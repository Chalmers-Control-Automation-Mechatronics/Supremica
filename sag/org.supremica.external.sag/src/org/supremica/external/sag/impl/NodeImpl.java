/**
 * <copyright>
 * </copyright>
 *
 * $Id: NodeImpl.java,v 1.3 2007-01-09 15:31:07 torda Exp $
 */
package org.supremica.external.sag.impl;

import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.emf.ecore.util.InternalEList;
import org.supremica.external.sag.Graph;
import org.supremica.external.sag.Node;
import org.supremica.external.sag.SagFactory;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Sensor;
import org.supremica.external.sag.Zone;
import static org.supremica.external.sag.util.OclHelper.*;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.supremica.external.sag.impl.NodeImpl#getGraph <em>Graph</em>}</li>
 * <li>{@link org.supremica.external.sag.impl.NodeImpl#getIncoming <em>Incoming</em>}</li>
 * <li>{@link org.supremica.external.sag.impl.NodeImpl#getOutgoing <em>Outgoing</em>}</li>
 * <li>{@link org.supremica.external.sag.impl.NodeImpl#getSensor <em>Sensor</em>}</li>
 * <li>{@link org.supremica.external.sag.impl.NodeImpl#getSensorName <em>Sensor Name</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class NodeImpl extends EObjectImpl implements Node {
	/**
	 * The cached value of the '{@link #getIncoming() <em>Incoming</em>}'
	 * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getIncoming()
	 * @generated
	 * @ordered
	 */
	protected EList<Zone> incoming = null;

	/**
	 * The cached value of the '{@link #getOutgoing() <em>Outgoing</em>}'
	 * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getOutgoing()
	 * @generated
	 * @ordered
	 */
	protected EList<Zone> outgoing = null;

	/**
	 * The cached value of the '{@link #getSensor() <em>Sensor</em>}'
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSensor()
	 * @generated
	 * @ordered
	 */
	protected Sensor sensor = null;

	/**
	 * The default value of the '{@link #getSensorName() <em>Sensor Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSensorName()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_NAME_EDEFAULT = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected NodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SagPackage.Literals.NODE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Sensor getSensor() {
		if (sensor != null && sensor.eIsProxy()) {
			InternalEObject oldSensor = (InternalEObject) sensor;
			sensor = (Sensor) eResolveProxy(oldSensor);
			if (sensor != oldSensor) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
							SagPackage.NODE__SENSOR, oldSensor, sensor));
			}
		}
		return sensor;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Sensor basicGetSensor() {
		return sensor;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetSensor(Sensor newSensor,
			NotificationChain msgs) {
		Sensor oldSensor = sensor;
		sensor = newSensor;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this,
					Notification.SET, SagPackage.NODE__SENSOR, oldSensor,
					newSensor);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setSensor(Sensor newSensor) {
		if (newSensor != sensor) {
			NotificationChain msgs = null;
			if (sensor != null)
				msgs = ((InternalEObject) sensor).eInverseRemove(this,
						SagPackage.SENSOR__NODE, Sensor.class, msgs);
			if (newSensor != null)
				msgs = ((InternalEObject) newSensor).eInverseAdd(this,
						SagPackage.SENSOR__NODE, Sensor.class, msgs);
			msgs = basicSetSensor(newSensor, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					SagPackage.NODE__SENSOR, newSensor, newSensor));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	public String getSensorName() {
		return getSensor() != null ? getSensor().getName() : null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	public void setSensorName(String newSensorName) {
		if (newSensorName == null || newSensorName.equals("")) {
			return;
		}
		Sensor existingSensorWithThisName = (Sensor) evaluate(this,
				"graph.project.sensor->any(name='"+newSensorName+"')");
		if (existingSensorWithThisName != null) {
			setSensor(existingSensorWithThisName);
		} else if (getSensor() != null) {
			getSensor().setName(newSensorName);
		} else {
			Sensor newSensor = SagFactory.eINSTANCE.createSensor();
			newSensor.setName(newSensorName);
			getGraph().getProject().getSensor().add(newSensor);
			setSensor(newSensor);
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Graph getGraph() {
		if (eContainerFeatureID != SagPackage.NODE__GRAPH)
			return null;
		return (Graph) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetGraph(Graph newGraph,
			NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newGraph,
				SagPackage.NODE__GRAPH, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setGraph(Graph newGraph) {
		if (newGraph != eInternalContainer()
				|| (eContainerFeatureID != SagPackage.NODE__GRAPH && newGraph != null)) {
			if (EcoreUtil.isAncestor(this, newGraph))
				throw new IllegalArgumentException(
						"Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newGraph != null)
				msgs = ((InternalEObject) newGraph).eInverseAdd(this,
						SagPackage.GRAPH__NODE, Graph.class, msgs);
			msgs = basicSetGraph(newGraph, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					SagPackage.NODE__GRAPH, newGraph, newGraph));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<Zone> getIncoming() {
		if (incoming == null) {
			incoming = new EObjectWithInverseResolvingEList<Zone>(Zone.class,
					this, SagPackage.NODE__INCOMING, SagPackage.ZONE__FRONT);
		}
		return incoming;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<Zone> getOutgoing() {
		if (outgoing == null) {
			outgoing = new EObjectWithInverseResolvingEList<Zone>(Zone.class,
					this, SagPackage.NODE__OUTGOING, SagPackage.ZONE__BACK);
		}
		return outgoing;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
		switch (featureID) {
		case SagPackage.NODE__GRAPH:
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			return basicSetGraph((Graph) otherEnd, msgs);
		case SagPackage.NODE__INCOMING:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getIncoming())
					.basicAdd(otherEnd, msgs);
		case SagPackage.NODE__OUTGOING:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getOutgoing())
					.basicAdd(otherEnd, msgs);
		case SagPackage.NODE__SENSOR:
			if (sensor != null)
				msgs = ((InternalEObject) sensor).eInverseRemove(this,
						SagPackage.SENSOR__NODE, Sensor.class, msgs);
			return basicSetSensor((Sensor) otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
		switch (featureID) {
		case SagPackage.NODE__GRAPH:
			return basicSetGraph(null, msgs);
		case SagPackage.NODE__INCOMING:
			return ((InternalEList<?>) getIncoming()).basicRemove(otherEnd,
					msgs);
		case SagPackage.NODE__OUTGOING:
			return ((InternalEList<?>) getOutgoing()).basicRemove(otherEnd,
					msgs);
		case SagPackage.NODE__SENSOR:
			return basicSetSensor(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(
			NotificationChain msgs) {
		switch (eContainerFeatureID) {
		case SagPackage.NODE__GRAPH:
			return eInternalContainer().eInverseRemove(this,
					SagPackage.GRAPH__NODE, Graph.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case SagPackage.NODE__GRAPH:
			return getGraph();
		case SagPackage.NODE__INCOMING:
			return getIncoming();
		case SagPackage.NODE__OUTGOING:
			return getOutgoing();
		case SagPackage.NODE__SENSOR:
			if (resolve)
				return getSensor();
			return basicGetSensor();
		case SagPackage.NODE__SENSOR_NAME:
			return getSensorName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case SagPackage.NODE__GRAPH:
			setGraph((Graph) newValue);
			return;
		case SagPackage.NODE__INCOMING:
			getIncoming().clear();
			getIncoming().addAll((Collection<? extends Zone>) newValue);
			return;
		case SagPackage.NODE__OUTGOING:
			getOutgoing().clear();
			getOutgoing().addAll((Collection<? extends Zone>) newValue);
			return;
		case SagPackage.NODE__SENSOR:
			setSensor((Sensor) newValue);
			return;
		case SagPackage.NODE__SENSOR_NAME:
			setSensorName((String) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case SagPackage.NODE__GRAPH:
			setGraph((Graph) null);
			return;
		case SagPackage.NODE__INCOMING:
			getIncoming().clear();
			return;
		case SagPackage.NODE__OUTGOING:
			getOutgoing().clear();
			return;
		case SagPackage.NODE__SENSOR:
			setSensor((Sensor) null);
			return;
		case SagPackage.NODE__SENSOR_NAME:
			setSensorName(SENSOR_NAME_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case SagPackage.NODE__GRAPH:
			return getGraph() != null;
		case SagPackage.NODE__INCOMING:
			return incoming != null && !incoming.isEmpty();
		case SagPackage.NODE__OUTGOING:
			return outgoing != null && !outgoing.isEmpty();
		case SagPackage.NODE__SENSOR:
			return sensor != null;
		case SagPackage.NODE__SENSOR_NAME:
			return SENSOR_NAME_EDEFAULT == null ? getSensorName() != null
					: !SENSOR_NAME_EDEFAULT.equals(getSensorName());
		}
		return super.eIsSet(featureID);
	}

} // NodeImpl
