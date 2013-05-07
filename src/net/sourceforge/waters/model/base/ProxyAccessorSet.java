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
import java.util.Iterator;
import java.util.Map;

/**
 * An alternative set interface that allows {@link Proxy} objects to
 * be treated using alternative equalities.
 * A proxy accessor map essentially is a map that maps {@link ProxyAccessor}
 * objects to the actual {@link Proxy} objects. This interface provides
 * set-like access to this set.
 *
 * @see ProxyAccessorCollection
 * @author Robi Malik
 */

public interface ProxyAccessorSet<P extends Proxy>
  extends Map<ProxyAccessor<P>,P>, Iterable<P>
{

  //#########################################################################
  //# Access as Proxy Set
  public boolean addProxy(P proxy);

  public boolean addAll(Collection<? extends P> collection);

  public boolean containsProxy(P proxy);

  public boolean containsAll(Collection<? extends P> collection);

  public <PP extends P> ProxyAccessor<P> createAccessor(PP proxy);

  public boolean equalsByAccessorEquality(ProxyAccessorSet<P> partner);

  public int hashCodeByAccessorEquality();

  @Override
  public Iterator<P> iterator();

  public boolean removeProxy(P proxy);

  public boolean removeAll(Collection<? extends P> collection);

}
