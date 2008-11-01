//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ToolGroupNodeAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ControlledToolbar;
import org.supremica.gui.ide.IDE;


public class ToolGroupNodeAction
  extends ToolAction
{

  //#########################################################################
  //# Constructors
  public ToolGroupNodeAction(final IDE ide)
  {
    super(ide,
	  "Group nodes",
	  "Create group nodes",
	  "/icons/waters/nodegroup16.gif",
	  ControlledToolbar.Tool.GROUPNODE);
  }

}
