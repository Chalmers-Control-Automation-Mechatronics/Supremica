//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;

/**
 * A new action
 */
public class AnalyzerStatisticsAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerStatisticsAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Statistics");
        putValue(Action.SHORT_DESCRIPTION, "Statistics");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_CONSOLE_INFO);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        final Logger logger = LogManager.getLogger();
        final int nbrOfAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getVisualProject().nbrOfAutomata();
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, false, false, true, true))
        {
            return;
        }

        logger.info("Number of selected automata: " + selectedAutomata.size() + " (" + nbrOfAutomata + ")");
        logger.info("Size of union alphabet: " + selectedAutomata.getUnionAlphabet().size());

        for (final Automaton currAutomaton : selectedAutomata)
        {
            final StringBuilder statusStr = new StringBuilder();

            statusStr.append("Status for automaton: " + currAutomaton.getName());

            statusStr.append("\n\tnumber of states: " + currAutomaton.nbrOfStates());
            statusStr.append("\n\tnumber of events: " + currAutomaton.nbrOfEvents());
            statusStr.append("\n\tnumber of transitions: " + currAutomaton.nbrOfTransitions());
            statusStr.append("\n\tnumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
            //statusStr.append("\n\tNumber of mutually accepting states: " + currAutomaton.nbrOfMutuallyAcceptingStates());
            statusStr.append("\n\tnumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());

            final int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();
            if (acceptingAndForbiddenStates > 0)
            {
                statusStr.append("\n\tnumber of accepting AND forbidden states: " + acceptingAndForbiddenStates);
            }

            if (currAutomaton.isDeterministic())
            {
                final Alphabet redundantEvents = currAutomaton.getRedundantEvents();
                if (redundantEvents.nbrOfEvents() > 0)
                    statusStr.append("\n\talphabet of redundant events: " + redundantEvents);
                statusStr.append("\n\tthe automaton is deterministic");
            }

            if ((currAutomaton.getComment() != null) && !currAutomaton.getComment().equals(""))
            {
                statusStr.append("\n\tcomment: \"" + currAutomaton.getComment() + "\"");
            }

            // logger.info(statusStr.toString());
            logger.info(statusStr.toString());
        }

        if (selectedAutomata.size() > 1)
        {
            double potentialNumberOfStates = 1.0;

            for (final Automaton currAutomaton : selectedAutomata)
            {
                potentialNumberOfStates = potentialNumberOfStates * currAutomaton.nbrOfStates();
            }

            logger.info("Number of potential states: " + ((long) potentialNumberOfStates));
        }
    }
}
