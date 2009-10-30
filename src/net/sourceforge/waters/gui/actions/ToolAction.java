//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ToolAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.ControlledToolbar;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.IDEToolBar;


/**
 * The class of all actions representing tool actions. This class is used
 * to switch between the toolbar's drawing tools ('Select', 'Node', 'Edge',
 * etc.) in a uniform way. It is <I>not</I> used for any other toolbar
 * buttons.
 *
 * @author Robi Malik
 */

public abstract class ToolAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  ToolAction(final IDE ide,
             final String name,
             final String description,
             final String iconname,
             final ControlledToolbar.Tool tool)
  {
    super(ide);
    putValue(Action.NAME, name);
    putValue(Action.SHORT_DESCRIPTION, description);
    putValue(Action.SMALL_ICON,
             new ImageIcon(IDE.class.getResource(iconname)));
    mTool = tool;
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final IDEToolBar toolbar = ide.getToolBar();
    toolbar.setTool(mTool);
  }


  //#########################################################################
  //# Data Members
  private final ControlledToolbar.Tool mTool;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
