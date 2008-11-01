//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   EventElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.des;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.plain.base.NamedElement;

import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>An event used by the automata in a DES.</P>
 *
 * <P>This is a simple immutable implementation of the {@link EventProxy}
 * interface.</P>
 *
 * @author Robi Malik
 */

public final class EventElement
  extends NamedElement
  implements EventProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an event.
   * @param  name        The name of the new event.
   * @param  kind        The kind of the new event.
   * @param  observable  <CODE>true</CODE> if the event is to be observable,
   *                     <CODE>false</CODE> otherwise.
   */
  EventElement(final String name,
               final EventKind kind,
               final boolean observable)
  {
    super(name);
    mKind = kind;
    mIsObservable = observable;
  }

  /**
   * Creates an observable event.
   * @param  name        The name of the new event.
   * @param  kind        The kind of the new event.
   */
  EventElement(final String name, final EventKind kind)
  {
    this(name, kind, true);
  }


  //#########################################################################
  //# Cloning
  public EventElement clone()
  {
    return (EventElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitEventProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.EventProxy
  public EventKind getKind()
  {
    return mKind;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<EventProxy> getProxyInterface()
  {
    return EventProxy.class;
  }

  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventProxy event = (EventProxy) partner;
      return
	mKind.equals(event.getKind()) &&
	(mIsObservable == event.isObservable());
    } else {
      return false;
    }    
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mKind.hashCode();
    result *= 5;
    if (mIsObservable) {
      result++;
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final EventKind mKind;
  private final boolean mIsObservable;

}
