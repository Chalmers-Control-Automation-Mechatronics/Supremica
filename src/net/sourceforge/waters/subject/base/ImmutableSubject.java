//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ImmutableSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>The common base class for all Waters elements in the <I>subject</I>
 * implementation.</P>
 *
 * <P>This is the abstract base class of all immutable Waters elements
 * in the <I>subject</I> implementation. It provides the basic functionality
 * of an immutable object.</P>
 * 
 * @author Robi Malik
 */

public abstract class ImmutableSubject
  extends AbstractSubject
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty element.
   */
  protected ImmutableSubject()
  {
  }

  /**
   * Creates a copy of an element.
   * @param  partner     The object to be copied from.
   */
  protected ImmutableSubject(final Proxy partner)
  {
    super(partner);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  public void addModelObserver(final ModelObserver observer)
  {
  }

  public void removeModelObserver(final ModelObserver observer)
  {
  }

  public void fireModelChanged(final ModelChangeEvent event)
  {
    throw new UnsupportedOperationException
      ("Trying to fire change for immutable object of class " +
       getClass().getName() + "!");
  }

}
