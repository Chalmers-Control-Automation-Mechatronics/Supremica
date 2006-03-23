package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.GridBagLayout;


public class AnalyzerPanel
	extends MainPanel
{
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabPanel;
	private JComponent automatonViewerPanel;
	private AnalyzerAutomataPanel automataPanel;

	public AnalyzerPanel(ModuleContainer moduleContainer, String name)
	{
		super(moduleContainer, name);
		setPreferredSize(IDEDimensions.mainPanelPreferredSize);
		setMinimumSize(IDEDimensions.mainPanelMinimumSize);

		tabPanel = new JTabbedPane(JTabbedPane.BOTTOM);
		tabPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
		tabPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);

		automataPanel = new AnalyzerAutomataPanel(this, moduleContainer, "All");
		automataPanel.setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
		automataPanel.setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
		tabPanel.add(automataPanel);

		tabPanel.setSelectedComponent(automataPanel);

		automatonViewerPanel = getEmptyRightPanel();
		//		automatonViewerPanel.setPreferredSize(IDEDimensions.rightAnalyzerPreferredSize);
		//		automatonViewerPanel.setMinimumSize(IDEDimensions.rightAnalyzerMinimumSize);

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, automatonViewerPanel);
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);
		//		splitPanelHorizontal.setPreferredSize(IDEDimensions.mainPanelPreferredSize);
		//		splitPanelHorizontal.setMinimumSize(IDEDimensions.mainPanelMinimumSize);
		
		((GridBagLayout)getLayout()).setConstraints(splitPanelHorizontal, getGridBagConstraints());

		add(splitPanelHorizontal);
		//		validate();
	}


	public void addToolBarEntries(IDEToolBar toolBar)
	{
	}

	public void disablePanel()
	{
		getActions().enableAnalyzerActions(false);
	}

	public void enablePanel()
	{
		getActions().enableAnalyzerActions(true);
	}

}
