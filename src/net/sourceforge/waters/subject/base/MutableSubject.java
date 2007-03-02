//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   MutableSubject
//###########################################################################
//# $Id: MutableSubject.java,v 1.4 2007-03-02 05:21:14 robi Exp $
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
  //# Cloning and Assigning
  public MutableSubject clone()
  {
    final MutableSubject cloned = (MutableSubject) super.clone();
    cloned.mObservers = null;
    return cloned;
  }

  /**
   * Assigns the contents of another subject to this subject.
   * This method ensures that the contents of this subject are equal to the
   * contents of the given subject according to the {@link
   * Proxy#equalsWithGeometry(Proxy) equalsWithGeometry()} method. Members
   * that differ are cloned or recursively assigned from the other
   * subject. The method produces as few state change notifications as
   * possible.
   * @param  partner  The subject to be copied from.
   * @return <CODE>true</CODE> if a state changed notification needs to
   *         be fired. To reduce the amount of state change notifications
   *         fired, implementations may suppress them and return
   *         <CODE>true</CODE>. All <EM>final</EM> subclasses must override
   *         this method, check the return value of the superclass method
   *         and call {@link #fireStateChanged()} as appropriate.
   * @throws ClassCastException to indicate that the argument is not of
   *         the same type as this subject.
   */
  public boolean assignFrom(final ProxySubject partner)
  {
    return false;
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

  protected void fireGeometryChanged(final Object geo)
  {
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, geo);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private Collection<ModelObserver> mObservers;

}
