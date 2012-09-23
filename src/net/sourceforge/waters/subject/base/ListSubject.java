//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ListSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.List;


/**
 * <P>A marker interface identifying implementations of the {@link
 * List} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface ListSubject<P extends ProxySubject>
  extends List<P>, CollectionSubject<P>
{

  //#########################################################################
  //# Cloning and Assigning
  public ListSubject<P> clone();

  /**
   * Creates assignment instructions to replaces the contents of this list
   * by the contents of another list. This method attempts to produce a
   * minimal sequence of deletions and insertions to convert this set
   * to the given new set.
   * @param  newSet   The list containing the data after the intended
   *                  assignment.
   * @return Undo information containing a minimal assignment sequence.
   */
  public UndoInfo createUndoInfo(final List<? extends P> newList);

}
