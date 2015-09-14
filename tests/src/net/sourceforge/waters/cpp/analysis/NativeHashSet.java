//###########################################################################
//# PROJECT: Waters C++
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeHashSet
//###########################################################################
//# $Id: 69bdf2adf09044c6d8f608f557546d9ffeff1563 $
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
