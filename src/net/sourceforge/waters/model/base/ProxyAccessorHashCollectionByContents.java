//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorHashCollectionByContents
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Map;


/**
 * @author Robi Malik
 */

public class ProxyAccessorHashCollectionByContents<P extends Proxy>
  extends ProxyAccessorHashCollection<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorHashCollectionByContents()
  {
  }

  public ProxyAccessorHashCollectionByContents(final int initialCapacity)
  {
    super(initialCapacity);
  }

  public ProxyAccessorHashCollectionByContents(final int initialCapacity,
                                               final float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }

  public ProxyAccessorHashCollectionByContents
    (final Map<ProxyAccessor<P>,Integer> map)
  {
    super(map);
  }

  public ProxyAccessorHashCollectionByContents
    (final Collection<? extends P> collection)
  {
    super(collection);
  }


  //#########################################################################
  //# Overrides for abstract base class ProxyAccessorHashCollection
  public <PP extends P> ProxyAccessor<PP> createAccessor(final PP proxy)
  {
    return new ProxyAccessorByContents<PP>(proxy);
  }

}
