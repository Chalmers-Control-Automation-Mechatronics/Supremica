//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   PlainEventListElement
//###########################################################################
//# $Id: PlainEventListElement.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.PlainEventListProxy;


/**
 * An immutable implementation of the {@link PlainEventListProxy} interface.
 *
 * @author Robi Malik
 */

public final class PlainEventListElement
  extends EventListExpressionElement
  implements PlainEventListProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new plain event list.
   * @param eventList The list of events of the new plain event list, or <CODE>null</CODE> if empty.
   */
  public PlainEventListElement(final Collection<? extends Proxy> eventList)
  {
    super(eventList);
  }

  /**
   * Creates a new plain event list using default values.
   * This constructor creates a plain event list with
   * an empty list of events.
   */
  public PlainEventListElement()
  {
    this(emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public PlainEventListElement clone()
  {
    return (PlainEventListElement) super.clone();
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
