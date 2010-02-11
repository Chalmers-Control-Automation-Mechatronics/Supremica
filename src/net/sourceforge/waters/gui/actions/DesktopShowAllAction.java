package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.supremica.gui.ide.IDE;

public class DesktopShowAllAction extends WatersDesktopAction
{
  protected DesktopShowAllAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Show All Automata");
    putValue(Action.SHORT_DESCRIPTION, "Open all automata windows");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().showAllAutomata();
  }

  private static final long serialVersionUID = -1644229513613033199L;
}