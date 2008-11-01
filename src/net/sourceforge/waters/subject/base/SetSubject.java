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
   * Assigns the contents of another set to this set.
   * This method ensures that the contents of this set are equal to the
   * contents of the given set according to the {@link
   * net.sourceforge.waters.model.base.Proxy#equalsWithGeometry(Proxy)
   * equalsWithGeometry()} method.
   * Items already contained in this set are resued. Items not contained
   * are cloned from the given set. The method produces as few model change
   * notifications as possible.
   * @param  set   The set to be copied from.
   */
  public void assignFrom(final Set<? extends P> set);

}
