//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   StateLookupFactory
//###########################################################################
//# $Id: EventLookupFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.List;

import net.sourceforge.waters.model.base.IndexedCollectionProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.des.EventRefType;
import net.sourceforge.waters.xsd.des.EventRefListType;


class EventLookupFactory implements ProxyFactory {

  //#########################################################################
  //# Constructor
  EventLookupFactory(final IndexedCollectionProxy eventmap)
  {
    mEventMap = eventmap;
  }


  //#########################################################################
  //# Event Lookup
  EventProxy findEvent(final String name)
    throws NameNotFoundException
  {
    return (EventProxy) mEventMap.find(name);
  }

  boolean contains(final EventProxy event)
  {
    return mEventMap.contains(event);
  }


  //#########################################################################
  //# Interface waters.model.base.ProxyFactory
  public Proxy createProxy(ElementType element)
    throws NameNotFoundException
  {
    final EventRefType ref = (EventRefType) element;
    final String name = ref.getName();
    return findEvent(name);
  }

  public List getList(final ElementType parent)
  {
    final EventRefListType reflist = (EventRefListType) parent;
    return reflist.getList();
  }


  //#########################################################################
  //# Data Members
  private final IndexedCollectionProxy mEventMap;

}
