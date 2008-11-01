//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ErrorLabel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;



public class ErrorLabel
  extends JLabel
  implements ErrorDisplay
{

  //#########################################################################
  //# Constructors
  public ErrorLabel()
  {
    super(" ", SwingConstants.CENTER);
  }

  public ErrorLabel(final String msg)
  {
    super(msg, SwingConstants.CENTER);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.ErrorDisplay
  public void displayError(final String msg)
  {
    setForeground(ERROR_COLOR);
    setText(msg);
  }

  public void displayWarning(final String msg)
  {
    setForeground(WARNING_COLOR);
    setText(msg);
  }

  public void displayMessage(final String msg)
  {
    setForeground(MESSAGE_COLOR);
    setText(msg);
  }

  public void clearDisplay()
  {
    setText(" ");
  }


  //#########################################################################
  //# Class Constants
  private static final Color ERROR_COLOR = Color.RED;
  private static final Color WARNING_COLOR = Color.RED;
  private static final Color MESSAGE_COLOR = null;

}
