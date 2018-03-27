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

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.gui.AutomataSynthesisWorker;
import org.supremica.gui.SynthesizerDialog;
import org.supremica.gui.ide.IDE;


public class AnalyzerSynthesizerAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    public AnalyzerSynthesizerAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Synthesize...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, "Synthesize a supervisor for the selected automata");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }


    @Override
    public void doAction()
    {
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, true, true, true, true))
        {
            return;
        }

        // Get the current options and allow the user to change them...
        final SynthesizerOptions options = new SynthesizerOptions();

        final SynthesizerDialog synthesizerDialog =
			new SynthesizerDialog(ide.getFrame(), selectedAutomata.size(), options);
        synthesizerDialog.setVisible(true);
        if (!options.getDialogOK())
        {
            return;
        }
        new AutomataSynthesisWorker(ide.getIDE(), selectedAutomata, options);

    }

}
