//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   LoggerErrorDisplay
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * A simple error display that sends all messages to Supremica's loggers.
 * The messages should show in the log window window at the bottom of
 * the IDE.
 *
 * @author Robi Malik
 */

public class LoggerErrorDisplay implements ErrorDisplay
{

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.ErrorDisplay
  public void displayError(final String msg)
  {
    LOGGER.error(msg);
  }

  public void displayWarning(final String msg)
  {
    LOGGER.warn(msg);
  }

  public void displayMessage(final String msg)
  {
    LOGGER.info(msg);
  }

  public void clearDisplay()
  {
  }


  //#########################################################################
  //# Static Class Constants
  private static final Logger LOGGER =
    LoggerFactory.createLogger(LoggerErrorDisplay.class);

}
