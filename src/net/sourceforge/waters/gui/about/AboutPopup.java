//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.about
//# CLASS:   AboutPopup
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.about;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.border.Border;

import net.sourceforge.waters.config.Version;

import org.supremica.gui.ide.IDEReportInterface;


/**
 * A dialog window that displays the version and configuration information.
 * This popup is triggered throw by About... menu button. It simply displays
 * an {@link AboutPanel}.
 *
 * @author Robi Malik
 */
public class AboutPopup
  extends JDialog
{

  //#########################################################################
  //# Constructor
  public AboutPopup(final IDEReportInterface ide)
  {
    super(ide.getFrame(), "About " + Version.getInstance().getTitle());

    // Create panel and calculate size
    final AboutPanel panel = new AboutPanel(ide);
    panel.setSize(TEXT_WIDTH - 6, Integer.MAX_VALUE);
    final int textHeight = panel.getPreferredSize().height;
    final Dimension size = new Dimension(TEXT_WIDTH, textHeight + 4);
    panel.setPreferredSize(size);
    final Border border = BorderFactory.createEmptyBorder(2, 5, 2, 1);
    panel.setBorder(border);
    add(panel);
    pack();

    // Place dialog close to mouse
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int screenHeight = (int) screenSize.getHeight();
    final Point clickPos = MouseInfo.getPointerInfo().getLocation();
    int x = clickPos.x - TEXT_WIDTH / 2;
    if (x < 0) {
      x = 0;
    } else if (x + TEXT_WIDTH > screenWidth) {
      x = screenWidth - TEXT_WIDTH;
    }
    int y = clickPos.y - textHeight / 2;
    if (y < 0) {
      y = 0;
    } else if (y + textHeight > screenHeight) {
      y = screenHeight - textHeight;
    }
    setLocation(x, y);
  }


  //#########################################################################
  //# Class Constants
  private static final int TEXT_WIDTH = 480;

  private static final long serialVersionUID = -6488302835635607997L;

}
