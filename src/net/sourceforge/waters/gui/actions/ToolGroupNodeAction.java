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
import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.IDE;


public class ToolGroupNodeAction
  extends ToolAction
{

  //#########################################################################
  //# Constructors
  public ToolGroupNodeAction(final IDE ide)
  {
    super(ide, "Group nodes", "Create group nodes",
          IconLoader.ICON_TOOL_GROUP_NODE, ControlledToolbar.Tool.GROUPNODE);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
