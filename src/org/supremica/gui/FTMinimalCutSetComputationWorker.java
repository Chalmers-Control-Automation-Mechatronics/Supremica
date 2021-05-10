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

package org.supremica.gui;

import java.awt.EventQueue;
import java.util.ArrayList;

import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.MinimalCutSetsSynthesizer;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.util.ActionTimer;


/**
 * Zenuity Hackfest 2019
 *
 * Threads dealing with the computation of minimal cut sets of FT EFA.
 *
 * @author zhefei
 * @since 12/05/2019
 */

public class FTMinimalCutSetComputationWorker extends Thread
  implements Abortable
{
  private static Logger logger =
    LogManager.getLogger(FTMinimalCutSetComputationWorker.class);

  private final IDEActionInterface ide;
  private final Automata theAutomata;
  private ExecutionDialog executionDialog;
  private boolean abortRequested = false;
  private ActionTimer timer;

  public FTMinimalCutSetComputationWorker(final IDEActionInterface gui,
                                          final Automata theAutomata)
  {
    this.ide = gui;
    this.theAutomata = theAutomata;
  }

  @Override
  public void run()
  {
    // Initialize the execution dialog
    final ArrayList<Abortable> threadsToStop = new ArrayList<Abortable>();
    threadsToStop.add(this);
    if (ide != null) {
      executionDialog =
        new ExecutionDialog(ide.getFrame(),
                            "Calculating FT minimal cut sets",
                            threadsToStop);
      executionDialog.setMode(ExecutionDialogMode.COMPUTEFTMCS);
    }

    timer = new ActionTimer();
    // Timer
    timer.start();
    // result = new Automata();

    try {
      final MinimalCutSetsSynthesizer synthesizer =
        new MinimalCutSetsSynthesizer(theAutomata);
      threadsToStop.add(synthesizer);
      // what should be returned?
      synthesizer.execute();
      threadsToStop.remove(synthesizer);
    } catch (final Exception ex) {
      logger.error("Exeception in MinimalCutSetsSynthesizer. " + ex);
      logger.debug(ex.getStackTrace());
    }

    // Timer
    timer.stop();

    // Hide execution dialog
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        if (ide != null) {
          if (executionDialog != null) {
            executionDialog.setMode(ExecutionDialogMode.HIDE);
          }
        }
      }
    });


    if (!abortRequested) {
      logger.info("Computation of minimal cut sets completed after "
        + timer.toString() + ".");

      // // Present result
//      if (ide != null) {
//        try {
//          ide.getIDE().getActiveDocumentContainer()
//            .getSupremicaAnalyzerPanel().addAutomata(result);
//        } catch (final Exception ex) {
//          logger.error(ex);
//        }
//      }
    } else {
      logger.info("Execution stopped after " + timer.toString());
    }

    if (ide != null) {
      if (executionDialog != null) {
        executionDialog.setMode(ExecutionDialogMode.HIDE);
      }
    }
  }

  @Override
  public void requestAbort()
  {
    abortRequested = true;

    logger.debug("MinimalCutSetsSynthesizer requested to stop.");

    if (executionDialog != null) {
      executionDialog.setMode(ExecutionDialogMode.HIDE);
    }
  }

  @Override
  public boolean isAborting()
  {
    return abortRequested;
  }

  @Override
  public void resetAbort()
  {
    abortRequested = false;
  }

}
