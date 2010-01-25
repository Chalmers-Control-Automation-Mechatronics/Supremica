package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.IDE;

public class DesktopOriginalSizeOtherAction extends WatersDesktopAction
{

  protected DesktopOriginalSizeOtherAction(final IDE ide, final AutomatonProxy autoToClose)
  {
    super(ide);
    mAutomaton = autoToClose;
    putValue(Action.NAME, "Resize Other Automaton");
    putValue(Action.SHORT_DESCRIPTION, "Resize all other automata to thier default size");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().resizeOther(mAutomaton.getName());
  }

  private final AutomatonProxy mAutomaton;

  private static final long serialVersionUID = -1644229513613033199L;
}
