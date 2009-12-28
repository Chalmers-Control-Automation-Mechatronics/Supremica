//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Map;

/**
 * An alternative map interface that allows {@link Proxy} keys to
 * be treated using alternative equalities.
 * A proxy accessor map essentially is a map that maps {@link ProxyAccessor}
 * objects to their associated values. This interface provides some more
 * convenient access to the keys as proxies.
 *
 * @author Robi Malik
 */

public interface ProxyAccessorMap<P extends Proxy,V>
  extends Map<ProxyAccessor<P>,V>
{

  //#########################################################################
  //# Access as Proxy Set
  public boolean containsProxyKey(P proxy);

  public <PP extends P> ProxyAccessor<PP> createAccessor(PP proxy);

  public V getByProxy(P proxy);

  public V putByProxy(P proxy, V value);

  public void putAllByProxies(Map<? extends P, ? extends V> map);

  public boolean removeProxy(P proxy);

  public boolean removeAllProxies(Collection<? extends P> collection);

}
