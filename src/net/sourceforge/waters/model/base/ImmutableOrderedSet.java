//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   IndexedHashSet
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import gnu.trove.THashSet;

import java.io.Serializable;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * <P>
 * A immutable set implementation that guarantees the ordering of elements as
 * passed to the constructor.
 * </P>
 *
 * <P>
 * This implementation is based an immutable {@link ArrayList} containing the
 * set elements. An efficient check for duplicates is performed on
 * construction, but no further performance improvements are made. The
 * {@link Set#contains(Object) contains()} operation is of linear time
 * complexity.
 * </P>
 *
 * <P>
 * All elements in an <CODE>ImmutableOrderedSet</CODE> must be of type
 * {@link NamedProxy}.
 * </P>
 *
 * @author Robi Malik
 */

public class ImmutableOrderedSet<P extends NamedProxy> extends AbstractSet<P>
  implements Cloneable, Serializable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty ordered set.
   */
  public ImmutableOrderedSet()
  {
    mProxyList = Collections.emptyList();
  }

  /**
   * Creates and initialises a ordered set.
   *
   * @param input
   *          A collection of objects that constitute the initial contents of
   *          the new set.
   * @throws DuplicateNameException
   *           to indicate that the input collection contains two elements
   *           with the same name.
   */
  public ImmutableOrderedSet(final Collection<? extends P> input)
  {
    if (input == null) {
      mProxyList = Collections.emptyList();
    } else {
      final int size = input.size();
      final Set<String> names = new THashSet<String>(size);
      final List<P> list = new ArrayList<P>(size);
      for (final P proxy : input) {
        if (proxy != null) {
          final String name = proxy.getName();
          if (names.add(name)) {
            list.add(proxy);
          } else {
            throw createDuplicateName(name);
          }
        }
      }
      mProxyList = Collections.unmodifiableList(list);
    }
  }

  //#########################################################################
  //# Cloning
  public ImmutableOrderedSet<P> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<ImmutableOrderedSet<P>> clazz =
        (Class<ImmutableOrderedSet<P>>) getClass();
      final ImmutableOrderedSet<P> cloned = clazz.cast(super.clone());
      cloned.mProxyList = new ArrayList<P>(mProxyList);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  //#########################################################################
  //# Interface java.util.Set
  public Iterator<P> iterator()
  {
    return mProxyList.iterator();
  }

  public int size()
  {
    return mProxyList.size();
  }

  //#########################################################################
  //# Error Messages
  protected DuplicateNameException createDuplicateName(final String name)
  {
    final StringBuffer buffer = new StringBuffer();
    appendContainerName(buffer);
    buffer.append(" contains more than one ");
    appendItemKindName(buffer);
    buffer.append(" named '");
    buffer.append(name);
    buffer.append("'!");
    return new DuplicateNameException(buffer.toString());
  }

  protected void appendContainerName(final StringBuffer buffer)
  {
    final String name = ProxyTools.getShortClassName(this);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuffer buffer)
  {
    buffer.append("item");
  }

  //#########################################################################
  //# Data Members
  /**
   * The contents of this set in the order specified by the constructor. An
   * immutable list.
   */
  private List<P> mProxyList;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
