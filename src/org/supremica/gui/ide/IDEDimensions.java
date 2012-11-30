//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEDimensions
//###########################################################################
//# $Id$
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
    new Dimension(100, 100);

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
