package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import org.supremica.gui.WhiteScrollPane;
import java.awt.Dimension;

class AnalyzerPanel
	extends JPanel
{
	private ModuleContainer moduleContainer;
	private String name;
	private JTabbedPane tabPanel;
	private JPanel automatonViewerPanel;
	private JPanel emptyAutomatonViewerPanel;

	private AnalyzerAutomataPanel automataPanel;
	private JSplitPane splitPanelHorizontal;
	private Dimension panelPreferredSize;
	private Dimension panelMinimumSize;
	private int preferredHeight = 400;

	AnalyzerPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		panelPreferredSize = new Dimension(500, preferredHeight);
		panelMinimumSize = new Dimension(100, 100);
		setPreferredSize(panelPreferredSize);
		setMinimumSize(panelMinimumSize);

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
		tabPanel.setPreferredSize(panelPreferredSize);
		tabPanel.setMinimumSize(panelMinimumSize);

		automataPanel = new AnalyzerAutomataPanel(this, moduleContainer, "All");
		automataPanel.setPreferredSize(panelPreferredSize);
		automataPanel.setMinimumSize(panelMinimumSize);
		tabPanel.add(automataPanel);

		tabPanel.setSelectedComponent(automataPanel);

		emptyAutomatonViewerPanel = new EmptyAutomatonViewerPanel();
		automatonViewerPanel = emptyAutomatonViewerPanel;

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, automatonViewerPanel);
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);

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


	public void setActiveAutomatonViewerPanel(JScrollPane newPanel)
	{
		System.err.println("setActiveAutomatonViewerPanel: " + newPanel);
		Component oldPanel = splitPanelHorizontal.getRightComponent();
		if (oldPanel != newPanel)
		{
			Dimension oldSize = emptyAutomatonViewerPanel.getSize();

			if (oldPanel != null)
			{
				splitPanelHorizontal.remove(oldPanel);
				oldSize = oldPanel.getSize();
			}

			if (newPanel == null)
			{
				emptyAutomatonViewerPanel.setPreferredSize(oldSize);
				splitPanelHorizontal.setRightComponent(emptyAutomatonViewerPanel);
			}
			else
			{
				System.err.println("setting new right panel");
				newPanel.setPreferredSize(oldSize);
				splitPanelHorizontal.setRightComponent(newPanel);
			}
		}

		validate();
		repaint();

	}

	public JToolBar getToolBar(JToolBar parentToolBar)
	{
		return parentToolBar;
	}

 	class EmptyAutomatonViewerPanel
 		extends JPanel
 	{
		public EmptyAutomatonViewerPanel()
		{
			setPreferredSize(new Dimension(200, preferredHeight));
			setMinimumSize(panelMinimumSize);
		}
	}
}