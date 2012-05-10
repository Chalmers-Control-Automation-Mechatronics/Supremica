//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   RecursiveUndoInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * An undo information record representing a sequence of assignments within
 * an object or a list. When a {@link Subject} is modified by assignment,
 * this operation is represented by a sequence of assignments to its
 * members, stored in a RecursiveUndoInfo object. Each entry is an
 * {@link UndoInfo} containing further information how to modify the
 * corresponding member. <CODE>null</CODE> entries are possible, indicating
 * that a member should be left unchanged. The exact interpretation of the
 * contents of the list depends on the type of the subject being modified.
 *
 * @author Robi Malik
 */

public class RecursiveUndoInfo
  implements UndoInfo, Iterable<UndoInfo>
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
  //# Interface java.lang.Iterable<UndoInfo>
  public Iterator<UndoInfo> iterator()
  {
    return mChildren.iterator();
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


  //#########################################################################
  //# Data Members
  private final Subject mSubject;
  private final List<UndoInfo> mChildren;

}
