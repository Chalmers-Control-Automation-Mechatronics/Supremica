//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   AutomataContainer
//###########################################################################
//# $Id: AutomataContainer.java,v 1.3 2007-06-21 15:57:55 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.JTabbedPane;
import org.supremica.automata.Project;

public class AutomataContainer 
    extends DocumentContainer
{
    //#######################################################################
    //# Constructor
    public AutomataContainer(final IDE ide, final Project project)
    {
        super(ide, project);
        getAnalyzerPanel().addProject(project);
    }
    
    public Project getAutomata()
    {
        return (Project) getDocument();
    }
    
    public void addToTabPanel(JTabbedPane tabPanel)
    {
        tabPanel.add(getAnalyzerPanel());
    }
    
    public EditorPanel getEditorPanel()
    {
        return null;
    }
    
    public AnalyzerPanel getAnalyzerPanel()
    {
        if (analyzerPanel == null)
        {
            analyzerPanel = new AnalyzerPanel(this, "Analyzer");
        }
        return analyzerPanel;
    }
    
    public SimulatorPanel getSimulatorPanel()
    {
        if (simulatorPanel == null)
        {
            simulatorPanel = new SimulatorPanel(this, "Simulator");
        }
        return simulatorPanel;
    }
    
    public void updateActiveTab(JTabbedPane tabPanel)
    {
    }
    
    private AnalyzerPanel analyzerPanel = null;
    private SimulatorPanel simulatorPanel = null;
}
