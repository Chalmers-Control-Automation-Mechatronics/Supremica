package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.BorderLayout;
import org.supremica.gui.WhiteScrollPane;

class EditorPanel
	extends JPanel
{
	private IDE ide;
	private String title;
	private JTabbedPane tabPanel;
	private EditorParametersPanel parametersPanel;
	private EditorEventsPanel eventsPanel;
	private EditorAliasesPanel aliasesPanel;
	private EditorComponentsPanel componentsPanel;
	private JSplitPane splitPanelHorizontal;

	EditorPanel(IDE ide, String title)
	{
		this.ide = ide;
		this.title = title;

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);

		parametersPanel = new EditorParametersPanel(ide, "Parameters");
		tabPanel.add(parametersPanel.getTitle(), parametersPanel);

		eventsPanel = new EditorEventsPanel(ide, "Events");
		tabPanel.add(eventsPanel.getTitle(), eventsPanel);

		aliasesPanel = new EditorAliasesPanel(ide, "Aliases");
		tabPanel.add(aliasesPanel.getTitle(), aliasesPanel);

		componentsPanel = new EditorComponentsPanel(ide, "Components");
		tabPanel.add(componentsPanel.getTitle(), componentsPanel);

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, new WhiteScrollPane());
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