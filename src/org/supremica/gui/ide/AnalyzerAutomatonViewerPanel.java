package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.DotBuilder;
import org.supremica.gui.DotBuilderObserver;
import org.supremica.automata.IO.AutomatonToDot;
import org.supremica.automata.Automaton;

import att.grappa.Graph;
import att.grappa.GrappaPanel;

class AnalyzerAutomatonViewerPanel
	extends WhiteScrollPane
	implements DotBuilderObserver
{
	private ModuleContainer moduleContainer;
	private String name;
	private DotBuilder builder;
	private Automaton theAutomaton;

	AnalyzerAutomatonViewerPanel(ModuleContainer moduleContainer, String name, Automaton theAutomaton)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
		this.theAutomaton = theAutomaton;
		build();
	}

	public String getName()
	{
		return name;
	}

	private void build()
	{
		builder = DotBuilder.getDotBuilder(this, new AutomatonToDot(theAutomaton));

		//builder.start();
	}

	public void setGraph(Graph theGraph)
	{
		GrappaPanel viewerPanel = new GrappaPanel(theGraph);
		viewerPanel.setScaleToFit(false);
		getViewport().add(viewerPanel);
		validate();
		revalidate();
		repaint();
	}

}