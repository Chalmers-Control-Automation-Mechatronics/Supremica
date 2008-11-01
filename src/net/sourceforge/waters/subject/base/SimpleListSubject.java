//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   SimpleListSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.List;


/**
 * <P>A marker interface identifying implementations of the {@link
 * List} interface that are also subjects ({@link Subject}). This is
 * a simple kind of list that may accept any types of elements---not
 * just subjects.</P>
 *
 * @author Robi Malik
 */

public interface SimpleListSubject<E>
  extends List<E>, SimpleCollectionSubject<E>
{

  //#########################################################################
  //# Cloning
  public SimpleListSubject<E> clone();

  /**
   * Assigns the contents of another list to this list.
   * This method ensures that the contents of this list are equal to the
   * contents of the given list according to the {@link
   * Object#equals(Object) equals()} method, in the given
   * order. Items already contained in this list are resued, and changed in
   * position as needed. Items not contained are cloned from the given
   * list. The method produces as few model change notifications as
   * possible.
   * @param  list  The list to be copied from.
   */
  public void assignFrom(final List<? extends E> list);

}
