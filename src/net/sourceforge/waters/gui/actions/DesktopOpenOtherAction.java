package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.IDE;

public class DesktopOpenOtherAction extends WatersDesktopAction
{
  protected DesktopOpenOtherAction(final IDE ide, final AutomatonProxy autoToOpen)
  {
    super(ide);
    mAutomaton = autoToOpen;
    putValue(Action.NAME, "Open Other Automata");
    putValue(Action.SHORT_DESCRIPTION, "View all the other Automata");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().openOtherAutomaton(mAutomaton.getName());
  }

  private final AutomatonProxy mAutomaton;
  private static final long serialVersionUID = -1644229513613033199L;
}
