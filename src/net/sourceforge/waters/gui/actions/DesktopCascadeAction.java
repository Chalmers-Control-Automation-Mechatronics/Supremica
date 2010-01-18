package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.supremica.gui.ide.IDE;

public class DesktopCascadeAction extends WatersDesktopAction
{
  protected DesktopCascadeAction(final IDE ide)
  {
    super(ide);

    putValue(Action.NAME, "Cascade");
    putValue(Action.SHORT_DESCRIPTION, "Tile all the open windows, so that all are visible");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().cascade();
  }

  private static final long serialVersionUID = -1644229513613033199L;
}
