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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.gui.AlphabetViewer;
import org.supremica.gui.ide.IDE;

/**
 * View alphabet action.
 */
public class AnalyzerViewAlphabetAction
    extends IDEAction
{
    private final Logger logger = LogManager.getLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    public AnalyzerViewAlphabetAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Alphabet");
        putValue(Action.SHORT_DESCRIPTION, "View Alphabet");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/Alphabet16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * Opens an alphabet viewer window.
     */
    @Override
    public void doAction()
    {
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();
        final Alphabet alphabetSubset = selectedAutomata.getUnionAlphabet();
        final Automata allAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getAllAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, false, false, true, false))
        {
            return;
        }

        // Why not simpy instantiate an AlphabetViewer with the given
        // automata object?? Use AutomataViewer instead!
        try
        {
            final AlphabetViewer alphabetViewer = new AlphabetViewer(allAutomata, alphabetSubset);
            alphabetViewer.setVisible(true);
        }
        catch (final Exception ex)
        {
            // logger.error("Exception in AlphabetViewer", ex);
            logger.error("Exception in AutomataViewer: " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }
}
