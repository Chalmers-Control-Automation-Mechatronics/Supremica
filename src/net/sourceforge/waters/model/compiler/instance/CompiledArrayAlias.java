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

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.IndexedIdentifierElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;


class CompiledArrayAlias implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledArrayAlias(final String name)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mParentInfo = new RootParentInfo(name);
    mMap = new ProxyAccessorHashMap<>(eq);
  }

  CompiledArrayAlias(final CompiledArrayAlias parent,
                     final SimpleExpressionProxy index)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mParentInfo = new IndexedParentInfo(parent, index);
    mMap = new ProxyAccessorHashMap<>(eq);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return getIdentifier().toString();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.CompiledEvent
  @Override
  public int getKindMask()
  {
    int mask = 0;
    for (final CompiledEvent event : mMap.values()) {
      mask |= event.getKindMask();
    }
    return mask;
  }

  @Override
  public boolean isObservable()
  {
    boolean observable = true;
    for (final CompiledEvent event : mMap.values()) {
      observable &= event.isObservable();
    }
    return observable;
  }

  @Override
  public List<CompiledRange> getIndexRanges()
  {
    return Collections.emptyList();
  }

  @Override
  public CompiledEvent find(final SimpleExpressionProxy index)
    throws UndefinedIdentifierException
  {
    final CompiledEvent result = get(index);
    if (result == null) {
      final ParentInfo info = new IndexedParentInfo(this, index);
      final String name = info.getName();
      throw new UndefinedIdentifierException(name);
    }
    return result;
  }

  @Override
  public SourceInfo getSourceInfo()
  {
    return null;
  }

  @Override
  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return mMap.values().iterator();
  }


  //#########################################################################
  //# Specific Access
  IdentifierProxy getIdentifier()
  {
    return mParentInfo.getIdentifier();
  }

  CompiledEvent get(final SimpleExpressionProxy index)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      mMap.createAccessor(index);
    return mMap.get(accessor);
  }

  void set(final SimpleExpressionProxy index, final CompiledEvent value)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      mMap.createAccessor(index);
    if (!mMap.containsKey(accessor)) {
      mMap.put(accessor, value);
    } else {
      final ParentInfo info = new IndexedParentInfo(this, index);
      final String name = info.getName();
      throw new DuplicateIdentifierException(name);
    }
  }

  void set(final List<SimpleExpressionProxy> indexes,
           final CompiledEvent value)
    throws DuplicateIdentifierException
  {
    if (indexes.isEmpty()) {
      throw new IllegalArgumentException
        ("Index list for new array alias must not be empty!");
    } else {
      final Iterator<SimpleExpressionProxy> iter = indexes.iterator();
      set(iter, value);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ParentInfo getParentInfo()
  {
    return mParentInfo;
  }

  private void set(final Iterator<SimpleExpressionProxy> iter,
                   final CompiledEvent value)
    throws DuplicateIdentifierException
  {
    final SimpleExpressionProxy index = iter.next();
    if (iter.hasNext()) {
      final CompiledEvent next = get(index);
      if (next == null) {
        final CompiledArrayAlias alias = new CompiledArrayAlias(this, index);
        alias.set(iter, value);
        set(index, alias);
      } else if (next instanceof CompiledArrayAlias) {
        final CompiledArrayAlias array = (CompiledArrayAlias) next;
        array.set(iter, value);
      } else {
        final ParentInfo info = new IndexedParentInfo(this, index);
        final String name = info.getName();
        throw new DuplicateIdentifierException(name);
      }
    } else {
      set(index, value);
    }
  }


  //#########################################################################
  //# Local Class ParentInfo
  private abstract class ParentInfo
  {

    //#######################################################################
    //# Naming
    String getName()
    {
      final IdentifierProxy ident = getIdentifier();
      return ident.toString();
    }

    IdentifierProxy getIdentifier()
    {
      final List<SimpleExpressionProxy> empty =
        new LinkedList<SimpleExpressionProxy>();
      return getIdentifier(empty);
    }

    abstract IdentifierProxy getIdentifier
      (final List<SimpleExpressionProxy> indexes);

  }


  //#########################################################################
  //# Local Class RootParentInfo
  private class RootParentInfo extends ParentInfo {

    //#######################################################################
    //# Constructor
    private RootParentInfo(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Naming
    @Override
    IdentifierProxy getIdentifier()
    {
      return new SimpleIdentifierElement(mName);
    }

    @Override
    IdentifierProxy getIdentifier(final List<SimpleExpressionProxy> indexes)
    {
      if (indexes.isEmpty()) {
        return new SimpleIdentifierElement(mName);
      } else {
        return new IndexedIdentifierElement(mName, indexes);
      }
    }

    //#######################################################################
    //# Data Members
    private final String mName;
  }


  //#########################################################################
  //# Local Class IndexedParentInfo
  private class IndexedParentInfo extends ParentInfo {

    //#######################################################################
    //# Constructor
    private IndexedParentInfo(final CompiledArrayAlias parent,
                              final SimpleExpressionProxy index)
    {
      mParent = parent;
      mIndex = index;
    }

    //#######################################################################
    //# Naming
    @Override
    IdentifierProxy getIdentifier(final List<SimpleExpressionProxy> indexes)
    {
      final ParentInfo info = mParent.getParentInfo();
      indexes.add(0, mIndex);
      return info.getIdentifier(indexes);
    }

    //#######################################################################
    //# Data Members
    private final CompiledArrayAlias mParent;
    private final SimpleExpressionProxy mIndex;

  }


  //#########################################################################
  //# Data Members
  private final ParentInfo mParentInfo;
  private final ProxyAccessorMap<SimpleExpressionProxy,CompiledEvent> mMap;

}
