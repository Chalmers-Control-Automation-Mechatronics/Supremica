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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.supremica.automata.Automata;
import org.supremica.automata.IO.EncodingHelper;
import org.supremica.gui.AutomataHierarchyViewer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;

/**
 * A new action
 */
public class AnalyzerViewModularStructureAction
    extends IDEAction
{
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerViewModularStructureAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Modular Structure");
        putValue(Action.SHORT_DESCRIPTION, "View Modular Structure");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/supremica/modularstructure16.gif")));
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
       final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

        // Sanity check
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 2, false, false, true, false))
        {
            return;
        }

        // Warn if there are too many "states" i.e. automata
        final int maxNbrOfStates = Config.DOT_MAX_NBR_OF_STATES.get();
        if (maxNbrOfStates < selectedAutomata.size())
        {
            String msg = "You have selected " + selectedAutomata.size() + " automata. It is not " +
                "recommended to display the modular structure for more than " + maxNbrOfStates +
                " automata.";
            msg = EncodingHelper.linebreakAdjust(msg);

            final Object[] options = { "Continue", "Abort" };
            final int response = JOptionPane.showOptionDialog(ide.getFrame(), msg, "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);
            if(response == JOptionPane.NO_OPTION)
            {
                return;
            }
        }

        // View
        try
        {
            final AutomataHierarchyViewer viewer = new AutomataHierarchyViewer(selectedAutomata);

            viewer.setVisible(true);

            //viewer.setState(Frame.NORMAL);
        }
        catch (final Exception ex)
        {
            logger.error("Exception in AutomataHierarchyViewer.", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }
}





