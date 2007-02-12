//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ControlledToolbar
//###########################################################################
//# $Id: ControlledToolbar.java,v 1.4 2007-02-12 21:38:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.observer.Subject;


public interface ControlledToolbar
  extends Subject
{

  //##########################################################################
  //# Simple Access
  /**
   * Gets the currently selected tool.
   */
  public Tool getTool();


  //##########################################################################
  //# Class Constants
  public enum Tool
  {
    SELECT,
    NODE,
    GROUPNODE,
    EDGE;
  }

}
