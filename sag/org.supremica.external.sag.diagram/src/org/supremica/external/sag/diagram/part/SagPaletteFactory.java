package org.supremica.external.sag.diagram.part;

import java.util.List;
import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import java.util.ArrayList;

import org.eclipse.gef.palette.PaletteGroup;

import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeConnectionTool;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeCreationTool;

import org.supremica.external.sag.diagram.providers.SagElementTypes;

/**
 * @generated
 */
public class SagPaletteFactory {

	/**
	 * @generated
	 */
	public void fillPalette(PaletteRoot paletteRoot) {
		paletteRoot.add(createGraphs1Group());
		paletteRoot.add(createNodes2Group());
		paletteRoot.add(createZones3Group());
	}

	/**
	 * Creates "Graphs" palette tool group
	 * @generated
	 */
	private PaletteContainer createGraphs1Group() {
		PaletteGroup paletteContainer = new PaletteGroup(SagDiagramEditorPlugin
				.getString("Graphs1Group.title")); //$NON-NLS-1$
		paletteContainer.add(createGraph1CreationTool());
		return paletteContainer;
	}

	/**
	 * Creates "Nodes" palette tool group
	 * @generated
	 */
	private PaletteContainer createNodes2Group() {
		PaletteGroup paletteContainer = new PaletteGroup(SagDiagramEditorPlugin
				.getString("Nodes2Group.title")); //$NON-NLS-1$
		paletteContainer.add(createSensor1CreationTool());
		paletteContainer.add(createEndNode2CreationTool());
		return paletteContainer;
	}

	/**
	 * Creates "Zones" palette tool group
	 * @generated
	 */
	private PaletteContainer createZones3Group() {
		PaletteGroup paletteContainer = new PaletteGroup(SagDiagramEditorPlugin
				.getString("Zones3Group.title")); //$NON-NLS-1$
		paletteContainer.add(createZone1CreationTool());
		return paletteContainer;
	}

	/**
	 * @generated
	 */
	private ToolEntry createGraph1CreationTool() {
		ImageDescriptor smallImage;
		ImageDescriptor largeImage;

		smallImage = SagElementTypes
				.getImageDescriptor(SagElementTypes.Graph_1001);

		largeImage = smallImage;

		final List elementTypes = new ArrayList();
		elementTypes.add(SagElementTypes.Graph_1001);
		ToolEntry result = new NodeToolEntry(
				SagDiagramEditorPlugin.getString("Graph1CreationTool.title"), SagDiagramEditorPlugin.getString("Graph1CreationTool.desc"), smallImage, largeImage, elementTypes); //$NON-NLS-1$ $NON-NLS-2$

		return result;
	}

	/**
	 * @generated
	 */
	private ToolEntry createSensor1CreationTool() {
		ImageDescriptor smallImage;
		ImageDescriptor largeImage;

		smallImage = SagElementTypes
				.getImageDescriptor(SagElementTypes.Node_2001);

		largeImage = smallImage;

		final List elementTypes = new ArrayList();
		elementTypes.add(SagElementTypes.Node_2001);
		ToolEntry result = new NodeToolEntry(
				SagDiagramEditorPlugin.getString("Sensor1CreationTool.title"), SagDiagramEditorPlugin.getString("Sensor1CreationTool.desc"), smallImage, largeImage, elementTypes); //$NON-NLS-1$ $NON-NLS-2$

		return result;
	}

	/**
	 * @generated
	 */
	private ToolEntry createEndNode2CreationTool() {
		ImageDescriptor smallImage;
		ImageDescriptor largeImage;

		smallImage = SagElementTypes
				.getImageDescriptor(SagElementTypes.Node_2002);

		largeImage = smallImage;

		final List elementTypes = new ArrayList();
		elementTypes.add(SagElementTypes.Node_2002);
		ToolEntry result = new NodeToolEntry(
				SagDiagramEditorPlugin.getString("EndNode2CreationTool.title"), SagDiagramEditorPlugin.getString("EndNode2CreationTool.desc"), smallImage, largeImage, elementTypes); //$NON-NLS-1$ $NON-NLS-2$

		return result;
	}

	/**
	 * @generated
	 */
	private ToolEntry createZone1CreationTool() {
		ImageDescriptor smallImage;
		ImageDescriptor largeImage;

		smallImage = SagElementTypes
				.getImageDescriptor(SagElementTypes.BoundedZone_3001);

		largeImage = smallImage;

		final List relationshipTypes = new ArrayList();
		relationshipTypes.add(SagElementTypes.BoundedZone_3001);
		relationshipTypes.add(SagElementTypes.BoundedZone_3002);
		relationshipTypes.add(SagElementTypes.UnboundedZone_3003);
		relationshipTypes.add(SagElementTypes.UnboundedZone_3004);
		relationshipTypes.add(SagElementTypes.UnboundedZone_3005);
		relationshipTypes.add(SagElementTypes.UnboundedZone_3006);
		ToolEntry result = new LinkToolEntry(
				SagDiagramEditorPlugin.getString("Zone1CreationTool.title"), SagDiagramEditorPlugin.getString("Zone1CreationTool.desc"), smallImage, largeImage, relationshipTypes); //$NON-NLS-1$ $NON-NLS-2$

		return result;
	}

	/**
	 * @generated
	 */
	private static class NodeToolEntry extends ToolEntry {

		/**
		 * @generated
		 */
		private final List elementTypes;

		/**
		 * @generated
		 */
		private NodeToolEntry(String title, String description,
				ImageDescriptor smallIcon, ImageDescriptor largeIcon,
				List elementTypes) {
			super(title, description, smallIcon, largeIcon);
			this.elementTypes = elementTypes;
		}

		/**
		 * @generated
		 */
		public Tool createTool() {
			Tool tool = new UnspecifiedTypeCreationTool(elementTypes);
			tool.setProperties(getToolProperties());
			return tool;
		}
	}

	/**
	 * @generated
	 */
	private static class LinkToolEntry extends ToolEntry {

		/**
		 * @generated
		 */
		private final List relationshipTypes;

		/**
		 * @generated
		 */
		private LinkToolEntry(String title, String description,
				ImageDescriptor smallIcon, ImageDescriptor largeIcon,
				List relationshipTypes) {
			super(title, description, smallIcon, largeIcon);
			this.relationshipTypes = relationshipTypes;
		}

		/**
		 * @generated
		 */
		public Tool createTool() {
			Tool tool = new UnspecifiedTypeConnectionTool(relationshipTypes);
			tool.setProperties(getToolProperties());
			return tool;
		}
	}
}
