//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   PlainEventListSubject
//###########################################################################
//# $Id: PlainEventListSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.PlainEventListProxy;


/**
 * The subject implementation of the {@link PlainEventListProxy} interface.
 *
 * @author Robi Malik
 */

public final class PlainEventListSubject
  extends EventListExpressionSubject
  implements PlainEventListProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new plain event list.
   * @param eventList The list of events of the new plain event list.
   */
  public PlainEventListSubject(final Collection<? extends Proxy> eventList)
  {
    super(eventList);
  }

  /**
   * Creates a new plain event list using default values.
   * This constructor creates a plain event list with
   * an empty list of events.
   */
  public PlainEventListSubject()
  {
    this(emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public PlainEventListSubject clone()
  {
    return (PlainEventListSubject) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitPlainEventListProxy(this);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }

}
