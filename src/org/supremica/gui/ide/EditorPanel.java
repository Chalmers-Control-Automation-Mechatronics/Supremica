package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import org.supremica.gui.WhiteScrollPane;

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

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);

		aliasesPanel = new EditorAliasesPanel(moduleContainer, "Aliases");
		tabPanel.add(aliasesPanel);

		componentsPanel = new EditorComponentsPanel(moduleContainer, "Components");
		tabPanel.add(componentsPanel);

		eventsPanel = new EditorEventsPanel(moduleContainer, "Events");
		tabPanel.add(eventsPanel);

		parametersPanel = new EditorParametersPanel(moduleContainer, "Parameters");
		tabPanel.add(parametersPanel);

		tabPanel.setSelectedComponent(componentsPanel);

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, new JPanel());
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);

		add(splitPanelHorizontal, BorderLayout.CENTER);
	}

	public String getName()
	{
		return name;
	}

}