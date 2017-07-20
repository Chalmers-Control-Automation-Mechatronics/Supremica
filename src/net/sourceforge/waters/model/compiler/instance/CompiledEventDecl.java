//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A compiler-internal representation of an event declaration.</P>
 *
 * <P>This class supports the mapping from a module's event declarations
 * and event expressions to the simple events later to be used in a
 * product DES.</P>
 *
 * @author Robi Malik
 */

class CompiledEventDecl
{

  //#########################################################################
  //# Constructors
  CompiledEventDecl(final CompiledNameSpace namespace,
                    final EventDeclProxy decl,
                    final List<CompiledRange> ranges)
  {
    mNameSpace = namespace;
    mDecl = decl;
    mRanges = ranges;
    int size = 1;
    for (final CompiledRange range : ranges) {
      size *= range.size();
    }
    mIndexValueMap = new HashMap<>(size);
  }


  //#########################################################################
  //# Simple Access
  CompiledNameSpace getNameSpace()
  {
    return mNameSpace;
  }

  EventDeclProxy getEventDeclProxy()
  {
    return mDecl;
  }

  IdentifierProxy getIdentifier()
  {
    return mDecl.getIdentifier();
  }

  EventKind getKind()
  {
    return mDecl.getKind();
  }

  boolean isObservable()
  {
    return mDecl.isObservable();
  }

  int getArity()
  {
    return mRanges.size();
  }

  List<CompiledRange> getRanges()
  {
    return mRanges;
  }

  CompiledRange getRange(final int index)
  {
    return mRanges.get(index);
  }


  //#########################################################################
  //# Auxiliary Methods
  CompiledEvent getCompiledEvent()
  {
    final List<SimpleExpressionProxy> empty = Collections.emptyList();
    return getCompiledEvent(empty);
  }

  CompiledEvent getCompiledEvent(final List<? extends SimpleExpressionProxy> indexes)
  {
    final HashableIndexList key = new HashableIndexList(indexes);
    CompiledEvent result = mIndexValueMap.get(key);
    if (result == null) {
      final List<SimpleExpressionProxy> indexCopy = key.getIndexList();
      if (indexes.size() < mRanges.size()) {
        result = new CompiledArrayEvent(this, indexCopy);
      } else {
        result = new CompiledSingleEvent(this, indexCopy);
      }
      mIndexValueMap.put(key, result);
    }
    return result;
  }

  void checkIndex(final int pos, final SimpleExpressionProxy value)
    throws IndexOutOfRangeException
  {
    final CompiledRange range = mRanges.get(pos);
    if (!range.contains(value)) {
      throw new IndexOutOfRangeException(value, range);
    }
  }


  //#########################################################################
  //# Inner Class HashableIndexList
  private class HashableIndexList
  {
    //#######################################################################
    //# Constructor
    private HashableIndexList(final List<? extends SimpleExpressionProxy> indexes)
    {
      final int size = indexes.size();
      if (size == 0) {
        mIndexes = null;
      } else {
        mIndexes = new SimpleExpressionProxy[size];
        indexes.toArray(mIndexes);
      }
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (other == null || !(other instanceof HashableIndexList)) {
        return false;
      } else {
        final HashableIndexList list = (HashableIndexList) other;
        final ModuleEqualityVisitor equality = mNameSpace.getEquality();
        return equality.isEqualArray(mIndexes, list.mIndexes);
      }
    }

    @Override
    public int hashCode()
    {
      final ModuleEqualityVisitor equality = mNameSpace.getEquality();
      final ModuleHashCodeVisitor visitor = equality.getHashCodeVisitor();
      return visitor.getArrayHashCode(mIndexes);
    }

    @Override
    public String toString()
    {
      return ModuleProxyPrinter.getPrintString(mIndexes);
    }

    //#######################################################################
    //# Simple Access
    private List<SimpleExpressionProxy> getIndexList()
    {
      if (mIndexes == null) {
        return Collections.emptyList();
      } else {
        return Arrays.asList(mIndexes);
      }
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxy[] mIndexes;
  }


  //#########################################################################
  //# Data Members
  private final CompiledNameSpace mNameSpace;
  private final EventDeclProxy mDecl;
  private final List<CompiledRange> mRanges;
  private final Map<HashableIndexList,CompiledEvent> mIndexValueMap;

}
