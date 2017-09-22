//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.AutomataListener;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.AutomataSerializer;
import org.supremica.automata.IO.AutomataToHierarchyToDot;


public class AutomataHierarchyViewer
    extends DotViewer
    implements AutomataListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(AutomataHierarchyViewer.class);
    private final Automata theAutomata;

    public AutomataHierarchyViewer(final Automata theAutomata)
    throws Exception
    {
        this.theAutomata = theAutomata;

        //super.setObjectName("Hierarchy " + theAutomata);
        super.setObjectName("Modular structure");
        theAutomata.getListeners().addListener(this);
    }

    // Implementation of AutomataListener interface
    @Override
    public void automatonAdded(final Automata automata, final Automaton automaton)
    {
        updated(automata, theAutomata);
    }

    @Override
    public void automatonRemoved(final Automata automata, final Automaton automaton)
    {
        updated(automata, theAutomata);
    }

    @Override
    public void automatonRenamed(final Automata automata, final Automaton automaton)
    {
        updated(automata, theAutomata);
    }

    @Override
    public void actionsOrControlsChanged(final Automata automata)
    {
        updated(automata, theAutomata);
    }

    // End of interface implementation
    @Override
    public AutomataSerializer getSerializer()
    {
        final AutomataToHierarchyToDot serializer = new AutomataToHierarchyToDot(theAutomata);

        serializer.setLeftToRight(leftToRightCheckBox.isSelected());
        serializer.setWithLabels(withLabelsCheckBox.isSelected());
        serializer.setWithCircles(withCirclesCheckBox.isSelected());
        serializer.setUseColors(useStateColorsCheckBox.isSelected());

        try
        {
            serializer.serialize("Output.txt");
        }
        catch (final Exception ex)
        {
            logger.error(ex.getMessage());
        }

        return serializer;
    }
}
