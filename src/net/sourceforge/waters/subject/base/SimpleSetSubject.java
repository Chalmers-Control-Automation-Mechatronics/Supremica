//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   SimpleSetSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.Set;


/**
 * <P>A marker interface identifying implementations of the {@link
 * Set} interface that are also subjects ({@link Subject}). This is
 * simple kind of set that may accept any types of elements---not
 * just subjects.</P>
 *
 * @author Robi Malik
 */

public interface SimpleSetSubject<E>
  extends Set<E>, SimpleCollectionSubject<E>
{

  //#########################################################################
  //# Cloning and Assigning
  public SimpleSetSubject<E> clone();

  /**
   * Creates assignment instructions to replaces the contents of this set
   * by the contents of another set. This method attempts to produce a
   * minimal sequence of deletions and insertions to convert this set
   * to the given new set.
   * @param  newSet   The set containing the data after the intended
   *                  assignment.
   * @param  boundary Set of unchanged {@link Subject}s. Any children of
   *                  the receiving subject contained in this set will be
   *                  assumed and changed and not recursed into. The
   *                  boundary can be <CODE>null</CODE> to force full
   *                  recursion.
   * @return Undo information containing a minimal assignment sequence.
   */
  public UndoInfo createUndoInfo(Set<? extends E> newSet,
                                 Set<? extends Subject> boundary);

}
