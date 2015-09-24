//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.base;

import gnu.trove.set.hash.THashSet;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * <P>A immutable set implementation that guarantees the ordering of elements
 * as passed to the constructor.</P>
 *
 * <P>This implementation is based an immutable {@link ArrayList} containing
 * the set elements. An efficient check for duplicates is performed on
 * construction, but no further performance improvements are made.
 * The {@link Set#contains(Object) contains()} operation is of linear time
 * complexity.</P>
 *
 * <P>All elements in an <CODE>ImmutableOrderedSet</CODE> must be of
 * type {@link NamedProxy}.</P>
 *
 * @author Robi Malik
 */

public class ImmutableOrderedSet<P extends NamedProxy>
  extends AbstractSet<P>
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
   * Creates and initialises an ordered set.
   * @param  input       A collection of objects that constitute the initial
   *                     contents of the new set.
   * @throws DuplicateNameException to indicate that the input collection
   *         contains two elements with the same name.
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
        final String name = proxy.getName();
        if (names.add(name)) {
          list.add(proxy);
        } else {
          throw createDuplicateName(name);
        }
      }
      mProxyList = Collections.unmodifiableList(list);
    }
  }


  //#########################################################################
  //# Cloning
  @Override
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
  @Override
  public Iterator<P> iterator()
  {
    return mProxyList.iterator();
  }

  @Override
  public int size()
  {
    return mProxyList.size();
  }


  //#########################################################################
  //# Error Messages
  protected DuplicateNameException createDuplicateName(final String name)
  {
    final StringBuilder buffer = new StringBuilder();
    appendContainerName(buffer);
    buffer.append(" contains more than one ");
    appendItemKindName(buffer);
    buffer.append(" named '");
    buffer.append(name);
    buffer.append("'!");
    return new DuplicateNameException(buffer.toString());
  }

  protected void appendContainerName(final StringBuilder buffer)
  {
    final String name = ProxyTools.getShortClassName(this);
    buffer.append(name);
  }

  protected void appendItemKindName(final StringBuilder buffer)
  {
    buffer.append("item");
  }


  //#########################################################################
  //# Data Members
  /**
   * The contents of this set in the order specified by the constructor.
   * An immutable list.
   */
  private List<P> mProxyList;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
