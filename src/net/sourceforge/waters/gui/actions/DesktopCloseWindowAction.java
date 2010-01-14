package net.sourceforge.waters.gui.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;

import org.supremica.gui.ide.IDE;

public class DesktopCloseWindowAction extends WatersDesktopAction
{

  protected DesktopCloseWindowAction(final IDE ide, final Point point)
  {
    super(ide);
    mPoint = point;
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().closeWindow(e, mPoint);
  }

  private final Point mPoint;
}
