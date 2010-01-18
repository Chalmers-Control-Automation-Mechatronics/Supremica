package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.supremica.gui.ide.IDE;

public class DesktopCloseAllAction extends WatersDesktopAction
{

  protected DesktopCloseAllAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Close All Automata");
    putValue(Action.SHORT_DESCRIPTION, "Close all automata windows");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().closeAllAutomaton();
  }

  private static final long serialVersionUID = -1644229513613033199L;
}
