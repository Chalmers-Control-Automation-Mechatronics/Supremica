/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.supremica.external.sag.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Zone;

/**
 * This is the item provider adapter for a {@link org.supremica.external.sag.Zone} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ZoneItemProvider
	extends NamedItemProvider
	implements	
		IEditingDomainItemProvider,	
		IStructuredItemContentProvider,	
		ITreeItemContentProvider,	
		IItemLabelProvider,	
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ZoneItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addFrontPropertyDescriptor(object);
			addBackPropertyDescriptor(object);
			addOnewayPropertyDescriptor(object);
			addCapacityPropertyDescriptor(object);
			addOutsideSystemBoundryPropertyDescriptor(object);
			addBoundedPropertyDescriptor(object);
			addForwardConditionPropertyDescriptor(object);
			addBackwardConditionPropertyDescriptor(object);
			addFrontEntryConditionPropertyDescriptor(object);
			addFrontExitConditionPropertyDescriptor(object);
			addBackEntryConditionPropertyDescriptor(object);
			addBackExitConditionPropertyDescriptor(object);
			addInitialNrOfObjectsPropertyDescriptor(object);
			addOverlappedPropertyDescriptor(object);
			addOrderedPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Front feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFrontPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_front_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_front_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__FRONT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Back feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBackPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_back_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_back_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__BACK,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Oneway feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOnewayPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_oneway_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_oneway_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__ONEWAY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Capacity feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCapacityPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_capacity_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_capacity_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__CAPACITY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Outside System Boundry feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOutsideSystemBoundryPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_outsideSystemBoundry_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_outsideSystemBoundry_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__OUTSIDE_SYSTEM_BOUNDRY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Bounded feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBoundedPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_bounded_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_bounded_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__BOUNDED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Forward Condition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addForwardConditionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_forwardCondition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_forwardCondition_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__FORWARD_CONDITION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Backward Condition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBackwardConditionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_backwardCondition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_backwardCondition_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__BACKWARD_CONDITION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Front Entry Condition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFrontEntryConditionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_frontEntryCondition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_frontEntryCondition_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__FRONT_ENTRY_CONDITION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Front Exit Condition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFrontExitConditionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_frontExitCondition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_frontExitCondition_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__FRONT_EXIT_CONDITION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Back Entry Condition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBackEntryConditionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_backEntryCondition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_backEntryCondition_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__BACK_ENTRY_CONDITION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Back Exit Condition feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBackExitConditionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_backExitCondition_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_backExitCondition_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__BACK_EXIT_CONDITION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Initial Nr Of Objects feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addInitialNrOfObjectsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_initialNrOfObjects_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_initialNrOfObjects_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__INITIAL_NR_OF_OBJECTS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Overlapped feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOverlappedPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_overlapped_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_overlapped_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__OVERLAPPED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ordered feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOrderedPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Zone_ordered_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Zone_ordered_feature", "_UI_Zone_type"),
				 SagPackage.Literals.ZONE__ORDERED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getText(Object object) {
		String label = ((Zone)object).getName();
		return label == null || label.length() == 0 ?
			getString("_UI_Zone_type") :
			getString("_UI_Zone_type") + " " + label;
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(Zone.class)) {
			case SagPackage.ZONE__ONEWAY:
			case SagPackage.ZONE__CAPACITY:
			case SagPackage.ZONE__OUTSIDE_SYSTEM_BOUNDRY:
			case SagPackage.ZONE__BOUNDED:
			case SagPackage.ZONE__FORWARD_CONDITION:
			case SagPackage.ZONE__BACKWARD_CONDITION:
			case SagPackage.ZONE__FRONT_ENTRY_CONDITION:
			case SagPackage.ZONE__FRONT_EXIT_CONDITION:
			case SagPackage.ZONE__BACK_ENTRY_CONDITION:
			case SagPackage.ZONE__BACK_EXIT_CONDITION:
			case SagPackage.ZONE__INITIAL_NR_OF_OBJECTS:
			case SagPackage.ZONE__OVERLAPPED:
			case SagPackage.ZONE__ORDERED:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
	 * describing all of the children that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourceLocator getResourceLocator() {
		return SagEditPlugin.INSTANCE;
	}

}
