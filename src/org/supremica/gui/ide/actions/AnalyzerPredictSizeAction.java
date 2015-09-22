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

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.GeneticAlgorithms;

/**
 * Action for predicting composition size.
 */
public class AnalyzerPredictSizeAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerPredictSizeAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Predict size");
        putValue(Action.SHORT_DESCRIPTION, "Predict composition size using genetically evolved algorithm, works best for two automata");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    /**
     * Predict the size, calculate the exact size and the worst case size and present the result.
     */
    public void doAction()
    {
        Automata automata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        
        // Calculate predicted synchronisation size
        int prediction = (int) GeneticAlgorithms.predictSynchronizationSize(automata);
        // Calculate exact synchronisation size (for comparison)
        int exact = GeneticAlgorithms.calculateSynchronizationSize(automata);
        // Calculate worst case size
        int worst = 1;
        for (Automaton aut: automata)
            worst *= aut.nbrOfStates();

        ide.info("Predicted size: " + prediction + ", exact size: " + exact + ", worst case size: " + worst + ".");
    }
}





