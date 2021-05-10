//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.cpp.analysis;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;


/**
 * A Java encapsulation of the C++ hash table for the purpose of unit
 * testing with JUnit. This tests the C++ class <CODE>PtrHashTable</CODE>
 * declared in <CODE>waters/base/HashTable.h</CODE>
 * through its encapsulation in <CODE>waters/base/JavaHashTable.h</CODE>.
 *
 * @author Robi Malik
 */

public class NativeHashSet<E> extends AbstractSet<E>
{

  //#########################################################################
  //# Constructors
  public NativeHashSet(final int initialsize)
  {
    mNativeHandler = createNativeHashSet(initialsize);
  }

  public NativeHashSet()
  {
    this(0);
  }

  public NativeHashSet(final Collection<? extends E> collection)
  {
    this(collection.size());
    addAll(collection);
  }

  @Override
  protected void finalize()
  {
    destroyNativeHashSet(mNativeHandler);
  }


  //#########################################################################
  //# Interface java.util.Collection
  @Override
  public int size()
  {
    return getNativeSize(mNativeHandler);
  }

  @Override
  public Iterator<E> iterator()
  {
    return new NativeHashSetIterator();
  }

  @Override
  public boolean add(final E item)
  {
    return addNative(mNativeHandler, item);
  }

  @Override
  public boolean contains(final Object item)
  {
    return containsNative(mNativeHandler, item);
  }

  @Override
  public void clear()
  {
    clearNative(mNativeHandler);
  }


  //#########################################################################
  //# Native Methods
  private native long createNativeHashSet(int size);

  private native void destroyNativeHashSet(long handler);

  private native int getNativeSize(long handler);

  private native boolean containsNative(long handler, Object item);

  private native boolean addNative(long handler, Object item);

  private native void clearNative(long handler);

  private native long createNativeIterator(long handler);

  private native void destroyNativeIterator(long iter);

  private native E getNativeNext(long handler, long iter);

  private native boolean hasNativeNext(long handler, long iter);

  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Inner Class NativeHashSetIterator
  private class NativeHashSetIterator implements Iterator<E>
  {

    //#######################################################################
    //# Constructor
    private NativeHashSetIterator()
    {
      mNativeIter = createNativeIterator(mNativeHandler);
    }

    @Override
    protected void finalize()
    {
      destroyNativeIterator(mNativeIter);
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      return hasNativeNext(mNativeHandler, mNativeIter);
    }

    @Override
    public E next()
    {
      return getNativeNext(mNativeHandler, mNativeIter);
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("NativeHashSet iterator does not support removal of elements!");
    }

    //#######################################################################
    //# Data Members
    private final long mNativeIter;
  }


  //#########################################################################
  //# Data Members
  private final long mNativeHandler;

}
