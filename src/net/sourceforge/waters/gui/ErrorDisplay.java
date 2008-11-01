//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ErrorDisplay
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;


public interface ErrorDisplay
{

  public void displayError(String msg);

  public void displayWarning(String msg);

  public void displayMessage(String msg);

  public void clearDisplay();

}
