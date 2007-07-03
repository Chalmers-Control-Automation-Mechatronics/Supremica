//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ProxySubject
//###########################################################################
//# $Id: ProxySubject.java,v 1.4 2007-07-03 11:20:53 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A marker interface identifying implementations of the {@link
 * Proxy} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface ProxySubject
  extends Proxy, Subject
{

  //#########################################################################
  //# Cloning
  /**
   * Creates and returns a copy of this object.
   * Subjects are cloned by deep copying. All contained objects are
   * cloned as well, ensuring that all references within the clone point
   * to other objects of the clone. However, the cloned subject has no
   * parent, and observers registered on a subject are not transferred
   * to the clone.
   */
  public ProxySubject clone();

  /**
   * Assigns the contents of another subject to this subject.  This method
   * ensures that the contents of this subject are equal to the contents of
   * the given subject according to the {@link
   * net.sourceforge.waters.model.base.Proxy#equalsWithGeometry(Proxy)
   * equalsWithGeometry()} method. Items already contained in this subject
   * are reused if possible, and may be changed in position as
   * needed. Items not contained are cloned from the given subject. The
   * method tries to produce as few model change notifications as possible.
   * @param  partner  The subject to be copied from.
   */
  public boolean assignFrom(ProxySubject partner);

}
