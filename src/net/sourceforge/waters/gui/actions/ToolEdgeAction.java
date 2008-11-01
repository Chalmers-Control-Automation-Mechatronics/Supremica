//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ToolEdgeAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ControlledToolbar;
import org.supremica.gui.ide.IDE;


public class ToolEdgeAction
  extends ToolAction
{

  //#########################################################################
  //# Constructors
  public ToolEdgeAction(final IDE ide)
  {
    super(ide,
	  "Edges",
	  "Create edges",
	  "/icons/waters/edge16.gif",
	  ControlledToolbar.Tool.EDGE);
  }

}
