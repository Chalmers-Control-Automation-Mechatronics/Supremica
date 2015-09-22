//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
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





