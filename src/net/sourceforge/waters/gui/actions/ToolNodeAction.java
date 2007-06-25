//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ToolNodeAction
//###########################################################################
//# $Id: ToolNodeAction.java,v 1.1 2007-06-25 20:18:48 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ControlledToolbar;
import org.supremica.gui.ide.IDE;


public class ToolNodeAction
  extends ToolAction
{

  //#########################################################################
  //# Constructors
  public ToolNodeAction(final IDE ide)
  {
    super(ide,
	  "Nodes",
	  "Create simple nodes",
	  "/icons/waters/node16.gif",
	  ControlledToolbar.Tool.NODE);
  }

}
