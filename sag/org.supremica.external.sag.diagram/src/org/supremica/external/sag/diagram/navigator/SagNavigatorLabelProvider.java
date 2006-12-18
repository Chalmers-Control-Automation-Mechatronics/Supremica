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
import org.supremica.external.sag.Node;
import org.supremica.external.sag.Zone;

import org.supremica.external.sag.diagram.edit.parts.BoundedZone2EditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacity2EditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneCapacityEditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphNameEditPart;
import org.supremica.external.sag.diagram.edit.parts.Node2EditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeSensorEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone2EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone3EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone4EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZoneEditPart;

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
					SagElementTypes.Graph_1001);
		case NodeEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Node?http://supremica.org/external/sag?Node",
					SagElementTypes.Node_2001);
		case Node2EditPart.VISUAL_ID:
			return getImage(
					"Navigator?Node?http://supremica.org/external/sag?Node",
					SagElementTypes.Node_2002);
		case ProjectEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Diagram?http://supremica.org/external/sag?Project",
					SagElementTypes.Project_79);
		case BoundedZoneEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?BoundedZone",
					SagElementTypes.BoundedZone_3001);
		case BoundedZone2EditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?BoundedZone",
					SagElementTypes.BoundedZone_3002);
		case UnboundedZoneEditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?UnboundedZone",
					SagElementTypes.UnboundedZone_3003);
		case UnboundedZone2EditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?UnboundedZone",
					SagElementTypes.UnboundedZone_3004);
		case UnboundedZone3EditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?UnboundedZone",
					SagElementTypes.UnboundedZone_3005);
		case UnboundedZone4EditPart.VISUAL_ID:
			return getImage(
					"Navigator?Link?http://supremica.org/external/sag?UnboundedZone",
					SagElementTypes.UnboundedZone_3006);
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
			return getGraph_1001Text(view);
		case NodeEditPart.VISUAL_ID:
			return getNode_2001Text(view);
		case Node2EditPart.VISUAL_ID:
			return getNode_2002Text(view);
		case ProjectEditPart.VISUAL_ID:
			return getProject_79Text(view);
		case BoundedZoneEditPart.VISUAL_ID:
			return getBoundedZone_3001Text(view);
		case BoundedZone2EditPart.VISUAL_ID:
			return getBoundedZone_3002Text(view);
		case UnboundedZoneEditPart.VISUAL_ID:
			return getUnboundedZone_3003Text(view);
		case UnboundedZone2EditPart.VISUAL_ID:
			return getUnboundedZone_3004Text(view);
		case UnboundedZone3EditPart.VISUAL_ID:
			return getUnboundedZone_3005Text(view);
		case UnboundedZone4EditPart.VISUAL_ID:
			return getUnboundedZone_3006Text(view);
		default:
			return getUnknownElementText(view);
		}
	}

	/**
	 * @generated
	 */
	private String getGraph_1001Text(View view) {
		IParser parser = ParserService.getInstance().getParser(
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						if (String.class.equals(adapter)) {
							return SagVisualIDRegistry
									.getType(GraphNameEditPart.VISUAL_ID);
						}
						if (IElementType.class.equals(adapter)) {
							return SagElementTypes.Graph_1001;
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
					"Parser was not found for label " + 4002);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getNode_2001Text(View view) {
		IParser parser = ParserService.getInstance().getParser(
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						if (String.class.equals(adapter)) {
							return SagVisualIDRegistry
									.getType(NodeSensorEditPart.VISUAL_ID);
						}
						if (IElementType.class.equals(adapter)) {
							return SagElementTypes.Node_2001;
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
					"Parser was not found for label " + 4001);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getNode_2002Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return ((Node) domainModelElement).getSensor();
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 2002);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getProject_79Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return ((Named) domainModelElement).getName();
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 79);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getBoundedZone_3001Text(View view) {
		IParser parser = ParserService.getInstance().getParser(
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						if (String.class.equals(adapter)) {
							return SagVisualIDRegistry
									.getType(BoundedZoneCapacityEditPart.VISUAL_ID);
						}
						if (IElementType.class.equals(adapter)) {
							return SagElementTypes.BoundedZone_3001;
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
					"Parser was not found for label " + 4003);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getBoundedZone_3002Text(View view) {
		IParser parser = ParserService.getInstance().getParser(
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						if (String.class.equals(adapter)) {
							return SagVisualIDRegistry
									.getType(BoundedZoneCapacity2EditPart.VISUAL_ID);
						}
						if (IElementType.class.equals(adapter)) {
							return SagElementTypes.BoundedZone_3002;
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
					"Parser was not found for label " + 4004);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getUnboundedZone_3003Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return String.valueOf(((Zone) domainModelElement).isIsOneway());
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 3003);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getUnboundedZone_3004Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return String.valueOf(((Zone) domainModelElement).isIsOneway());
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 3004);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getUnboundedZone_3005Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return String.valueOf(((Zone) domainModelElement).isIsOneway());
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 3005);
			return "";
		}
	}

	/**
	 * @generated
	 */
	private String getUnboundedZone_3006Text(View view) {
		EObject domainModelElement = view.getElement();
		if (domainModelElement != null) {
			return String.valueOf(((Zone) domainModelElement).isIsOneway());
		} else {
			SagDiagramEditorPlugin.getInstance().logError(
					"No domain element for view with visualID = " + 3006);
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
