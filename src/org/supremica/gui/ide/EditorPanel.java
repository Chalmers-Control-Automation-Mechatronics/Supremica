package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import org.supremica.gui.WhiteScrollPane;

class EditorPanel
	extends JPanel
{
	private ModuleContainer moduleContainer;
	private String title;
	private JTabbedPane tabPanel;
	private EditorParametersPanel parametersPanel;
	private EditorEventsPanel eventsPanel;
	private EditorAliasesPanel aliasesPanel;
	private EditorComponentsPanel componentsPanel;
	private JSplitPane splitPanelHorizontal;

	EditorPanel(ModuleContainer moduleContainer, String title)
	{
		this.moduleContainer = moduleContainer;
		this.title = title;

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);

		parametersPanel = new EditorParametersPanel(moduleContainer, "Parameters");
		tabPanel.add(parametersPanel.getTitle(), parametersPanel);

		eventsPanel = new EditorEventsPanel(moduleContainer, "Events");
		tabPanel.add(eventsPanel.getTitle(), eventsPanel);

		aliasesPanel = new EditorAliasesPanel(moduleContainer, "Events");
		tabPanel.add(aliasesPanel.getTitle(), aliasesPanel);

		componentsPanel = new EditorComponentsPanel(moduleContainer, "Components");
		tabPanel.add(componentsPanel.getTitle(), componentsPanel);

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, new JPanel());
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);

		add(splitPanelHorizontal, BorderLayout.CENTER);
	}

	public String getTitle()
	{
		return title;
	}

}