package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.analyzer.AutomataTable;
import net.sourceforge.waters.gui.dialog.AutomatonSynthesizerDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.IDE;

/**
 * The action to invoke the Conflict Checker dialog in the Waters analyser.
 *
 * @author Brandon Bassett
 */
public class AnalyzerConflictCheckAction extends WatersAnalyzerAction
{
  //#########################################################################
  //# Constructor
  protected AnalyzerConflictCheckAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Conflict Check ...");
    //putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_ANALYZER_SYNTH);
    //putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Y);
    //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.ALT_MASK));
    updateEnabledStatus();
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent arg0)
  {
    final IDE ide = getIDE();
    if (ide != null)
      new AutomatonSynthesizerDialog(getAnalyzerPanel());           //To be replaced with conflict dialog
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }

  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final AutomataTable table = getAnalyzerTable();
    if (table == null) {
      setEnabled(false);
      putValue(Action.SHORT_DESCRIPTION, "Conflict Check 1");
    } else if (table.getSelectedRowCount() > 0) {
      setEnabled(true);
      putValue(Action.SHORT_DESCRIPTION,
        "Conflict Check 2");
    } else {
      setEnabled(table.getRowCount() > 0);
      putValue(Action.SHORT_DESCRIPTION,
        "Conflict Check 3");
    }
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}