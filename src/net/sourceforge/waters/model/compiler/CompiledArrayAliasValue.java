//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledArrayAliasValue
//###########################################################################
//# $Id: CompiledArrayAliasValue.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.RangeValue;


class CompiledArrayAliasValue implements ArrayValue, EventValue
{

  //#########################################################################
  //# Constructor
  CompiledArrayAliasValue(final String name)
  {
    mParentInfo = new RootParentInfo(name);
    mMap = new HashMap<IndexValue,EventValue>();
  }

  CompiledArrayAliasValue(final CompiledArrayAliasValue parent,
                          final IndexValue index)
  {
    mParentInfo = new IndexedParentInfo(parent, index);
    mMap = new HashMap<IndexValue,EventValue>();
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return getName();
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.ArrayValue
  public EventValue find(final IndexValue index)
    throws UndefinedIdentifierException
  {
    final EventValue result = get(index);
    if (result == null) {
      final ParentInfo info = new IndexedParentInfo(this, index);
      final String name = info.getName();
      throw new UndefinedIdentifierException(name);
    }
    return result;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public int getKindMask()
  {
    int mask = 0;
    for (final EventValue event : mMap.values()) {
      mask |= event.getKindMask();
    }
    return mask;
  }

  public boolean isObservable()
  {
    boolean observable = true;
    for (final EventValue event : mMap.values()) {
      observable &= event.isObservable();
    }
    return observable;
  }

  public Iterator<CompiledSingleEventValue> getEventIterator()
  {
    return new ArrayAliasIterator();
  }

  public List<RangeValue> getIndexRanges()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Specific Access
  String getName()
  {
    return mParentInfo.getName();
  }

  EventValue get(final IndexValue index)
  {
    return mMap.get(index);
  }

  void set(final IndexValue index, final EventValue value)
    throws DuplicateIdentifierException
  {
    if (!mMap.containsKey(index)) {
      mMap.put(index, value);
    } else {
      final ParentInfo info = new IndexedParentInfo(this, index);
      final String name = info.getName();
      throw new DuplicateIdentifierException(name);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ParentInfo getParentInfo()
  {
    return mParentInfo;
  }


  //#########################################################################
  //# Local Class ParentInfo
  private abstract class ParentInfo
  {

    //#######################################################################
    //# Naming
    private String getName()
    {
      final StringBuffer buffer = new StringBuffer();
      appendName(buffer);
      return buffer.toString();
    }

    abstract void appendName(StringBuffer buffer);

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
    void appendName(final StringBuffer buffer)
    {
      buffer.append(mName);
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
    private IndexedParentInfo(final CompiledArrayAliasValue parent,
                              final IndexValue index)
    {
      mParent = parent;
      mIndex = index;
    }

    //#######################################################################
    //# Naming
    void appendName(final StringBuffer buffer)
    {
      final ParentInfo parentInfo = mParent.getParentInfo();
      parentInfo.appendName(buffer);
      buffer.append('[');
      buffer.append(mIndex.toString());
      buffer.append(']');
    }

    //#######################################################################
    //# Data Members
    private final CompiledArrayAliasValue mParent;
    private final IndexValue mIndex;

  }


  //#########################################################################
  //# Local Class ArrayAliasIterator
  private class ArrayAliasIterator
    implements Iterator<CompiledSingleEventValue>
  {

    //#######################################################################
    //# Constructor
    ArrayAliasIterator()
    {
      mKeyIterator = mMap.keySet().iterator();
      advance();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mInnerIterator != null;
    }

    public CompiledSingleEventValue next()
    {
      if (mInnerIterator != null) {
        final CompiledSingleEventValue result = mInnerIterator.next();
        if (!mInnerIterator.hasNext()) {
          advance();
        }
        return result;
      } else {
        throw new NoSuchElementException
          ("Out of elements in ArrayAliasIterator!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("ArrayAliasIterator does not support remove() operation!");
    } 

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      if (mKeyIterator.hasNext()) {
        final IndexValue index = mKeyIterator.next();
        final EventValue value = mMap.get(index);
        mInnerIterator = value.getEventIterator();
      } else {
        mKeyIterator = null;
        mInnerIterator = null;
      }
    }

    //#######################################################################
    //# Data Members
    private Iterator<IndexValue> mKeyIterator;
    private Iterator<CompiledSingleEventValue> mInnerIterator;

  }


  //#########################################################################
  //# Data Members
  private final ParentInfo mParentInfo;
  private final Map<IndexValue,EventValue> mMap;

}
