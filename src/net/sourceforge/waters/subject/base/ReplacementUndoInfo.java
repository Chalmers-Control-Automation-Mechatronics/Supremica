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
