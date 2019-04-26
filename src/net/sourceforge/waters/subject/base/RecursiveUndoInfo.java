//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.subject.base;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * An undo information record representing a sequence of assignments within
 * an object or a list. When a {@link Subject} is modified by assignment,
 * this operation is represented by a sequence of assignments to its
 * members, stored in a RecursiveUndoInfo object. Each entry is an
 * {@link UndoInfo} containing further information how to modify the
 * corresponding member. The exact interpretation of the contents of the
 * list depends on the type of the subject being modified.
 *
 * @author Robi Malik
 */

public class RecursiveUndoInfo
  implements UndoInfo
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new recursive undo information record with an empty list
   * of assignments.
   * @param  subject  The subject the assignment sequence applies to.
   */
  public RecursiveUndoInfo(final Subject subject)
  {
    mSubject = subject;
    mChildren = new LinkedList<UndoInfo>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.UndoInfo
  public ModelChangeEvent redo(final Subject parent)
  {
    final List<ModelChangeEvent> events = new LinkedList<ModelChangeEvent>();
    for (final UndoInfo child : mChildren) {
      final ModelChangeEvent event = child.redo(mSubject);
      if (event != null && !events.contains(event)) {
        events.add(event);
      }
    }
    for (final ModelChangeEvent event : events) {
      event.fire();
    }
    return null;
  }

  public ModelChangeEvent undo(final Subject parent)
  {
    final List<ModelChangeEvent> events = new LinkedList<ModelChangeEvent>();
    final int end = mChildren.size();
    final ListIterator<UndoInfo> iter = mChildren.listIterator(end);
    while (iter.hasPrevious()) {
      final UndoInfo child = iter.previous();
      final ModelChangeEvent event = child.undo(mSubject);
      if (event != null && !events.contains(event)) {
        events.add(event);
      }
    }
    for (final ModelChangeEvent event : events) {
      event.fire();
    }
    return null;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the subject this assignment sequence applies to.
   */
  public Subject getSubject()
  {
    return mSubject;
  }

  /**
   * Adds an assignment to the end of the assignment sequence.
   */
  public void add(final UndoInfo child)
  {
    mChildren.add(child);
  }

  /**
   * Tests whether this assignment sequence contains any assignments,
   * i.e., whether applying it will change the subject.
   */
  public boolean isEmpty()
  {
    return mChildren.isEmpty();
  }


  //#########################################################################
  //# Data Members
  private final Subject mSubject;
  private final List<UndoInfo> mChildren;

}
