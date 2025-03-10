//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
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
import java.net.URI;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.gui.AutomataVerificationWorker;
import org.supremica.gui.VerificationDialog;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.ModuleContainer;

/**
 *	MF fix issue #138 (Feb 2022), make mOptions static final to remember setting between invokations
**/
public class AnalyzerVerifierAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    public AnalyzerVerifierAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Verify...");
        putValue(Action.SHORT_DESCRIPTION, "Run verification on the selected automata");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_VERIFY);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

	// MF, fix issue #138, static final makes Supremica remember the chosen options between invokations
	static final MinimizationOptions mOptions = MinimizationOptions.getDefaultVerificationOptions();

    @Override
    public void doAction()
    {
      final JFrame owner = ide.getFrame();
      // Retrieve the selected automata and make a sanity check
      final DocumentContainer container = ide.getActiveDocumentContainer();
      final Automata selectedAutomata =
        container.getSupremicaAnalyzerPanel().getSelectedAutomata();
      if (!selectedAutomata.sanityCheck(owner, 1, true, false, true, true)) {
        return;
      }
      if (container instanceof ModuleContainer) {
        final ModuleContainer moduleContainer = (ModuleContainer) container;
        final ModuleProxy module = moduleContainer.getModule();
        final URI uri = module.getLocation();
        selectedAutomata.setLocation(uri);
      }
      // Get the current options and allow the user to change them...
      final VerificationOptions vOptions = new VerificationOptions();
 //     final MinimizationOptions mOptions = MinimizationOptions.getDefaultVerificationOptions();
      final VerificationDialog verificationDialog =
        new VerificationDialog(ide.getIDE(), vOptions, mOptions);
      verificationDialog.show();
      if (!vOptions.getDialogOK()) {
        return;
      }
      if (vOptions.getVerificationType() ==
          VerificationType.LANGUAGEINCLUSION) {
        vOptions.setInclusionAutomata(container.getSupremicaAnalyzerPanel().getUnselectedAutomata());
      }
      final SynchronizationOptions sOptions =
        SynchronizationOptions.getDefaultVerificationOptions();
      // Work!
      new AutomataVerificationWorker(owner, selectedAutomata,
                                     vOptions, sOptions, mOptions);
    }
}
