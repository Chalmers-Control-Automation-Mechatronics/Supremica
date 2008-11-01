//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashMapByContents
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Map;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashMapByContents<P extends Proxy>
  extends ProxyAccessorHashMap<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashMapByContents()
  {
  }

  public ProxyAccessorHashMapByContents(final int initialCapacity)
  {
    super(initialCapacity);
  }

  public ProxyAccessorHashMapByContents(final int initialCapacity,
                                        final float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashMapByContents
    (final Map<? extends ProxyAccessor<P>, ? extends P> map)
  {
    super(map);
  }

  public ProxyAccessorHashMapByContents
    (final Collection<? extends P> collection)
  {
    super(collection);
  }


  //#########################################################################
  //# Overrides for abstract base class ProxyAccessorHashMap
  public <PP extends Proxy> ProxyAccessor<PP> createAccessor(final PP proxy)
  {
    return new ProxyAccessorByContents<PP>(proxy);
  }

}
