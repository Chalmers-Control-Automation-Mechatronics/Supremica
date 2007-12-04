//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   UnSelectCommand
//###########################################################################
//# $Id: UnSelectCommand.java,v 1.7 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A generic command for removing items from a selection.</P>
 *
 * <P>This command can deselect any collection of items from components that
 * implement the {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik, Simon Ware
 */

public class UnSelectCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to deselect the given item.
   * @param  proxy      The item to be deselected.
   * @param  panel      The panel that owns and controls the selection.
   */
  public UnSelectCommand(final Proxy proxy, final SelectionOwner panel)
  {
    this(Collections.singletonList(proxy), panel);
  }

  /**
   * Creates a command to deselect the given list of items.
   * @param  unselected A list items to be deselected.
   * @param  panel      The panel that owns and controls the selection.
   */
  public UnSelectCommand(final List<? extends Proxy> unselected,
			 final SelectionOwner panel)
  {
    mPanel = panel;
    mUnSelected = unselected;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mPanel.removeFromSelection(mUnSelected);
  }
	
  public void undo()
  {
    mPanel.addToSelection(mUnSelected);
  }
	
  public boolean isSignificant()
  {
    return false;
  }
	
  public String getName()
  {
    return "UnSelect";
  }


  //#########################################################################
  //# Data Members
  private final SelectionOwner mPanel;
  private final List<? extends Proxy> mUnSelected;

}
