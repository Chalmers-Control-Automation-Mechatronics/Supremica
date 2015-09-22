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








