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
    final AboutPanel panel = new AboutPanel(ide);
    final Border border = BorderFactory.createEmptyBorder(2, 4, 2, 1);
    panel.setBorder(border);
    panel.setSize(TEXT_WIDTH, Integer.MAX_VALUE);
    final int textHeight = panel.getPreferredSize().height;
    final Dimension panelSize = new Dimension(TEXT_WIDTH, textHeight);
    panel.setPreferredSize(panelSize);
    add(panel);
    pack();
    setLocationRelativeTo(ide.getFrame());
    setResizable(false);
  }


  //#########################################################################
  //# Class Constants
  private static final int TEXT_WIDTH = 480;

  private static final long serialVersionUID = -6488302835635607997L;

}
