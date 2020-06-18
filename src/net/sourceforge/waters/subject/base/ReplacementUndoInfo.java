//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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


/**
 * An undo information record representing a single assignment of a member.
 * Contains the old and new value of the assignment, which can be of any
 * type. When executing an assignment, object-type values will not be cloned,
 * so the record contains the exact objects to be assigned. Insertions and
 * deletions can be represented by using <CODE>null</CODE> for the old or
 * new value.
 *
 * @author Robi Malik
 */

public class ReplacementUndoInfo implements UndoInfo
{

  //#########################################################################
  //# Constructors
  public ReplacementUndoInfo(final Object oldValue,
                             final Object newValue)
  {
    this(-1, oldValue, newValue);
  }

  public ReplacementUndoInfo(final int index,
                             final Object oldValue,
                             final Object newValue)
  {
    mIndex = index;
    mOldValue = oldValue;
    mNewValue = newValue;
  }

  public ReplacementUndoInfo(final int index,
                             final int oldValue,
                             final int newValue)
  {
    mIndex = index;
    mOldValue = oldValue;
    mNewValue = newValue;
  }

  public ReplacementUndoInfo(final int index,
                             final boolean oldValue,
                             final boolean newValue)
  {
    mIndex = index;
    mOldValue = oldValue;
    mNewValue = newValue;
  }

  public ReplacementUndoInfo(final int index,
                             final double oldValue,
                             final double newValue)
  {
    mIndex = index;
    mOldValue = oldValue;
    mNewValue = newValue;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.UndoInfo
  public ModelChangeEvent redo(final Subject parent)
  {
    return parent.assignMember(mIndex, mOldValue, mNewValue);
  }

  public ModelChangeEvent undo(final Subject parent)
  {
    return parent.assignMember(mIndex, mNewValue, mOldValue);
  }


  //#########################################################################
  //# Data Members
  private final int mIndex;
  private final Object mOldValue;
  private final Object mNewValue;

}
