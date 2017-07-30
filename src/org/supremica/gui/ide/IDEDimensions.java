//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Dimension;


/**
 * A static class containing constants identifying the preferred window
 * sizes of the IDE.
 *
 * @author Knut &Aring;kesson
 */

public class IDEDimensions
{

  //#########################################################################
  //# Class Constants
  private static final int MAINWINDOWWIDTH = 1024;
  private static final int MAINWINDOWHEIGHT = 768;

  public static final Dimension mainWindowPreferredSize =
    new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT);
  public static final Dimension mainWindowMinimumSize =
    new Dimension(100, 100);

  public static final Dimension loggerPreferredSize =
    new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT/4);
  public static final Dimension loggerMinimumSize =
    new Dimension(100, 60);

  public static final Dimension mainPanelPreferredSize =
    new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT*3/4);
  public static final Dimension mainPanelMinimumSize =
    new Dimension(100, 100);

  public static final Dimension leftEditorPreferredSize =
    new Dimension(MAINWINDOWWIDTH/3, MAINWINDOWHEIGHT*3/4);
  public static final Dimension leftEditorMinimumSize =
    new Dimension(100, 50);

  public static final Dimension rightEditorPreferredSize =
    new Dimension(MAINWINDOWWIDTH*2/3, MAINWINDOWHEIGHT*3/4);
  public static final Dimension rightEditorMinimumSize =
    new Dimension(100, 50);

  public static final Dimension leftAnalyzerPreferredSize =
    new Dimension(MAINWINDOWWIDTH*2/5, MAINWINDOWHEIGHT*3/4);
  public static final Dimension leftAnalyzerMinimumSize =
    leftEditorMinimumSize;

  public static final Dimension rightAnalyzerPreferredSize =
    new Dimension(MAINWINDOWWIDTH*3/5, MAINWINDOWHEIGHT*3/4);
  public static final Dimension rightAnalyzerMinimumSize =
    rightEditorMinimumSize;

  public static final Dimension rightEmptyPreferredSize =
    leftEditorPreferredSize;
  public static final Dimension rightEmptyMinimumSize =
    leftEditorMinimumSize;

  public static final Dimension leftSimulatorTablePreferredSize =
    new Dimension(MAINWINDOWWIDTH*1/3 - 20, MAINWINDOWHEIGHT*3/4 - 30);
  public static final Dimension leftSimulatorTableMinimumSize =
    new Dimension(80, 20);


  //#########################################################################
  //# Dummy Constructor to Prevent Instantiation of Class
  private IDEDimensions()
  {
  }

}
