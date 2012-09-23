//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   SetSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.Set;


/**
 * <P>A marker interface identifying implementations of the {@link
 * Set} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface SetSubject<P extends ProxySubject>
  extends Set<P>, CollectionSubject<P>
{

  //#########################################################################
  //# Cloning and Assigning
  /**
   * Creates assignment instructions to replaces the contents of this set
   * by the contents of another set. This method attempts to produce a
   * minimal sequence of deletions and insertions to convert this set
   * to the given new set.
   * @param  newSet   The set containing the data after the intended
   *                  assignment.
   * @return Undo information containing a minimal assignment sequence.
   */
  public UndoInfo createUndoInfo(final Set<? extends P> newSet);

}
