package org.supremica.external.sag.diagram.providers;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.supremica.external.sag.SagPackage;

import org.supremica.external.sag.diagram.expressions.SagAbstractExpression;
import org.supremica.external.sag.diagram.expressions.SagOCLFactory;

import org.supremica.external.sag.diagram.part.SagDiagramEditorPlugin;

/**
 * @generated
 */
public class SagElementTypes {

	/**
	 * @generated
	 */
	private SagElementTypes() {
	}

	/**
	 * @generated
	 */
	private static Map elements;

	/**
	 * @generated
	 */
	private static ImageRegistry imageRegistry;

	/**
	 * @generated
	 */
	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	/**
	 * @generated
	 */
	private static String getImageRegistryKey(ENamedElement element) {
		return element.getName();
	}

	/**
	 * @generated
	 */
	private static ImageDescriptor getProvidedImageDescriptor(
			ENamedElement element) {
		if (element instanceof EStructuralFeature) {
			element = ((EStructuralFeature) element).getEContainingClass();
		}
		if (element instanceof EClass) {
			EClass eClass = (EClass) element;
			if (!eClass.isAbstract()) {
				return SagDiagramEditorPlugin.getInstance()
						.getItemImageDescriptor(
								eClass.getEPackage().getEFactoryInstance()
										.create(eClass));
			}
		}
		// TODO : support structural features
		return null;
	}

	/**
	 * @generated
	 */
	public static ImageDescriptor getImageDescriptor(ENamedElement element) {
		String key = getImageRegistryKey(element);
		ImageDescriptor imageDescriptor = getImageRegistry().getDescriptor(key);
		if (imageDescriptor == null) {
			imageDescriptor = getProvidedImageDescriptor(element);
			if (imageDescriptor == null) {
				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
			}
			getImageRegistry().put(key, imageDescriptor);
		}
		return imageDescriptor;
	}

	/**
	 * @generated
	 */
	public static Image getImage(ENamedElement element) {
		String key = getImageRegistryKey(element);
		Image image = getImageRegistry().get(key);
		if (image == null) {
			ImageDescriptor imageDescriptor = getProvidedImageDescriptor(element);
			if (imageDescriptor == null) {
				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
			}
			getImageRegistry().put(key, imageDescriptor);
			image = getImageRegistry().get(key);
		}
		return image;
	}

	/**
	 * @generated
	 */
	public static ImageDescriptor getImageDescriptor(IAdaptable hint) {
		ENamedElement element = getElement(hint);
		if (element == null) {
			return null;
		}
		return getImageDescriptor(element);
	}

	/**
	 * @generated
	 */
	public static Image getImage(IAdaptable hint) {
		ENamedElement element = getElement(hint);
		if (element == null) {
			return null;
		}
		return getImage(element);
	}

	/**
	 * Returns 'type' of the ecore object associated with the hint.
	 * 
	 * @generated
	 */
	public static ENamedElement getElement(IAdaptable hint) {
		Object type = hint.getAdapter(IElementType.class);
		if (elements == null) {
			elements = new IdentityHashMap();
			elements.put(Project_79, SagPackage.eINSTANCE.getProject());
			elements.put(Node_2001, SagPackage.eINSTANCE.getNode());
			elements.put(Node_2002, SagPackage.eINSTANCE.getNode());
			elements.put(Graph_1001, SagPackage.eINSTANCE.getGraph());
			elements.put(BoundedZone_3001, SagPackage.eINSTANCE
					.getBoundedZone());
			elements.put(BoundedZone_3002, SagPackage.eINSTANCE
					.getBoundedZone());
			elements.put(UnboundedZone_3003, SagPackage.eINSTANCE
					.getUnboundedZone());
			elements.put(UnboundedZone_3004, SagPackage.eINSTANCE
					.getUnboundedZone());
			elements.put(UnboundedZone_3005, SagPackage.eINSTANCE
					.getUnboundedZone());
			elements.put(UnboundedZone_3006, SagPackage.eINSTANCE
					.getUnboundedZone());
		}
		return (ENamedElement) elements.get(type);
	}

	/**
	 * @generated
	 */
	public static final IElementType Project_79 = getElementType("org.supremica.external.sag.diagram.Project_79"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType Node_2001 = getElementType("org.supremica.external.sag.diagram.Node_2001"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType Node_2002 = getElementType("org.supremica.external.sag.diagram.Node_2002"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType Graph_1001 = getElementType("org.supremica.external.sag.diagram.Graph_1001"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType BoundedZone_3001 = getElementType("org.supremica.external.sag.diagram.BoundedZone_3001"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType BoundedZone_3002 = getElementType("org.supremica.external.sag.diagram.BoundedZone_3002"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType UnboundedZone_3003 = getElementType("org.supremica.external.sag.diagram.UnboundedZone_3003"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType UnboundedZone_3004 = getElementType("org.supremica.external.sag.diagram.UnboundedZone_3004"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType UnboundedZone_3005 = getElementType("org.supremica.external.sag.diagram.UnboundedZone_3005"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	public static final IElementType UnboundedZone_3006 = getElementType("org.supremica.external.sag.diagram.UnboundedZone_3006"); //$NON-NLS-1$

	/**
	 * @generated
	 */
	private static IElementType getElementType(String id) {
		return ElementTypeRegistry.getInstance().getType(id);
	}

	/**
	 * @generated
	 */
	private static Set KNOWN_ELEMENT_TYPES;

	/**
	 * @generated
	 */
	public static boolean isKnownElementType(IElementType elementType) {
		if (KNOWN_ELEMENT_TYPES == null) {
			KNOWN_ELEMENT_TYPES = new HashSet();
			KNOWN_ELEMENT_TYPES.add(Project_79);
			KNOWN_ELEMENT_TYPES.add(Node_2001);
			KNOWN_ELEMENT_TYPES.add(Node_2002);
			KNOWN_ELEMENT_TYPES.add(Graph_1001);
			KNOWN_ELEMENT_TYPES.add(BoundedZone_3001);
			KNOWN_ELEMENT_TYPES.add(BoundedZone_3002);
			KNOWN_ELEMENT_TYPES.add(UnboundedZone_3003);
			KNOWN_ELEMENT_TYPES.add(UnboundedZone_3004);
			KNOWN_ELEMENT_TYPES.add(UnboundedZone_3005);
			KNOWN_ELEMENT_TYPES.add(UnboundedZone_3006);
		}
		return KNOWN_ELEMENT_TYPES.contains(elementType);
	}

	/**
	 * @generated
	 */
	public static class Initializers {
		/**
		 * @generated
		 */
		public static final IObjectInitializer BoundedZone_3001 = new ObjectInitializer(
				org.supremica.external.sag.SagPackage.eINSTANCE
						.getBoundedZone()) {
			protected void init() {
				add(createExpressionFeatureInitializer(SagPackage.eINSTANCE
						.getZone_IsOneway(), SagOCLFactory.getExpression(
						"true", //$NON-NLS-1$
						SagPackage.eINSTANCE.getBoundedZone())));
			}
		}; // BoundedZone_3001 ObjectInitializer		
		/**
		 * @generated
		 */
		public static final IObjectInitializer UnboundedZone_3003 = new ObjectInitializer(
				org.supremica.external.sag.SagPackage.eINSTANCE
						.getUnboundedZone()) {
			protected void init() {
				add(createExpressionFeatureInitializer(SagPackage.eINSTANCE
						.getZone_IsOneway(), SagOCLFactory.getExpression(
						"true", //$NON-NLS-1$
						SagPackage.eINSTANCE.getUnboundedZone())));
			}
		}; // UnboundedZone_3003 ObjectInitializer		
		/**
		 * @generated
		 */
		public static final IObjectInitializer UnboundedZone_3005 = new ObjectInitializer(
				org.supremica.external.sag.SagPackage.eINSTANCE
						.getUnboundedZone()) {
			protected void init() {
				add(createExpressionFeatureInitializer(SagPackage.eINSTANCE
						.getZone_IsOneway(), SagOCLFactory.getExpression(
						"true", //$NON-NLS-1$
						SagPackage.eINSTANCE.getUnboundedZone())));
				add(createExpressionFeatureInitializer(SagPackage.eINSTANCE
						.getUnboundedZone_IsOutside(), SagOCLFactory
						.getExpression("true", //$NON-NLS-1$
								SagPackage.eINSTANCE.getUnboundedZone())));
			}
		}; // UnboundedZone_3005 ObjectInitializer		
		/**
		 * @generated
		 */
		public static final IObjectInitializer UnboundedZone_3006 = new ObjectInitializer(
				org.supremica.external.sag.SagPackage.eINSTANCE
						.getUnboundedZone()) {
			protected void init() {
				add(createExpressionFeatureInitializer(SagPackage.eINSTANCE
						.getUnboundedZone_IsOutside(), SagOCLFactory
						.getExpression("true", //$NON-NLS-1$
								SagPackage.eINSTANCE.getUnboundedZone())));
			}
		}; // UnboundedZone_3006 ObjectInitializer

		/** 
		 * @generated
		 */
		private Initializers() {
		}

		/** 
		 * @generated
		 */
		public static interface IObjectInitializer {
			/** 
			 * @generated
			 */
			public void init(EObject instance);
		}

		/** 
		 * @generated
		 */
		public static abstract class ObjectInitializer implements
				IObjectInitializer {
			/** 
			 * @generated
			 */
			final EClass element;
			/** 
			 * @generated
			 */
			private List featureInitializers = new ArrayList();

			/** 
			 * @generated
			 */
			ObjectInitializer(EClass element) {
				this.element = element;
				init();
			}

			/**
			 * @generated
			 */
			protected abstract void init();

			/** 
			 * @generated
			 */
			protected final IFeatureInitializer add(
					IFeatureInitializer initializer) {
				featureInitializers.add(initializer);
				return initializer;
			}

			/** 
			 * @generated
			 */
			public void init(EObject instance) {
				for (java.util.Iterator it = featureInitializers.iterator(); it
						.hasNext();) {
					IFeatureInitializer nextExpr = (IFeatureInitializer) it
							.next();
					try {
						nextExpr.init(instance);
					} catch (RuntimeException e) {
						SagDiagramEditorPlugin.getInstance().logError(
								"Feature initialization failed", e); //$NON-NLS-1$						
					}
				}
			}
		} // end of ObjectInitializer

		/** 
		 * @generated
		 */
		interface IFeatureInitializer {
			/**
			 * @generated
			 */
			void init(EObject contextInstance);
		}

		/**
		 * @generated
		 */
		static IFeatureInitializer createNewElementFeatureInitializer(
				EStructuralFeature initFeature,
				ObjectInitializer[] newObjectInitializers) {
			final EStructuralFeature feature = initFeature;
			final ObjectInitializer[] initializers = newObjectInitializers;
			return new IFeatureInitializer() {
				public void init(EObject contextInstance) {
					for (int i = 0; i < initializers.length; i++) {
						EObject newInstance = initializers[i].element
								.getEPackage().getEFactoryInstance().create(
										initializers[i].element);
						if (feature.isMany()) {
							((Collection) contextInstance.eGet(feature))
									.add(newInstance);
						} else {
							contextInstance.eSet(feature, newInstance);
						}
						initializers[i].init(newInstance);
					}
				}
			};
		}

		/**
		 * @generated
		 */
		static IFeatureInitializer createExpressionFeatureInitializer(
				EStructuralFeature initFeature,
				SagAbstractExpression valueExpression) {
			final EStructuralFeature feature = initFeature;
			final SagAbstractExpression expression = valueExpression;
			return new IFeatureInitializer() {
				public void init(EObject contextInstance) {
					expression.assignTo(feature, contextInstance);
				}
			};
		}
	} // end of Initializers
}
