//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: WATERS GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CompoundCommand
//###########################################################################
//# $Id: CompoundCommand.java,v 1.6 2007-06-11 15:07:51 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
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
  public CompoundCommand()
  {
    this("Compound Command");
  }

  public CompoundCommand(final String name)
  {
    mInProgress = true;
    mCommands = new ArrayList();
    mDescription = name;
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

  public boolean isEmpty()
  {
    return mCommands.isEmpty();
  }


  //#########################################################################
  //# Command Construction
  public boolean addCommand(Command c)
  {
    if (mInProgress && c != null) {
      mCommands.add(c);
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
    for (final Command c : mCommands) {
      c.execute();
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
  private final String mDescription;
  private boolean mInProgress;

}
