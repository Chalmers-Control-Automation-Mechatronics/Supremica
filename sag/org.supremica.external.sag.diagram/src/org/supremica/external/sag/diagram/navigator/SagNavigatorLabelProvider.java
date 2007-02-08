package org.supremica.external.sag.diagram.navigator;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserOptions;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserService;

import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;

import org.eclipse.gmf.runtime.emf.type.core.IElementType;

import org.eclipse.gmf.runtime.notation.View;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;

import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.IMemento;

import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

import org.supremica.external.sag.Named;

import org.supremica.external.sag.diagram.edit.parts.EndNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorNodeNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.ZoneEditPart;

import org.supremica.external.sag.diagram.part.SagDiagramEditorPlugin;
import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

import org.supremica.external.sag.diagram.providers.SagElementTypes;

/**
 * @generated
 */
public class SagNavigatorLabelProvider extends LabelProvider implements
		ICommonLabelProvider, ITreePathLabelProvider {

	/**
	 * @generated
	 */
	static {
		SagDiagramEditorPlugin.getInstance().getImageRegistry().put(
				"Navigator?InvalidElement",
				ImageDescriptor.getMissingImageDescriptor());
		SagDiagramEditorPlugin.getInstance().getImageRegistry().put(
				"Navigator?UnknownElement",
				ImageDescriptor.getMissingImageDescriptor());
		SagDiagramEditorPlugin.getInstance().getImageRegistry().put(
				"Navigator?ImageNotFound",
				ImageDescriptor.getMissingImageDescriptor());
	}

	/**
	 * @generated
	 */
	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = elementPath.getLastSegment();
		if (element instanceof SagNavigatorItem
				&& !isOwnView(((SagNavigatorItem) element).getView())) {
			return;
		}
		label.setText(getText(element));
		label.setImage(getImage(element));
	}

	/**
	 * @generated
	 */
	public Image getImage(Object element) {
		if (element instanceof SagNavigatorGroup) {
			SagNavigatorGroup group = (SagNavigatorGroup) element;
			return SagDiagramEditorPlugin.getInstance().getBundledImage(
					group.getIcon());
		}

		if (element instanceof SagNavigatorItem) {
			SagNavigatorItem navigatorItem = (SagNavigatorItem) element;
			if (!isOwnView(navigatorItem.getView())) {
				return super.getImage(element);
			}
			return getImage(navigatorItem.getView());
		}

		return super.getImage(element);
	}

	/**
	 * @generated
	 */
	public Image getImage(View view) {
		switch (SagVisualIDRegistry.getVisualID(view)) {
		case GraphEditPart.VISUAL_ID:
			return getImage(
					"Navigator?TopLevelNode?http://supremica.org/external/sag?Graph",
					SagElementTypes.Graph_2010);
		case SensorNodeEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Node?http://supremica.org/external/sag?SensorNode",
					SagElementTypes.SensorNode_3006);
		case EndNodeEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Node?http://supremica.org/external/sag?EndNode",
					SagElementTypes.EndNode_3007);
		case ProjectEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Diagram?http://supremica.org/external/sag?Project",
					SagElementTypes.Project_1000);
		case ZoneEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?Zone",
					SagElementTypes.Zone_4010);
		default:
			return getImage("Navigator?UnknownElement", null);
		}
	}

	/**
	 * @generated
	 */
	private Image getImage(String key, IElementType elementType) {
		ImageRegistry imageRegistry = SagDiagramEditorPlugin.getInstance()
				.getImageRegistry();
		Image image = imageRegistry.get(key);
		if (image == null && elementType != null
				&& SagElementTypes.isKnownElementType(elementType)) {
			image = SagElementTypes.getImage(elementType);
			imageRegistry.put(key, image);
		}

		if (image == null) {
			image = imageRegistry.get("Navigator?ImageNotFound");
			imageRegistry.put(key, image);
		}
		return image;
	}

	/**
	 * @generated
	 */
	public String getText(Object element) {
		if (element instanceof SagNavigatorGroup) {
			SagNavigatorGroup group = (SagNavigatorGroup) element;
			return group.getGroupName();
		}

		if (element instanceof SagNavigatorItem) {
			SagNavigatorItem navigatorItem = (SagNavigatorItem) element;
			if (!isOwnView(navigatorItem.getView())) {
				return null;
			}
			return getText(navigatorItem.getView());
		}

		return super.getText(element);
	}

	/**
	 * @generated
	 */
	public String getText(View view) {
		switch (SagVisualIDRegistry.getVisualID(view)) {
		case GraphEditPart.VISUAL_ID:
			return getGraph_2010Text(view);
		case SensorNodeEditPart.VISUAL_ID:
			return getSensorNode_3006Text(view);
		case EndNodeEditPart.VISUAL_ID:
			return getEndNode_3007Text(view);
		case ProjectEditPart.VISUAL_ID:
			return getProject_1000Text(view);
		case ZoneEditPart.VISUAL_ID:
			return getZone_4010Text(view);
		default:
			return getUnknownElementText(view);
		}
	}

	/**
	 * @generated
	 */
	private String getGraph_2010Text(View view) {
		IParser parser = ParserService.getInstance().getParser(
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						if (String.class.equals(adapter)) {
							return SagVisualIDRegistry
									.getType(GraphNameEditPart.VISUAL_ID);
						}
						if (IElementType.class.equals(adapter)) {
							return SagElementTypes.Graph_2010;
						}
						return null;
					}
				});
		if (parser != null) {
			return parser.getPrintString(new EObjectAdapter(
					view.getElement() != null ? view.getElement() : view),
					ParserOptions.NONE.intValue());
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"Parser was not found for label " + 5004);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getSensorNode_3006Text(View view) {
		IParser parser = ParserService.getInstance().getParser(
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						if (String.class.equals(adapter)) {
							return SagVisualIDRegistry
									.getType(SensorNodeNameEditPart.VISUAL_ID);
						}
						if (IElementType.class.equals(adapter)) {
							return SagElementTypes.SensorNode_3006;
						}
						return null;
					}
				});
		if (parser != null) {
			return parser.getPrintString(new EObjectAdapter(
					view.getElement() != null ? view.getElement() : view),
					ParserOptions.NONE.intValue());
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"Parser was not found for label " + 5003);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getEndNode_3007Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return ((Named) domainModelElement).getName();
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 3007);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getProject_1000Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return ((Named) domainModelElement).getName();
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 1000);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getZone_4010Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return ((Named) domainModelElement).getName();
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 4010);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getUnknownElementText(View view) {
		return "<UnknownElement Visual_ID = " + view.getType() + ">";
	}

	/**
	 * @generated
	 */
	public void init(ICommonContentExtensionSite aConfig) {
	}

	/**
	 * @generated
	 */
	public void restoreState(IMemento aMemento) {
	}

	/**
	 * @generated
	 */
	public void saveState(IMemento aMemento) {
	}

	/**
	 * @generated
	 */
	public String getDescription(Object anElement) {
		return null;
	}

	/**
	 * @generated
	 */
	private boolean isOwnView(View view) {
		return ProjectEditPart.MODEL_ID.equals(SagVisualIDRegistry
				.getModelID(view));
	}

}
