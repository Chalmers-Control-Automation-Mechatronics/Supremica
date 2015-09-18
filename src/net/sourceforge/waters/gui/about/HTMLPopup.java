//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.about
//# CLASS:   HTMLPopup
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.about;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;


/**
 * A dialog window that can display arbitrary HTML contents.
 * A scrollbar is included if necessary.
 * This is used by the {@link AboutPanel} to display the license information.
 *
 * @author Robi Malik
 */
public class HTMLPopup
  extends JDialog
  implements PropertyChangeListener
{

  //#########################################################################
  //# Constructor
  public HTMLPopup(final String title, final URL url, final JFrame owner)
    throws IOException
  {
    super(owner, title);
    mClickPosition = MouseInfo.getPointerInfo().getLocation();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int textWidth = Math.min(TEXT_WIDTH, screenWidth);
    mPanel = new JEditorPane();
    mPanel.setEditable(false);
    mPanel.setSize(textWidth, Integer.MAX_VALUE);
    mPanel.addPropertyChangeListener("page", this);
    mPanel.setPage(url);
  }


  //#########################################################################
  //# Interface java.beans.PropertyChangeListener
  @Override
  public void propertyChange(final PropertyChangeEvent event)
  {
    mPanel.removePropertyChangeListener(this);
    final JScrollPane scroll = new JScrollPane(mPanel);
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int screenHeight = (int) screenSize.getHeight();
    final int textWidth = Math.min(TEXT_WIDTH, screenWidth);
    final int textHeight =
      Math.min(mPanel.getPreferredSize().height, screenHeight * 7 / 8);
    final Dimension size = new Dimension(textWidth, textHeight);
    mPanel.setPreferredSize(size);
    setBackground(Color.WHITE);
    add(scroll);
    pack();
    final Dimension windowSize = getSize();
    int x = mClickPosition.x - windowSize.width / 2;
    if (x < 0) {
      x = 0;
    } else if (x + windowSize.width > screenWidth) {
      x = screenWidth - windowSize.width;
    }
    int y = mClickPosition.y - windowSize.height / 2;
    if (y < 0) {
      y = 0;
    } else if (y + windowSize.height > screenHeight) {
      y = screenHeight - windowSize.height;
    }
    setLocation(x, y);
    setVisible(true);
  }


  //#########################################################################
  //# Data Members
  private final JEditorPane mPanel;
  private final Point mClickPosition;


  //#########################################################################
  //# Class Constants
  private static final int TEXT_WIDTH = 640;

  private static final long serialVersionUID = -5901475634903492023L;

}
