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
		List/*<IElementType>*/types = new ArrayList/*<IElementType>*/(1);
		types.add(SagElementTypes.Graph_2010);
		NodeToolEntry entry = new NodeToolEntry(
				SagDiagramEditorPlugin.getString("Graph1CreationTool.title"), SagDiagramEditorPlugin.getString("Graph1CreationTool.desc"), types); //$NON-NLS-1$ $NON-NLS-2$
		entry.setSmallIcon(SagElementTypes
				.getImageDescriptor(SagElementTypes.Graph_2010));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createSensor1CreationTool() {
		List/*<IElementType>*/types = new ArrayList/*<IElementType>*/(1);
		types.add(SagElementTypes.SensorNode_3006);
		NodeToolEntry entry = new NodeToolEntry(
				SagDiagramEditorPlugin.getString("Sensor1CreationTool.title"), SagDiagramEditorPlugin.getString("Sensor1CreationTool.desc"), types); //$NON-NLS-1$ $NON-NLS-2$
		entry.setSmallIcon(SagElementTypes
				.getImageDescriptor(SagElementTypes.SensorNode_3006));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createEndNode2CreationTool() {
		List/*<IElementType>*/types = new ArrayList/*<IElementType>*/(1);
		types.add(SagElementTypes.EndNode_3007);
		NodeToolEntry entry = new NodeToolEntry(
				SagDiagramEditorPlugin.getString("EndNode2CreationTool.title"), SagDiagramEditorPlugin.getString("EndNode2CreationTool.desc"), types); //$NON-NLS-1$ $NON-NLS-2$
		entry.setSmallIcon(SagElementTypes
				.getImageDescriptor(SagElementTypes.EndNode_3007));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createZone1CreationTool() {
		List/*<IElementType>*/types = new ArrayList/*<IElementType>*/(2);
		types.add(SagElementTypes.BoundedZone_4007);
		types.add(SagElementTypes.UnboundedZone_4009);
		LinkToolEntry entry = new LinkToolEntry(
				SagDiagramEditorPlugin.getString("Zone1CreationTool.title"), SagDiagramEditorPlugin.getString("Zone1CreationTool.desc"), types); //$NON-NLS-1$ $NON-NLS-2$
		entry.setSmallIcon(SagElementTypes
				.getImageDescriptor(SagElementTypes.BoundedZone_4007));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
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
				List elementTypes) {
			super(title, description, null, null);
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
				List relationshipTypes) {
			super(title, description, null, null);
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
