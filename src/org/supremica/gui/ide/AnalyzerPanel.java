package org.supremica.gui.ide;

import javax.swing.*;
import java.awt.GridBagLayout;

import org.supremica.automata.Automata;
import org.supremica.automata.Project;

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

		splitPanelHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, automatonViewerPanel);
		splitPanelHorizontal.setContinuousLayout(false);
		splitPanelHorizontal.setOneTouchExpandable(false);
		splitPanelHorizontal.setDividerLocation(0.2);
		splitPanelHorizontal.setResizeWeight(0.0);

		((GridBagLayout)getLayout()).setConstraints(splitPanelHorizontal, getGridBagConstraints());

		add(splitPanelHorizontal);
	}

	/**
	 * Gets the selected automata.
	 */
	public Automata getSelectedAutomata()
	{
		return automataPanel.getSelectedAutomata();
	}

/*
	public Project getSelectedProject()
	{
		return automataPanel.getSelectedProject();
	}
*/

	public Automata getUnselectedAutomata()
	{
		return automataPanel.getUnselectedAutomata();
	}

	public Automata getAllAutomata()
	{
		return automataPanel.getAllAutomata();
	}


	public void addToolBarEntries(IDEToolBar toolBar)
	{
	}

	public void disablePanel()
	{
	}

	public void enablePanel()
	{
	}

}
