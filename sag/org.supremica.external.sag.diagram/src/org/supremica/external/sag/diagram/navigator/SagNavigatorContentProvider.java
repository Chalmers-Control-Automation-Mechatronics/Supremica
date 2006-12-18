package org.supremica.external.sag.diagram.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;

import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;

import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.IMemento;

import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

import org.supremica.external.sag.diagram.edit.parts.BoundedZone2EditPart;
import org.supremica.external.sag.diagram.edit.parts.BoundedZoneEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphEditPart;
import org.supremica.external.sag.diagram.edit.parts.GraphGraphCompartmentEditPart;
import org.supremica.external.sag.diagram.edit.parts.Node2EditPart;
import org.supremica.external.sag.diagram.edit.parts.NodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.ProjectEditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone2EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone3EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZone4EditPart;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZoneEditPart;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class SagNavigatorContentProvider implements ICommonContentProvider {

	/**
	 * @generated
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * @generated
	 */
	public void dispose() {
	}

	/**
	 * @generated
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * @generated
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * @generated
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFile) {
			IFile file = (IFile) parentElement;
			AdapterFactoryEditingDomain editingDomain = (AdapterFactoryEditingDomain) GMFEditingDomainFactory.INSTANCE
					.createEditingDomain();
			editingDomain.setResourceToReadOnlyMap(new HashMap() {
				public Object get(Object key) {
					if (!containsKey(key)) {
						put(key, Boolean.TRUE);
					}
					return super.get(key);
				}
			});
			ResourceSet resourceSet = editingDomain.getResourceSet();

			URI fileURI = URI.createPlatformResourceURI(file.getFullPath()
					.toString(), true);
			Resource resource = resourceSet.getResource(fileURI, true);

			Collection result = new ArrayList();
			result.addAll(createNavigatorItems(selectViewsByType(resource
					.getContents(), ProjectEditPart.MODEL_ID), file));
			return result.toArray();
		}

		if (parentElement instanceof SagNavigatorGroup) {
			SagNavigatorGroup group = (SagNavigatorGroup) parentElement;
			return group.getChildren();
		}

		if (parentElement instanceof SagNavigatorItem) {
			SagNavigatorItem navigatorItem = (SagNavigatorItem) parentElement;
			if (navigatorItem.isLeaf() || !isOwnView(navigatorItem.getView())) {
				return EMPTY_ARRAY;
			}
			return getViewChildren(navigatorItem.getView(), parentElement);
		}

		return EMPTY_ARRAY;
	}

	/**
	 * @generated
	 */
	private Object[] getViewChildren(View view, Object parentElement) {
		switch (SagVisualIDRegistry.getVisualID(view)) {
		case GraphEditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			result
					.addAll(getGraph_1001ToNode_2001Children(view,
							parentElement));
			result
					.addAll(getGraph_1001ToNode_2002Children(view,
							parentElement));
			return result.toArray();
		}
		case NodeEditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup incominglinks = new SagNavigatorGroup(
					"incoming links", "icons/incomingLinksNavigatorGroup.gif",
					parentElement);
			incominglinks.addChildren(getNode_2001ToBoundedZone_3001InSource(
					view, incominglinks));
			SagNavigatorGroup outgoinglinks = new SagNavigatorGroup(
					"outgoing links", "icons/outgoingLinksNavigatorGroup.gif",
					parentElement);
			outgoinglinks.addChildren(getNode_2001ToBoundedZone_3001OutTarget(
					view, outgoinglinks));
			incominglinks.addChildren(getNode_2001ToBoundedZone_3002InSource(
					view, incominglinks));
			outgoinglinks.addChildren(getNode_2001ToBoundedZone_3002OutTarget(
					view, outgoinglinks));
			incominglinks.addChildren(getNode_2001ToUnboundedZone_3003InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2001ToUnboundedZone_3003OutTarget(
							view, outgoinglinks));
			incominglinks.addChildren(getNode_2001ToUnboundedZone_3004InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2001ToUnboundedZone_3004OutTarget(
							view, outgoinglinks));
			incominglinks.addChildren(getNode_2001ToUnboundedZone_3005InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2001ToUnboundedZone_3005OutTarget(
							view, outgoinglinks));
			incominglinks.addChildren(getNode_2001ToUnboundedZone_3006InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2001ToUnboundedZone_3006OutTarget(
							view, outgoinglinks));
			if (!outgoinglinks.isEmpty()) {
				result.add(outgoinglinks);
			}
			if (!incominglinks.isEmpty()) {
				result.add(incominglinks);
			}
			return result.toArray();
		}
		case Node2EditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup incominglinks = new SagNavigatorGroup(
					"incoming links", "icons/incomingLinksNavigatorGroup.gif",
					parentElement);
			incominglinks.addChildren(getNode_2002ToBoundedZone_3001InSource(
					view, incominglinks));
			SagNavigatorGroup outgoinglinks = new SagNavigatorGroup(
					"outgoing links", "icons/outgoingLinksNavigatorGroup.gif",
					parentElement);
			outgoinglinks.addChildren(getNode_2002ToBoundedZone_3001OutTarget(
					view, outgoinglinks));
			incominglinks.addChildren(getNode_2002ToBoundedZone_3002InSource(
					view, incominglinks));
			outgoinglinks.addChildren(getNode_2002ToBoundedZone_3002OutTarget(
					view, outgoinglinks));
			incominglinks.addChildren(getNode_2002ToUnboundedZone_3003InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2002ToUnboundedZone_3003OutTarget(
							view, outgoinglinks));
			incominglinks.addChildren(getNode_2002ToUnboundedZone_3004InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2002ToUnboundedZone_3004OutTarget(
							view, outgoinglinks));
			incominglinks.addChildren(getNode_2002ToUnboundedZone_3005InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2002ToUnboundedZone_3005OutTarget(
							view, outgoinglinks));
			incominglinks.addChildren(getNode_2002ToUnboundedZone_3006InSource(
					view, incominglinks));
			outgoinglinks
					.addChildren(getNode_2002ToUnboundedZone_3006OutTarget(
							view, outgoinglinks));
			if (!outgoinglinks.isEmpty()) {
				result.add(outgoinglinks);
			}
			if (!incominglinks.isEmpty()) {
				result.add(incominglinks);
			}
			return result.toArray();
		}
		case ProjectEditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			result
					.addAll(getProject_79ToGraph_1001Children(view,
							parentElement));
			SagNavigatorGroup links = new SagNavigatorGroup("links",
					"icons/linksNavigatorGroup.gif", parentElement);
			links.addChildren(getProject_79ToBoundedZone_3001Children(view,
					links));
			links.addChildren(getProject_79ToBoundedZone_3002Children(view,
					links));
			links.addChildren(getProject_79ToUnboundedZone_3003Children(view,
					links));
			links.addChildren(getProject_79ToUnboundedZone_3004Children(view,
					links));
			links.addChildren(getProject_79ToUnboundedZone_3005Children(view,
					links));
			links.addChildren(getProject_79ToUnboundedZone_3006Children(view,
					links));
			if (!links.isEmpty()) {
				result.add(links);
			}
			return result.toArray();
		}
		case BoundedZoneEditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup target = new SagNavigatorGroup("target",
					"icons/linkTargetNavigatorGroup.gif", parentElement);
			target.addChildren(getBoundedZone_3001ToNode_2001OutTarget(
					(Edge) view, target));
			target.addChildren(getBoundedZone_3001ToNode_2002OutTarget(
					(Edge) view, target));
			SagNavigatorGroup source = new SagNavigatorGroup("source",
					"icons/linkSourceNavigatorGroup.gif", parentElement);
			source.addChildren(getBoundedZone_3001ToNode_2001InSource(
					(Edge) view, source));
			source.addChildren(getBoundedZone_3001ToNode_2002InSource(
					(Edge) view, source));
			if (!source.isEmpty()) {
				result.add(source);
			}
			if (!target.isEmpty()) {
				result.add(target);
			}
			return result.toArray();
		}
		case BoundedZone2EditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup target = new SagNavigatorGroup("target",
					"icons/linkTargetNavigatorGroup.gif", parentElement);
			target.addChildren(getBoundedZone_3002ToNode_2001OutTarget(
					(Edge) view, target));
			target.addChildren(getBoundedZone_3002ToNode_2002OutTarget(
					(Edge) view, target));
			SagNavigatorGroup source = new SagNavigatorGroup("source",
					"icons/linkSourceNavigatorGroup.gif", parentElement);
			source.addChildren(getBoundedZone_3002ToNode_2001InSource(
					(Edge) view, source));
			source.addChildren(getBoundedZone_3002ToNode_2002InSource(
					(Edge) view, source));
			if (!source.isEmpty()) {
				result.add(source);
			}
			if (!target.isEmpty()) {
				result.add(target);
			}
			return result.toArray();
		}
		case UnboundedZoneEditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup target = new SagNavigatorGroup("target",
					"icons/linkTargetNavigatorGroup.gif", parentElement);
			target.addChildren(getUnboundedZone_3003ToNode_2001OutTarget(
					(Edge) view, target));
			target.addChildren(getUnboundedZone_3003ToNode_2002OutTarget(
					(Edge) view, target));
			SagNavigatorGroup source = new SagNavigatorGroup("source",
					"icons/linkSourceNavigatorGroup.gif", parentElement);
			source.addChildren(getUnboundedZone_3003ToNode_2001InSource(
					(Edge) view, source));
			source.addChildren(getUnboundedZone_3003ToNode_2002InSource(
					(Edge) view, source));
			if (!source.isEmpty()) {
				result.add(source);
			}
			if (!target.isEmpty()) {
				result.add(target);
			}
			return result.toArray();
		}
		case UnboundedZone2EditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup target = new SagNavigatorGroup("target",
					"icons/linkTargetNavigatorGroup.gif", parentElement);
			target.addChildren(getUnboundedZone_3004ToNode_2001OutTarget(
					(Edge) view, target));
			target.addChildren(getUnboundedZone_3004ToNode_2002OutTarget(
					(Edge) view, target));
			SagNavigatorGroup source = new SagNavigatorGroup("source",
					"icons/linkSourceNavigatorGroup.gif", parentElement);
			source.addChildren(getUnboundedZone_3004ToNode_2001InSource(
					(Edge) view, source));
			source.addChildren(getUnboundedZone_3004ToNode_2002InSource(
					(Edge) view, source));
			if (!source.isEmpty()) {
				result.add(source);
			}
			if (!target.isEmpty()) {
				result.add(target);
			}
			return result.toArray();
		}
		case UnboundedZone3EditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup target = new SagNavigatorGroup("target",
					"icons/linkTargetNavigatorGroup.gif", parentElement);
			target.addChildren(getUnboundedZone_3005ToNode_2001OutTarget(
					(Edge) view, target));
			target.addChildren(getUnboundedZone_3005ToNode_2002OutTarget(
					(Edge) view, target));
			SagNavigatorGroup source = new SagNavigatorGroup("source",
					"icons/linkSourceNavigatorGroup.gif", parentElement);
			source.addChildren(getUnboundedZone_3005ToNode_2001InSource(
					(Edge) view, source));
			source.addChildren(getUnboundedZone_3005ToNode_2002InSource(
					(Edge) view, source));
			if (!source.isEmpty()) {
				result.add(source);
			}
			if (!target.isEmpty()) {
				result.add(target);
			}
			return result.toArray();
		}
		case UnboundedZone4EditPart.VISUAL_ID: {
			Collection result = new ArrayList();
			SagNavigatorGroup target = new SagNavigatorGroup("target",
					"icons/linkTargetNavigatorGroup.gif", parentElement);
			target.addChildren(getUnboundedZone_3006ToNode_2001OutTarget(
					(Edge) view, target));
			target.addChildren(getUnboundedZone_3006ToNode_2002OutTarget(
					(Edge) view, target));
			SagNavigatorGroup source = new SagNavigatorGroup("source",
					"icons/linkSourceNavigatorGroup.gif", parentElement);
			source.addChildren(getUnboundedZone_3006ToNode_2001InSource(
					(Edge) view, source));
			source.addChildren(getUnboundedZone_3006ToNode_2002InSource(
					(Edge) view, source));
			if (!source.isEmpty()) {
				result.add(source);
			}
			if (!target.isEmpty()) {
				result.add(target);
			}
			return result.toArray();
		}
		}
		return EMPTY_ARRAY;
	}

	/**
	 * @generated
	 */
	public Object getParent(Object element) {
		if (element instanceof SagAbstractNavigatorItem) {
			SagAbstractNavigatorItem abstractNavigatorItem = (SagAbstractNavigatorItem) element;
			return abstractNavigatorItem.getParent();
		}
		return null;
	}

	/**
	 * @generated
	 */
	public boolean hasChildren(Object element) {
		return element instanceof IFile || getChildren(element).length > 0;
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
	private Collection getNode_2002ToUnboundedZone_3004InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3004InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3004InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToBoundedZone_3002InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToBoundedZone_3002InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToBoundedZone_3002InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToBoundedZone_3001Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getDiagramLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToBoundedZone_3001ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToBoundedZone_3001ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3002ToNode_2001InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3002ToNode_2001InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3002ToNode_2001InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3003OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3003OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3003OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToUnboundedZone_3005Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getDiagramLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone3EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToUnboundedZone_3005ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToUnboundedZone_3005ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3005InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone3EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3005InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3005InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3005ToNode_2001OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3005ToNode_2001OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3005ToNode_2001OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3003OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3003OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3003OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3001ToNode_2002OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3001ToNode_2002OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3001ToNode_2002OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getGraph_1001ToNode_2002Children(View view, Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getChildrenByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(GraphGraphCompartmentEditPart.VISUAL_ID));
		connectedViews = getChildrenByType(connectedViews, SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isGraph_1001ToNode_2002ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isGraph_1001ToNode_2002ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3006InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone4EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3006InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3006InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToBoundedZone_3001OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToBoundedZone_3001OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToBoundedZone_3001OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3006ToNode_2002OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3006ToNode_2002OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3006ToNode_2002OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3005OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone3EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3005OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3005OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3005ToNode_2001InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3005ToNode_2001InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3005ToNode_2001InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToBoundedZone_3002OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToBoundedZone_3002OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToBoundedZone_3002OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3006ToNode_2001InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3006ToNode_2001InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3006ToNode_2001InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3004ToNode_2002InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3004ToNode_2002InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3004ToNode_2002InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3005ToNode_2002OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3005ToNode_2002OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3005ToNode_2002OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3002ToNode_2001OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3002ToNode_2001OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3002ToNode_2001OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToBoundedZone_3002Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getDiagramLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToBoundedZone_3002ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToBoundedZone_3002ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToUnboundedZone_3004Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getDiagramLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToUnboundedZone_3004ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToUnboundedZone_3004ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3004InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3004InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3004InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3003ToNode_2002InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3003ToNode_2002InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3003ToNode_2002InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getGraph_1001ToNode_2001Children(View view, Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getChildrenByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(GraphGraphCompartmentEditPart.VISUAL_ID));
		connectedViews = getChildrenByType(connectedViews, SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isGraph_1001ToNode_2001ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isGraph_1001ToNode_2001ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3004ToNode_2001OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3004ToNode_2001OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3004ToNode_2001OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3006ToNode_2001OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3006ToNode_2001OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3006ToNode_2001OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToBoundedZone_3001InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToBoundedZone_3001InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToBoundedZone_3001InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3004ToNode_2002OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3004ToNode_2002OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3004ToNode_2002OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3003InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3003InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3003InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3001ToNode_2002InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3001ToNode_2002InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3001ToNode_2002InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3002ToNode_2002InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3002ToNode_2002InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3002ToNode_2002InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToBoundedZone_3001InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToBoundedZone_3001InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToBoundedZone_3001InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3001ToNode_2001OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3001ToNode_2001OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3001ToNode_2001OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3006ToNode_2002InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3006ToNode_2002InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3006ToNode_2002InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3006OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone4EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3006OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3006OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3004OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3004OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3004OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3005OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone3EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3005OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3005OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToUnboundedZone_3003Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getDiagramLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToUnboundedZone_3003ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToUnboundedZone_3003ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3006OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone4EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3006OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3006OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3001ToNode_2001InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3001ToNode_2001InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3001ToNode_2001InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3003ToNode_2001OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3003ToNode_2001OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3003ToNode_2001OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToBoundedZone_3002InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToBoundedZone_3002InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToBoundedZone_3002InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3005InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone3EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3005InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3005InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3005ToNode_2002InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3005ToNode_2002InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3005ToNode_2002InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3003ToNode_2002OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3003ToNode_2002OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3003ToNode_2002OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToBoundedZone_3002OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToBoundedZone_3002OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToBoundedZone_3002OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToUnboundedZone_3006Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getDiagramLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone4EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToUnboundedZone_3006ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToUnboundedZone_3006ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3003ToNode_2001InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3003ToNode_2001InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3003ToNode_2001InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3003InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3003InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3003InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToUnboundedZone_3004OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToUnboundedZone_3004OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToUnboundedZone_3004OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getProject_79ToGraph_1001Children(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getChildrenByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(GraphEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isProject_79ToGraph_1001ChildrenLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isProject_79ToGraph_1001ChildrenLeaf(View view) {
		return false;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2001ToUnboundedZone_3006InSource(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getIncomingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(UnboundedZone4EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2001ToUnboundedZone_3006InSourceLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2001ToUnboundedZone_3006InSourceLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getUnboundedZone_3004ToNode_2001InSource(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksSourceByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(NodeEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isUnboundedZone_3004ToNode_2001InSourceLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isUnboundedZone_3004ToNode_2001InSourceLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getNode_2002ToBoundedZone_3001OutTarget(View view,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getOutgoingLinksByType(Collections
				.singleton(view), SagVisualIDRegistry
				.getType(BoundedZoneEditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isNode_2002ToBoundedZone_3001OutTargetLeaf(view));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isNode_2002ToBoundedZone_3001OutTargetLeaf(View view) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getBoundedZone_3002ToNode_2002OutTarget(Edge edge,
			Object parent) {
		Collection result = new ArrayList();
		Collection connectedViews = getLinksTargetByType(Collections
				.singleton(edge), SagVisualIDRegistry
				.getType(Node2EditPart.VISUAL_ID));
		createNavigatorItems(connectedViews, parent, result,
				isBoundedZone_3002ToNode_2002OutTargetLeaf(edge));
		return result;
	}

	/**
	 * @generated
	 */
	private boolean isBoundedZone_3002ToNode_2002OutTargetLeaf(Edge edge) {
		return true;
	}

	/**
	 * @generated
	 */
	private Collection getLinksSourceByType(Collection edges, String type) {
		Collection result = new ArrayList();
		for (Iterator it = edges.iterator(); it.hasNext();) {
			Edge nextEdge = (Edge) it.next();
			View nextEdgeSource = nextEdge.getSource();
			if (type.equals(nextEdgeSource.getType())
					&& !isOwnView(nextEdgeSource)) {
				result.add(nextEdgeSource);
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection getLinksTargetByType(Collection edges, String type) {
		Collection result = new ArrayList();
		for (Iterator it = edges.iterator(); it.hasNext();) {
			Edge nextEdge = (Edge) it.next();
			View nextEdgeSource = nextEdge.getTarget();
			if (type.equals(nextEdgeSource.getType())
					&& !isOwnView(nextEdgeSource)) {
				result.add(nextEdgeSource);
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection getOutgoingLinksByType(Collection nodes, String type) {
		Collection result = new ArrayList();
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			View nextNode = (View) it.next();
			result.addAll(selectViewsByType(nextNode.getSourceEdges(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection getIncomingLinksByType(Collection nodes, String type) {
		Collection result = new ArrayList();
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			View nextNode = (View) it.next();
			result.addAll(selectViewsByType(nextNode.getTargetEdges(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection getChildrenByType(Collection nodes, String type) {
		Collection result = new ArrayList();
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			View nextNode = (View) it.next();
			result.addAll(selectViewsByType(nextNode.getChildren(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection getDiagramLinksByType(Collection diagrams, String type) {
		Collection result = new ArrayList();
		for (Iterator it = diagrams.iterator(); it.hasNext();) {
			Diagram nextDiagram = (Diagram) it.next();
			result.addAll(selectViewsByType(nextDiagram.getEdges(), type));
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection selectViewsByType(Collection views, String type) {
		Collection result = new ArrayList();
		for (Iterator it = views.iterator(); it.hasNext();) {
			View nextView = (View) it.next();
			if (type.equals(nextView.getType()) && isOwnView(nextView)) {
				result.add(nextView);
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	private Collection createNavigatorItems(Collection views, Object parent) {
		Collection result = new ArrayList();
		createNavigatorItems(views, parent, result, false);
		return result;
	}

	/**
	 * @generated
	 */
	private void createNavigatorItems(Collection views, Object parent,
			Collection result, boolean isLeafs) {
		for (Iterator it = views.iterator(); it.hasNext();) {
			result.add(new SagNavigatorItem((View) it.next(), parent, isLeafs));
		}
	}

	/**
	 * @generated
	 */
	private boolean isOwnView(View view) {
		return ProjectEditPart.MODEL_ID.equals(SagVisualIDRegistry
				.getModelID(view));
	}

}
