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
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.diagram.part.SagDiagramEditorPlugin;

/**
 * @generated
 */
public class SagElementTypes extends ElementInitializers {

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

			elements.put(Project_1000, SagPackage.eINSTANCE.getProject());

			elements.put(Graph_2010, SagPackage.eINSTANCE.getGraph());

			elements.put(SensorNode_3006, SagPackage.eINSTANCE.getSensorNode());

			elements.put(EndNode_3007, SagPackage.eINSTANCE.getEndNode());

			elements.put(BoundedZone_4007, SagPackage.eINSTANCE
					.getBoundedZone());

			elements.put(UnboundedZone_4009, SagPackage.eINSTANCE
					.getUnboundedZone());
		}
		return (ENamedElement) elements.get(type);
	}

	/**
	 * @generated
	 */
	public static final IElementType Project_1000 = getElementType("org.supremica.external.sag.diagram.Project_1000"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType Graph_2010 = getElementType("org.supremica.external.sag.diagram.Graph_2010"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType SensorNode_3006 = getElementType("org.supremica.external.sag.diagram.SensorNode_3006"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType EndNode_3007 = getElementType("org.supremica.external.sag.diagram.EndNode_3007"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType BoundedZone_4007 = getElementType("org.supremica.external.sag.diagram.BoundedZone_4007"); //$NON-NLS-1$
	/**
	 * @generated
	 */
	public static final IElementType UnboundedZone_4009 = getElementType("org.supremica.external.sag.diagram.UnboundedZone_4009"); //$NON-NLS-1$

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
			KNOWN_ELEMENT_TYPES.add(Project_1000);
			KNOWN_ELEMENT_TYPES.add(Graph_2010);
			KNOWN_ELEMENT_TYPES.add(SensorNode_3006);
			KNOWN_ELEMENT_TYPES.add(EndNode_3007);
			KNOWN_ELEMENT_TYPES.add(BoundedZone_4007);
			KNOWN_ELEMENT_TYPES.add(UnboundedZone_4009);
		}
		return KNOWN_ELEMENT_TYPES.contains(elementType);
	}
}
