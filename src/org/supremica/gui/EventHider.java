//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.ide.actions.IDEActionInterface;

/**
 * Lets the user choose events to be hidden.
 */
class EventHiderDialog
    extends LanguageRestrictorDialog
{
    // Gaaaah! LanguageRestrictorDialog has lots of stuff in it that should be somewhere else.
    // There should be a "EventSelectorDialog" or something that should be used for the selection.
    // Other classes may want to do this, you know. I didn't have the energy to do all that so this
    // is an ugly fix using the LanguageRestrictorDialog.

    // LanguageRestictorDialog is no longer used by itself! It was used from old Supremica (ActionMan) before.
    // The function is not in the AutomataMinimizer.

    private static final long serialVersionUID = 1L;

    private static Logger logger = LogManager.getLogger(EventHiderDialog.class);

    private final IDEActionInterface ide;

    private boolean preserveControllability = false;

    public EventHiderDialog(final IDEActionInterface ide, final Automata automata, final Alphabet globalAlphabet)
    {
        super(automata, globalAlphabet);
        super.setTitle("Event hider");
        super.okButton = new OkButton();

        // Add menu for choosing whether to preserve controllability or not.
        final JMenuBar menuBar = super.getJMenuBar();

        // Restrict
        final JMenu controllabilityMenu = new JMenu("Controllability");
        controllabilityMenu.setMnemonic(KeyEvent.VK_C);
        // Ignore controllability
        final JRadioButtonMenuItem preserveMenuIgnore = new JRadioButtonMenuItem("Ignore controllability", !preserveControllability);
        preserveMenuIgnore.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                preserveControllability = false;
            }
        });
        controllabilityMenu.add(preserveMenuIgnore);
        // Preserve controllability
        final JRadioButtonMenuItem preserveMenuPreserve = new JRadioButtonMenuItem("Preserve controllability", preserveControllability);
        preserveMenuPreserve.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                preserveControllability = true;
            }
        });
        controllabilityMenu.add(preserveMenuPreserve);
        // Group the radio buttons
        final ButtonGroup preserveGroup = new ButtonGroup();
        preserveGroup.add(preserveMenuIgnore);
        preserveGroup.add(preserveMenuPreserve);
        // Add to menu
        menuBar.add(controllabilityMenu);

        super.pack();
        this.ide = ide;
    }

    private class OkButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;

        public OkButton()
        {
            super("Ok");

            setToolTipText("Do the hiding");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    System.err.println("Tryck inte så hårt!");
                    doRestrict();
                }
            });
        }
    }

    @Override
    protected void doRestrict()
    {
        // The set of new automata, based on the selected automata
        final Automata newAutomata = new Automata();

        // Get the events selected by the user (may be for keeping or for hiding)
        final Alphabet alpha = restrictEvents.getAlphabet();
        Alphabet toBeHidden;

        // Loop over the selected automata
        final Iterator<Automaton> autit = automata.iterator();
        while (autit.hasNext())
        {
            final Automaton automaton = autit.next();
            final Automaton newAutomaton = new Automaton(automaton);
            newAutomaton.setName(null);

            // Find out which events should be hidden
            if (restrictEvents.toErase())
            {
                // Take the chosen ones
                toBeHidden = alpha;
            }
            else
            {
                // Invert
                toBeHidden = AlphabetHelpers.minus(automaton.getAlphabet(), alpha);
            }

            // Do the hiding (preserve controllability!)
            newAutomaton.hide(toBeHidden, preserveControllability);

            // Set appropriate comment
            newAutomaton.setComment(automaton.getName() + "//" +
                AlphabetHelpers.intersect(automaton.getAlphabet(), toBeHidden));

            // Add automaton
            newAutomata.addAutomaton(newAutomaton);
        }

        // Shut the window!!
        shutWindow();

        try
        {
            ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().addAutomata(newAutomata);
        }
        catch (final Exception ex)
        {
            logger.debug("EventHiderDialog::doRestrict() -- ", ex);
            logger.debug(ex.getStackTrace());
        }
    }
}

public class EventHider
    extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    private final IDEActionInterface ide;

    public EventHider(final IDEActionInterface ide)
    {
        putValue(NAME, "Event hider");
        putValue(SHORT_DESCRIPTION, "Stop observing selected events");
        this.ide = ide;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
        // Get the selected automata
        final Automata automata = ActionMan.getGui().getSelectedAutomata();

        // Throw up the dialog, let the user select the alphabet
        new EventHiderDialog(ide, automata, ide.getIDE().getActiveDocumentContainer().getSupremicaAnalyzerPanel().getUnselectedAutomata().getUnionAlphabet());
    }

    public void doAction(final Automata theAutomata, final Alphabet othersAlphabet)
    {
        new EventHiderDialog(ide, theAutomata, othersAlphabet);
    }
}
