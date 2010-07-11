package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.supremica.gui.ide.IDE;

/**
 * This class is not used anymore, but remains here incase it is useful.
 * @author ach17
 */
public class DesktopResizeAllAction extends WatersDesktopAction
{
  protected DesktopResizeAllAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Close Other Automata");
    putValue(Action.SHORT_DESCRIPTION, "Close all other automata windows");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().resizeAllAutomata();
  }

  private static final long serialVersionUID = -1644229513613033199L;
}