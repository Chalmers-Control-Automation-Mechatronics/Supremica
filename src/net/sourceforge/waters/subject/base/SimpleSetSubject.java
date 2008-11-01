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
   * Assigns the contents of another set to this set.
   * This method ensures that the contents of this set are equal to the
   * contents of the given set according to the {@link
   * Object#equals(Object) equals()} method. Items already
   * contained in this set are resued. Items not contained are cloned from
   * the given set. The method produces as few model change notifications
   * as possible.
   * @param  set   The set to be copied from.
   */
  public void assignFrom(final Set<? extends E> set);

}
