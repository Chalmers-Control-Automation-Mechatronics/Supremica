package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import org.supremica.gui.WhiteScrollPane;
import java.awt.Dimension;

class EditorPanel
	extends JPanel
{
	private ModuleContainer moduleContainer;
	private String name;
	private JTabbedPane tabPanel;
	private JPanel componentEditorPanel;
	private JPanel emptyComponentEditorPanel;

	private EditorParametersPanel parametersPanel;
	private EditorEventsPanel eventsPanel;
	private EditorAliasesPanel aliasesPanel;
	private EditorComponentsPanel componentsPanel;
	private JSplitPane splitPanelHorizontal;
	private Dimension panelPreferredSize;
	private Dimension panelMinimumSize;
	private int preferredHeight = 400;

	EditorPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		panelPreferredSize = new Dimension(250, preferredHeight);
		panelMinimumSize = new Dimension(100, 100);
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

		emptyComponentEditorPanel = new EmptyComponentEditorPanel();
		componentEditorPanel = emptyComponentEditorPanel;

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, componentEditorPanel);
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);

		// add(splitPanelHorizontal, BorderLayout.CENTER);


		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.NORTH;

		setLayout(gridbag);

		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;

		gridbag.setConstraints(splitPanelHorizontal, constraints);
		add(splitPanelHorizontal);


	}

	public String getName()
	{
		return name;
	}


	public void setActiveComponentEditorPanel(JPanel currPanel)
	{
		System.err.println("setActiveComponentEditorPanel");

		Component oldPanel = splitPanelHorizontal.getRightComponent();
		if (oldPanel != currPanel)
		{
			System.err.println("Switching panel");

			if (oldPanel != null)
			{
				splitPanelHorizontal.remove(oldPanel);
			}

			if (currPanel == null)
			{
				splitPanelHorizontal.setRightComponent(emptyComponentEditorPanel);
			}
			else
			{
				splitPanelHorizontal.setRightComponent(currPanel);
				System.err.println("setRightPanel");
			}
		}

		validate();

	}

 	class EmptyComponentEditorPanel
 		extends JPanel
 	{
		public EmptyComponentEditorPanel()
		{
			setPreferredSize(new Dimension(600, preferredHeight));
			setMinimumSize(panelMinimumSize);
		}
	}
}