//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ToolSelectAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ControlledToolbar;
import org.supremica.gui.ide.IDE;


public class ToolSelectAction
  extends ToolAction
{

  //#########################################################################
  //# Constructors
  public ToolSelectAction(final IDE ide)
  {
    super(ide,
	  "Select",
	  "Select",
	  "/icons/waters/select16.gif",
	  ControlledToolbar.Tool.SELECT);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
