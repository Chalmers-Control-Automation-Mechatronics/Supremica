package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.gui.FTMinimalCutSetComputationWorker;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.SupremicaAnalyzerPanel;

/**
 * Zenuity Hackfest 2019
 *
 * The computation of FT minimal cut sets from the FT model.
 *
 * @author zhefei
 */

public class AnalyzerComputeMinimalCutSetsAction
  extends IDEAction
{
  private final Logger logger =
    LogManager.getLogger(AnalyzerComputeMinimalCutSetsAction.class);

  private static final long serialVersionUID = 1L;

  public AnalyzerComputeMinimalCutSetsAction(final List<IDEAction> actionList)
  {
    super(actionList);
    setEditorActiveRequired(false);
    setAnalyzerActiveRequired(true);
    putValue(Action.NAME, "Compute FT minimal cut sets");
    putValue(Action.SHORT_DESCRIPTION,
             "Compute FT minimal cut sets");
    final ImageIcon icon =
      new ImageIcon(IDE.class.getResource("/icons/supremica/modularstructure16.gif"));
    putValue(Action.SMALL_ICON, icon);
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
    final SupremicaAnalyzerPanel panel =
      ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel();
    final Automata selected = panel.getSelectedAutomata();
    if (!selected.sanityCheck(ide.getIDE(), 2, true, false, true, true))
    {
        return;
    }

    final FTMinimalCutSetComputationWorker fmcsw =
      new FTMinimalCutSetComputationWorker(ide.getIDE(), selected);
    fmcsw.start();
  }

}
