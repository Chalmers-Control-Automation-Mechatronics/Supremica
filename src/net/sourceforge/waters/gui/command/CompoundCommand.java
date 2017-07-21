//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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

  public void setUpdatesSelection(final boolean update)
  {
    for (final Command cmd : mCommands) {
      cmd.setUpdatesSelection(update);
    }
  }


  //#########################################################################
  //# Data Members
  private final List<Command> mCommands;
  private String mDescription;
  private boolean mInProgress;

}
