//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledEventDecl
//###########################################################################
//# $Id: CompiledEventDecl.java,v 1.7 2008-04-21 22:54:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A compiler-internal representation of an event declaration.</P>
 *
 * <P>This class supports the mapping from a module's event declarations
 * and event expressions to the simple events in a product DES.</P>
 *
 * @author Robi Malik
 */

class CompiledEventDecl
{

  //#########################################################################
  //# Constructors
  CompiledEventDecl(final String prefixedName,
                    final EventDeclProxy decl,
                    final List<RangeValue> ranges)
  {
    mPrefixedName = prefixedName;
    mDecl = decl;
    mRanges = ranges;
    int size = 1;
    for (final RangeValue range : ranges) {
      size *= range.size();
    }
    mIndexValueMap =
      new HashMap<List<IndexValue>,EventValue>(size);
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mDecl.getName();
  }

  String getPrefixedName()
  {
    return mPrefixedName;
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

  List<RangeValue> getRanges()
  {
    return mRanges;
  }

  RangeValue getRange(final int index)
  {
    return mRanges.get(index);
  }

  ColorGeometryProxy getColorGeometry()
  {
    return mDecl.getColorGeometry();
  }


  //#########################################################################
  //# Auxiliary Methods
  EventValue getValue()
  {
    final List<IndexValue> empty = Collections.emptyList();
    return getValue(empty);
  }

  EventValue getValue(List<? extends IndexValue> indexes)
  {
    EventValue result = mIndexValueMap.get(indexes);
    if (result == null) {
      final List<IndexValue> indexcopy;
      if (indexes.isEmpty()) {
        indexcopy = Collections.emptyList();
      } else {
        indexcopy = new ArrayList<IndexValue>(indexes);
      }
      if (indexes.size() < mRanges.size()) {
	result = new CompiledArrayEventValue(this, indexcopy);
      } else {
	result = new CompiledSingleEventValue(this, indexcopy);
      }
      mIndexValueMap.put(indexcopy, result);
    }
    return result;
  }

  String getIndexedName(final List<? extends IndexValue> indexes)
  {
    final StringBuffer buffer = new StringBuffer(mPrefixedName);
    for (final IndexValue index : indexes) {
      buffer.append('[');
      buffer.append(index);
      buffer.append(']');
    }
    return buffer.toString();
  }

  void checkIndex(final int pos, final IndexValue value)
    throws IndexOutOfRangeException
  {
    final RangeValue range = mRanges.get(pos);
    if (!range.contains(value)) {
      throw new IndexOutOfRangeException(value, range);
    }
  }


  //#########################################################################
  //# Data Members
  private final String mPrefixedName;
  private final EventDeclProxy mDecl;
  private final List<RangeValue> mRanges;
  private final Map<List<IndexValue>,EventValue>
    mIndexValueMap;

}
