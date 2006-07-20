//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorMap
//###########################################################################
//# $Id: ProxyAccessorMap.java,v 1.2 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
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
