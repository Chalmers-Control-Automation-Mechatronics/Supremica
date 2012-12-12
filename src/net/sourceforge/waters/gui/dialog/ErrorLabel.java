//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   ErrorLabel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.sourceforge.waters.gui.ErrorDisplay;


/**
 * @author Robi Malik
 */

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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
