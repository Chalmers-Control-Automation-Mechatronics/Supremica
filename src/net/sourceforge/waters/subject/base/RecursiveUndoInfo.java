//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   RecursiveUndoInfo
//###########################################################################
//# $Id$
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
