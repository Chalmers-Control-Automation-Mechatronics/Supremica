//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.about;

import java.awt.Color;
import java.awt.Dimension;
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
    setBackground(Color.WHITE);
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int screenWidth = (int) screenSize.getWidth();
    final int textWidth = Math.min(TEXT_WIDTH, screenWidth);
    mPanel = new JEditorPane();
    mPanel.setEditable(false);
    mPanel.setSize(textWidth, Integer.MAX_VALUE);
    mPanel.addPropertyChangeListener("page", this);
    mPanel.setPage(url);
    // The data is loaded asynchronously by another thread.
    // We can only query and adjust the panel size after loading,
    // hence the propertyChange() listener below completes the display.
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
    add(scroll);
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);
  }


  //#########################################################################
  //# Data Members
  private final JEditorPane mPanel;


  //#########################################################################
  //# Class Constants
  private static final int TEXT_WIDTH = 640;

  private static final long serialVersionUID = -5901475634903492023L;

}
