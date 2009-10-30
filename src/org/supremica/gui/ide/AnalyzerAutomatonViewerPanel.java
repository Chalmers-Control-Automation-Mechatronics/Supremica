package org.supremica.gui.ide;

import java.awt.Dimension;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.DotBuilder;
import org.supremica.gui.DotBuilderGraphObserver;
import org.supremica.automata.IO.AutomatonToDot;
import org.supremica.automata.Automaton;

import att.grappa.Graph;
import att.grappa.GrappaPanel;

class AnalyzerAutomatonViewerPanel
    extends WhiteScrollPane
    implements DotBuilderGraphObserver
{
    private static final long serialVersionUID = 1L;
    
    private String name;
    private Automaton theAutomaton;
    private GrappaPanel viewerPanel = null;
    
    AnalyzerAutomatonViewerPanel(String name, Automaton theAutomaton)
    {
        this.name = name;
        this.theAutomaton = theAutomaton;
        setPreferredSize(IDEDimensions.rightAnalyzerPreferredSize);
        setMinimumSize(IDEDimensions.rightAnalyzerMinimumSize);
        build();
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setPreferredSize(Dimension dimension)
    {
        super.setPreferredSize(dimension);
        if (viewerPanel != null)
        {
            viewerPanel.setPreferredSize(dimension);
        }
    }
    
    public void setMinimumSize(Dimension dimension)
    {
        super.setMinimumSize(dimension);
        if (viewerPanel != null)
        {
            viewerPanel.setMinimumSize(dimension);
        }
    }
    
    private void build()
    {
        DotBuilder.getDotBuilder(null, this, new AutomatonToDot(theAutomaton), "");
    }
    
    public void setGraph(Graph theGraph)
    {
        viewerPanel = new GrappaPanel(theGraph);
        viewerPanel.setScaleToFit(false);
        viewerPanel.setPreferredSize(getPreferredSize());
        viewerPanel.setMinimumSize(getMinimumSize());
        getViewport().add(viewerPanel);
        getViewport().setPreferredSize(getPreferredSize());
        getViewport().setMinimumSize(getMinimumSize());
        validate();
    }
    
}