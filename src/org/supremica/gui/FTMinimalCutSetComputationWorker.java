package org.supremica.gui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;
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
  private List<Set<LabeledEvent>> result;

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
