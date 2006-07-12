package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.GridBagLayout;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.subject.module.ModuleSubject;


public class EditorPanel
	extends MainPanel
{
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabPanel;
	private JComponent componentEditorPanel;

	private EditorParametersPanel parametersPanel;
	private EditorEventsPanel eventsPanel;
	private EditorAliasesPanel aliasesPanel;
	private EditorComponentsPanel componentsPanel;
	private ButtonGroup editorButtonGroup;

	public EditorPanel(ModuleContainer moduleContainer, String name)
	{
		super(moduleContainer, name);

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
		tabPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		tabPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);

/*
		aliasesPanel = new EditorAliasesPanel(moduleContainer, "Aliases");
		aliasesPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		aliasesPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
		tabPanel.add(aliasesPanel);
*/
		componentsPanel = new EditorComponentsPanel(moduleContainer, "Components");
		componentsPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		componentsPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
		tabPanel.add(componentsPanel);

		eventsPanel = new EditorEventsPanel(moduleContainer, "Events");
		eventsPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		eventsPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
		tabPanel.add(eventsPanel);

/*
		parametersPanel = new EditorParametersPanel(moduleContainer, "Parameters");
		parametersPanel.setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		parametersPanel.setMinimumSize(IDEDimensions.leftEditorMinimumSize);
		tabPanel.add(parametersPanel);
*/
		tabPanel.setSelectedComponent(componentsPanel);

		componentEditorPanel = getEmptyRightPanel();

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, componentEditorPanel);
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);

		((GridBagLayout)getLayout()).setConstraints(splitPanelHorizontal, getGridBagConstraints());
		add(splitPanelHorizontal);
	}

	public void addToolBarEntries(IDEToolBar toolBar)
	{
		editorButtonGroup = new ButtonGroup();
		toolBar.addSeparator();
		(toolBar.add(getActions().editorSelectAction, editorButtonGroup)).setSelected(true);
		toolBar.setCommand(ControlledSurface.SELECT);
		toolBar.add(getActions().editorAddNodeAction, editorButtonGroup);
		toolBar.add(getActions().editorAddNodeGroupAction, editorButtonGroup);
		toolBar.add(getActions().editorAddEdgeAction, editorButtonGroup);
//		toolBar.add(getActions().editorAddEventAction, editorButtonGroup);
	}

	public void disablePanel()
	{
		//getActions().enableEditorActions(false);
	}

	public void enablePanel()
	{
		//getActions().enableEditorActions(true);
	}

	public EditorWindowInterface getActiveEditorWindowInterface()
	{
	    if (getRightComponent() instanceof EditorWindowInterface)
	    {
			return (EditorWindowInterface)getRightComponent();
	    }
	    return null;
    //	    return getEditorWindowInterface();
	}

	public EditorPanelInterface getEditorPanelInterface()
	{
		return new EditorPanelInterfaceImpl();
	}

	class EditorPanelInterfaceImpl
		implements EditorPanelInterface
		{
			public void addComponent()
			{
				componentsPanel.addComponent();
			}

			public void addComponent(AbstractSubject component)
			{
				componentsPanel.addComponent(component);
			}

			public void addEvent()
			{
				eventsPanel.addEvent();
			}

			public DefaultListModel getEventDataList()
			{
				return eventsPanel.getEventDataList();
			}

			public ModuleSubject getModuleSubject()
			{
				return componentsPanel.getModuleSubject();
			}

		}

}
