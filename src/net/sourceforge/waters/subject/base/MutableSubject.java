//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   MutableSubject
//###########################################################################
//# $Id: MutableSubject.java,v 1.3 2007-02-12 21:38:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>The common base class for all Waters mutable elements in the
 * <I>subject</I> implementation.</P>
 *
 * <P>This is the abstract base class of all mutable Waters elements
 * in the <I>subject</I> implementation. It provides the basic functionality
 * of a mutable object.</P>
 * 
 * @author Robi Malik
 */

public abstract class MutableSubject
  extends AbstractSubject
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty element.
   */
  protected MutableSubject()
  {
  }

  /**
   * Creates a copy of an element.
   * @param  partner     The object to be copied from.
   */
  protected MutableSubject(final Proxy partner)
  {
    super(partner);
  }


  //#########################################################################
  //# Cloning
  public MutableSubject clone()
  {
    final MutableSubject cloned = (MutableSubject) super.clone();
    cloned.mObservers = null;
    return cloned;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  public void addModelObserver(final ModelObserver observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<ModelObserver>();
    }
    mObservers.add(observer);
  }

  public void removeModelObserver(final ModelObserver observer)
  {
    if (mObservers != null &&
        mObservers.remove(observer) &&
        mObservers.isEmpty()) {
      mObservers = null;
    }
  }

  public void fireModelChanged(final ModelChangeEvent event)
  {
    if (mObservers != null) {
      // ARGH! People may add or remove observers while we iterate!
      final Collection<ModelObserver> copy =
        new ArrayList<ModelObserver>(mObservers);
      for (final ModelObserver observer : copy) {
        observer.modelChanged(event);
      }
    }
    super.fireModelChanged(event);
  }


  //#########################################################################
  //# Convenience Methods
  protected void fireStateChanged()
  {
    final ModelChangeEvent event = ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private Collection<ModelObserver> mObservers;

}
