package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import org.supremica.gui.WhiteScrollPane;
import java.awt.Dimension;

class EditorPanel
	extends JPanel
{
	private ModuleContainer moduleContainer;
	private String name;
	private JTabbedPane tabPanel;
	private EditorParametersPanel parametersPanel;
	private EditorEventsPanel eventsPanel;
	private EditorAliasesPanel aliasesPanel;
	private EditorComponentsPanel componentsPanel;
	private JSplitPane splitPanelHorizontal;

	EditorPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		Dimension panelPreferredSize = new Dimension(200, 300);
		Dimension panelMinimumSize = new Dimension(100, 100);
		setPreferredSize(panelPreferredSize);
		setMinimumSize(panelMinimumSize);

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
		tabPanel.setPreferredSize(panelPreferredSize);
		tabPanel.setMinimumSize(panelMinimumSize);

		aliasesPanel = new EditorAliasesPanel(moduleContainer, "Aliases");
		aliasesPanel.setPreferredSize(panelPreferredSize);
		aliasesPanel.setMinimumSize(panelMinimumSize);
		tabPanel.add(aliasesPanel);

		componentsPanel = new EditorComponentsPanel(moduleContainer, "Components");
		componentsPanel.setPreferredSize(panelPreferredSize);
		componentsPanel.setMinimumSize(panelMinimumSize);
		tabPanel.add(componentsPanel);

		eventsPanel = new EditorEventsPanel(moduleContainer, "Events");
		eventsPanel.setPreferredSize(panelPreferredSize);
		eventsPanel.setMinimumSize(panelMinimumSize);
		tabPanel.add(eventsPanel);

		parametersPanel = new EditorParametersPanel(moduleContainer, "Parameters");
		parametersPanel.setPreferredSize(panelPreferredSize);
		parametersPanel.setMinimumSize(panelMinimumSize);
		tabPanel.add(parametersPanel);

		tabPanel.setSelectedComponent(componentsPanel);

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, new JPanel());
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);

		add(splitPanelHorizontal, BorderLayout.CENTER);
	}

	public String getName()
	{
		return name;
	}

}