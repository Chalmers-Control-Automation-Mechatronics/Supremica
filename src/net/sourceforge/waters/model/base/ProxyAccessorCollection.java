//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorCollection
//###########################################################################
//# $Id: ProxyAccessorCollection.java,v 1.1 2006-08-18 06:39:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * An alternative collection interface that allows {@link Proxy} objects to
 * be treated using alternative equalities.
 * This interface supports actual collections, where more than a single
 * instance of a set of equal items may be contained, and where the order
 * is immaterial.  A proxy accessor map essentially is a map that maps
 * {@link ProxyAccessor} objects to the number of times they are contained
 * in the collection. This interface provides set-like access to this set.
 *
 * @see ProxyAccessorMap
 * @author Robi Malik
 */

public interface ProxyAccessorCollection<P extends Proxy>
  extends Map<ProxyAccessor<P>,Integer>
{

  //#########################################################################
  //# Access as Proxy Collection
  public boolean addProxy(P proxy);

  public boolean addAll(Collection<? extends P> collection);

  public boolean containsProxy(P proxy);

  public boolean containsAll(Collection<? extends P> collection);

  public boolean equalsByAccessorEquality(ProxyAccessorCollection<P> partner);

  public int hashCodeByAccessorEquality();

  public Iterator<P> iterator();

  public boolean removeProxy(P proxy);

  public boolean removeAll(Collection<? extends P> collection);

}
