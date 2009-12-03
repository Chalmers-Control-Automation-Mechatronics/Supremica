//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   AutomataContainer
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;

import org.supremica.automata.Project;


public class AutomataContainer
    extends DocumentContainer
{

    //#######################################################################
    //# Constructor
    public AutomataContainer(final IDE ide, final Project project)
    {
        super(ide, project);
        mAnalyzerPanel = new AnalyzerPanel(this, "Analyzer");
        mAnalyzerPanel.addProject(project);
    }


    //#######################################################################
    //# Overrides for Abstract Base Class
    //# org.supremica.gui.ide.DocumentContainer
    public Component getPanel()
    {
        return mAnalyzerPanel;
    }

    public EditorPanel getEditorPanel()
    {
        return null;
    }

    public AnalyzerPanel getAnalyzerPanel()
    {
        return mAnalyzerPanel;
    }

    public Component getActivePanel()
    {
        return mAnalyzerPanel;
    }

    public String getTypeString()
    {
        return TYPE_STRING;
    }


    //#######################################################################
    //# Simple Access
    public Project getAutomata()
    {
        return (Project) getDocument();
    }
    /*
    public SimulatorPanel getSimulatorPanel()
    {
        if (simulatorPanel == null)
        {
            simulatorPanel = new SimulatorPanel(this, "Simulator");
        }
        return simulatorPanel;
    }*/


    //#######################################################################
    //# Data Members
    private final AnalyzerPanel mAnalyzerPanel;
    //private SimulatorPanel simulatorPanel = null;


    //#######################################################################
    //# Class Constants
    static final String TYPE_STRING = "Supremica project";

}
