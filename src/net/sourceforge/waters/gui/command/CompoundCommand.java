//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: WATERS GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CompoundCommand
//###########################################################################
//# $Id: CompoundCommand.java,v 1.8 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * An abstraction of a command that can consist of several steps (other
 * commands) to be executed sequentially.
 *
 * @author Simon Ware
 */

public class CompoundCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new compound command with a default name.
   */
  public CompoundCommand()
  {
    this(null);
  }

  /**
   * Creates a new compound command with the given name.
   * @param  name    The description to be used for the new command,
   *                 or <CODE>null</CODE> to use a default name.
   */
  public CompoundCommand(final String name)
  {
    mInProgress = true;
    mCommands = new LinkedList<Command>();
    mDescription = name == null ? "Compound Command" : name;
  }


  //#########################################################################
  //# Simple Access
  public boolean isSignificant()
  {
    for (final Command c : mCommands) {
      if (c.isSignificant()) {
        return true;
      }
    }
    return false;
  }

  public String getName()
  {
    return mDescription;
  }

  public void setName(final String description)
  {
    mDescription = description;
  }

  public boolean isEmpty()
  {
    return mCommands.isEmpty();
  }


  //#########################################################################
  //# Command Construction
  public boolean addCommand(final Command cmd)
  {
    if (mInProgress && cmd != null) {
      mCommands.add(cmd);
      return true;
    } else {
      return false;
    }
  }

  public boolean addCommands(final List<? extends Command> commands)
  {
    if (mInProgress) {
      mCommands.addAll(commands);
      return true;
    } else {
      return false;
    }
  }

  public void end()
  {
    mInProgress = false;
  }


  //#########################################################################
  //# Command Execution
  public void execute()
  {
    for (final Command cmd : mCommands) {
      cmd.execute();
    }
  }

  public void undo()
  {
    final ListIterator<Command> li = mCommands.listIterator(mCommands.size());
    while (li.hasPrevious()) {
      li.previous().undo();
    }
  }


  //#########################################################################
  //# Data Members
  private final List<Command> mCommands;
  private String mDescription;
  private boolean mInProgress;

}
