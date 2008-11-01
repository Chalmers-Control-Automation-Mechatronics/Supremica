//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashCollectionWithGeometry
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Map;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashCollectionWithGeometry<P extends Proxy>
  extends ProxyAccessorHashCollection<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashCollectionWithGeometry()
  {
  }

  public ProxyAccessorHashCollectionWithGeometry(final int initialCapacity)
  {
    super(initialCapacity);
  }

  public ProxyAccessorHashCollectionWithGeometry(final int initialCapacity,
                                               final float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashCollectionWithGeometry
    (final Map<ProxyAccessor<P>,Integer> map)
  {
    super(map);
  }

  public ProxyAccessorHashCollectionWithGeometry
    (final Collection<? extends P> collection)
  {
    super(collection);
  }


  //#########################################################################
  //# Overrides for abstract base class ProxyAccessorHashCollection
  public ProxyAccessor<P> createAccessor(final P proxy)
  {
    return new ProxyAccessorWithGeometry<P>(proxy);
  }

}
