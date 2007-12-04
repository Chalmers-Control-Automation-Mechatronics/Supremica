//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   AbstractEditCommand
//###########################################################################
//# $Id: AbstractEditCommand.java,v 1.2 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.transfer.SelectionOwner;


/**
 * <P>The general superclass for the generic commands that operate
 * on a selection owner.</P>
 *
 * <P>In addition to the panel (implementing the {@link SelectionOwner}
 * interface), this class stores a changeable descriptive name and a flag
 * to indicate whether the command should update the panel's selection. The
 * latter is useful, because in some cases a command is placed in a
 * compound that does its own selection handling, so the selection handling
 * features built into the individual commands needs to be disabled.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractEditCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edit command that updates the selection.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   */
  public AbstractEditCommand(final SelectionOwner panel)
  {
    this(panel, null, true);
  }

  /**
   * Creates a new edit command that updates the selection.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   * @param  name              The description of the command.
   */
  public AbstractEditCommand(final SelectionOwner panel,
                             final String name)
  {
    this(panel, name, true);
  }

  /**
   * Creates a new edit command.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public AbstractEditCommand(final SelectionOwner panel,
                             final boolean updatesSelection)
  {
    this(panel, null, updatesSelection);
  }

  /**
   * Creates a new edit command.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   * @param  name              The description of the command.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public AbstractEditCommand(final SelectionOwner panel,
                             final String name,
                             final boolean updatesSelection)
  {
    mPanel = panel;
    mName = name;
    mUpdatesSelection = updatesSelection;
  }
        

  //#########################################################################
  //# Simple Access
  public SelectionOwner getPanel()
  {
    return mPanel;
  }

  public void setName(final String name)
  {
    mName = name;
  }

  public boolean getUpdatesSelection()
  {
    return mUpdatesSelection;
  }

  public void setUpdatesSelection(final boolean updatesSelection)
  {
    mUpdatesSelection = updatesSelection;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public String getName()
  {
    return mName != null ? mName : getClass().getName();
  }

  public boolean isSignificant()
  {
    return true;
  }


  //#########################################################################
  //# Data Members
  private final SelectionOwner mPanel;
  private String mName;
  private boolean mUpdatesSelection;

}
