//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ToolNodeAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.IDE;


public class ToolNodeAction
  extends ToolAction
{

  //#########################################################################
  //# Constructors
  public ToolNodeAction(final IDE ide)
  {
    super(ide, "Nodes", "Create simple nodes", IconLoader.ICON_TOOL_NODE,
          ControlledToolbar.Tool.NODE);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
