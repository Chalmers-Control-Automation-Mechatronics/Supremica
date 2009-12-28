//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   AbstractHashCodeVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A visitor to compute hash code for {@link Proxy} objects based on their
 * contents. It can be parameterised to respect or not to respect the geometry
 * information for in some {@link Proxy} objects.
 *
 * @see AbstractEqualityVisitor
 * @author Robi Malik
 */

public abstract class AbstractHashCodeVisitor
  implements ProxyVisitor
{

  //#########################################################################
  //# Invocation
  public AbstractHashCodeVisitor()
  {
    this(false);
  }

  public AbstractHashCodeVisitor(final boolean geo)
  {
    mIsRespectingGeometry = geo;
  }


  //#########################################################################
  //# Simple Access
  public boolean isRespectingGeometry()
  {
    return mIsRespectingGeometry;
  }


  //#########################################################################
  //# Invocation
  public int hashCode(final Proxy proxy)
  {
    try {
      return getProxyHashCode(proxy);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.base.ProxyVisitor
  public Integer visitProxy(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return iface.hashCode();
  }

  public Integer visitGeometryProxy(final GeometryProxy proxy)
  {
    return visitProxy(proxy);
  }

  public Integer visitNamedProxy(final NamedProxy proxy)
  {
    int result = visitProxy(proxy);
    final String name = proxy.getName();
    result *= 5;
    result += name.hashCode();
    return result;
  }

  public Integer visitDocumentProxy(final DocumentProxy proxy)
  {
    int result = visitNamedProxy(proxy);
    final String comment = proxy.getComment();
    result *= 5;
    result += getOptionalHashCode(comment);
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected int getOptionalHashCode(final Object item)
  {
    if (item == null) {
      return HASH_NULL;
    } else {
      return item.hashCode();
    }
  }

  protected int getProxyHashCode(final Proxy proxy)
    throws VisitorException
  {
    if (proxy == null) {
      return HASH_NULL;
    } else {
      return (Integer) proxy.acceptVisitor(this);
    }
  }

  protected int getRefHashCode(final NamedProxy proxy)
    throws VisitorException
  {
    if (proxy == null) {
      return HASH_NULL;
    } else {
      return proxy.refHashCode();
    }
  }

  protected int getCollectionHashCode(final Collection<? extends Proxy> set)
    throws VisitorException
  {
    int result = 0;
    for (final Proxy proxy : set) {
      result += (Integer) proxy.acceptVisitor(this);
    }
    return result;
  }

  protected int getListHashCode(final List<? extends Proxy> list)
    throws VisitorException
  {
    int result = 0;
    for (final Proxy proxy : list) {
      result *= 5;
      result += (Integer) proxy.acceptVisitor(this);
    }
    return result;
  }

  protected int getRefCollectionHashCode
      (final Collection<? extends NamedProxy> coll)
    throws VisitorException
  {
    int result = 0;
    for (final NamedProxy proxy : coll) {
      result += proxy.refHashCode();
    }
    return result;
  }

  protected int getRefSetHashCode(final Collection<? extends NamedProxy> coll)
    throws VisitorException
  {
    if (coll instanceof Set<?>) {
      return getRefCollectionHashCode(coll);
    } else {
      final int size = coll.size();
      final Set<String> names = new HashSet<String>(size);
      int result = 0;
      for (final NamedProxy proxy : coll) {
        final String name = proxy.getName();
        if (names.add(name)) {
          result += proxy.refHashCode();
        }
      }
      return result;
    }
  }

  protected int getRefListHashCode(final List<? extends NamedProxy> list)
    throws VisitorException
  {
    int result = 0;
    for (final NamedProxy proxy : list) {
      result *= 5;
      result += proxy.refHashCode();
    }
    return result;
  }

  protected int getRefMapHashCode
      (final Map<? extends NamedProxy,? extends NamedProxy> map)
    throws VisitorException
  {
    int result = 0;
    for (final Map.Entry<? extends NamedProxy,? extends NamedProxy> entry :
         map.entrySet()) {
      final NamedProxy key = entry.getKey();
      final NamedProxy value = entry.getValue();
      result += key.refHashCode() + 5 * value.refHashCode();
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRespectingGeometry;


  //#########################################################################
  //# Class Constants
  /**
   * A cryptic constant returned as hash code for <CODE>null</CODE>
   * references.
   */
  private static final int HASH_NULL = 0xabababab;

}
