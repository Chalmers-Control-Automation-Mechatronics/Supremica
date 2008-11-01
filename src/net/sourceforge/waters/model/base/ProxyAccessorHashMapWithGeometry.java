//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashMapWithGeometry
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Map;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashMapWithGeometry<P extends Proxy>
  extends ProxyAccessorHashMap<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashMapWithGeometry()
  {
  }

  public ProxyAccessorHashMapWithGeometry(final int initialCapacity)
  {
    super(initialCapacity);
  }

  public ProxyAccessorHashMapWithGeometry(final int initialCapacity,
                                        final float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashMapWithGeometry
    (final Map<? extends ProxyAccessor<P>, ? extends P> map)
  {
    super(map);
  }

  public ProxyAccessorHashMapWithGeometry
    (final Collection<? extends P> collection)
  {
    super(collection);
  }


  //#########################################################################
  //# Overrides for abstract base class ProxyAccessorHashMap
  public <PP extends Proxy> ProxyAccessor<PP> createAccessor(final PP proxy)
  {
    return new ProxyAccessorWithGeometry<PP>(proxy);
  }

}
