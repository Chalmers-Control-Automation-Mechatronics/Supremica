//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ReplacementUndoInfo
//###########################################################################
//# $Id$
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
  public ReplacementUndoInfo(final Object oldValue, final Object newValue)
  {
    mOldValue = oldValue;
    mNewValue = newValue;
  }

  public ReplacementUndoInfo(final int oldValue, final int newValue)
  {
    mOldValue = oldValue;
    mNewValue = newValue;
  }

  public ReplacementUndoInfo(final boolean oldValue, final boolean newValue)
  {
    mOldValue = oldValue;
    mNewValue = newValue;
  }

  public ReplacementUndoInfo(final double oldValue, final double newValue)
  {
    mOldValue = oldValue;
    mNewValue = newValue;
  }


  //#########################################################################
  //# Simple Access
  public Object getValue(final UndoInfo.Mode mode)
  {
    switch (mode) {
    case UNDO:
      return mOldValue;
    case REDO:
      return mNewValue;
    default:
      throw new IllegalArgumentException("Unknown undo mode " + mode + "!");
    }
  }

  public Object getOldValue()
  {
    return mOldValue;
  }

  public Object getNewValue()
  {
    return mNewValue;
  }


  //#########################################################################
  //# Data Members
  private final Object mOldValue;
  private final Object mNewValue;

}
