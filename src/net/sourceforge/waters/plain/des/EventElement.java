//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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
   * @param  attribs     The attribute map for the new event,
   *                     or <CODE>null</CODE> if empty.
   */
  EventElement(final String name,
               final EventKind kind,
               final boolean observable,
               final Map<String,String> attribs)
  {
    super(name);
    mKind = kind;
    mIsObservable = observable;
    if (attribs == null) {
      mAttributes = Collections.emptyMap();
    } else {
      final Map<String,String> attribscopy = new TreeMap<String,String>(attribs);
      mAttributes = Collections.unmodifiableMap(attribscopy);
    }
  }

  /**
   * Creates an event without attributes.
   * @param  name        The name of the new event.
   * @param  kind        The kind of the new event.
   * @param  observable  <CODE>true</CODE> if the event is to be observable,
   *                     <CODE>false</CODE> otherwise.
   */
  EventElement(final String name,
               final EventKind kind,
               final boolean observable)
  {
    this(name, kind, observable, null);
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

  public Map<String,String> getAttributes()
  {
    return mAttributes;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<EventProxy> getProxyInterface()
  {
    return EventProxy.class;
  }


  //#########################################################################
  //# Data Members
  private final EventKind mKind;
  private final boolean mIsObservable;
  private final Map<String,String> mAttributes;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
