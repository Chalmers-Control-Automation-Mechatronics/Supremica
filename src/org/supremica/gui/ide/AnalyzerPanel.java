package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;


public class AnalyzerPanel
	extends MainPanel
{
	private JTabbedPane tabPanel;
	private JComponent automatonViewerPanel;
	private AnalyzerAutomataPanel automataPanel;

	public AnalyzerPanel(ModuleContainer moduleContainer, String name)
	{
		super(moduleContainer, name);

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
		tabPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
		tabPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);

		automataPanel = new AnalyzerAutomataPanel(this, moduleContainer, "All");
		automataPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
		automataPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
		tabPanel.add(automataPanel);

		tabPanel.setSelectedComponent(automataPanel);

		automatonViewerPanel = getEmptyRightPanel();

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, automatonViewerPanel);
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);

		((GridBagLayout)getLayout()).setConstraints(splitPanelHorizontal, getGridBagConstraints());

		add(splitPanelHorizontal);
	}


	public void addToolBarEntries(JToolBar toolBar)
	{
	}
}