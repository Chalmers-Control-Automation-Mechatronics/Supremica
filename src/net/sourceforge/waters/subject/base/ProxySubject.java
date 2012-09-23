//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ProxySubject
//###########################################################################
//# $Id$
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
   * Creates assignment instructions to replaces the contents of this list
   * by the contents of another list. This method attempts to produce a
   * minimal sequence of deletions and insertions to convert this set
   * to the given new set.
   * @param  newSet   The list containing the data after the intended
   *                  assignment.
   * @return Undo information containing a minimal assignment sequence.
   */
  public UndoInfo createUndoInfo(ProxySubject newState);

}
