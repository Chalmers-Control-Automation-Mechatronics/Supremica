//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   SelectCommand
//###########################################################################
//# $Id: SelectCommand.java,v 1.6 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A generic command for adding items to a selection.</P>
 *
 * <P>This command can select any collection of items from components that
 * implement the {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik, Simon Ware
 */

public class SelectCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a command to select the given item.
   * @param  proxy      The item to be selected.
   * @param  panel      The panel that owns and controls the selection.
   */
  public SelectCommand(final Proxy proxy, final SelectionOwner panel)
  {
    this(Collections.singletonList(proxy), panel);
  }

  /**
   * Creates a command to select the given list of items.
   * @param  selected   A list items to be selected.
   * @param  panel      The panel that owns and controls the selection.
   */
  public SelectCommand(final List<? extends Proxy> selected,
                       final SelectionOwner panel)
  {
    mPanel = panel;
    mSelected = selected;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    mPanel.addToSelection(mSelected);
  }
	
  public void undo()
  {
    mPanel.removeFromSelection(mSelected);
  }
	
  public boolean isSignificant()
  {
    return false;
  }
	
  public String getName()
  {
    return "Select";
  }


  //#########################################################################
  //# Data Members
  private final SelectionOwner mPanel;
  private final List<? extends Proxy> mSelected;

}
