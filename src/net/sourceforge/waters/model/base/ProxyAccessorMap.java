//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorMap
//###########################################################################
//# $Id: ProxyAccessorMap.java,v 1.3 2006-08-18 06:39:29 robi Exp $
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

public interface ProxyAccessorMap<P extends Proxy>
  extends Map<ProxyAccessor<P>,P>
{

  //#########################################################################
  //# Access as Proxy Set
  public boolean addProxy(P proxy);

  public boolean addAll(Collection<? extends P> collection);

  public boolean containsProxy(Object item);

  public boolean containsAll(Collection<?> collection);

  public boolean equalsByAccessorEquality(ProxyAccessorMap<P> partner);

  public int hashCodeByAccessorEquality();

  public Iterator<P> iterator();

  public boolean removeProxy(Object item);

  public boolean removeAll(Collection<?> collection);

}
