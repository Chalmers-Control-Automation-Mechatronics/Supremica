//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   MultilineLabel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import javax.swing.JLabel;

import net.sourceforge.waters.gui.HTMLPrinter;


public class MultilineLabel
  extends JLabel
{

  //#########################################################################
  //# Constructors
  public MultilineLabel(final String msg)
  {
    setHorizontalAlignment(CENTER);
    setText(msg);
  }


  //#########################################################################
  //# Overrides for javax.swing.JLabel
  @Override
  public void setText(final String msg)
  {
    super.setText("<html><P STYLE=\"text-align:center\">" +
      HTMLPrinter.encodeInHTML(msg) + "</p></html>");
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
