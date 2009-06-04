//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeHashSet
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;


/**
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

  protected void finalize()
  {
    destroyNativeHashSet(mNativeHandler);
  }


  //#########################################################################
  //# Interface java.util.Collection
  public int size()
  {
    return getNativeSize(mNativeHandler);
  }

  public Iterator<E> iterator()
  {
    return new NativeHashSetIterator();
  }

  public boolean add(final E item)
  {
    return addNative(mNativeHandler, item);
  }

  public boolean contains(final Object item)
  {
    return containsNative(mNativeHandler, item);
  }

  public void clear()
  {
    clearNative(mNativeHandler);
  }


  //#########################################################################
  //# Native Methods
  private native int createNativeHashSet(int size);

  private native void destroyNativeHashSet(int handler);

  private native int getNativeSize(int handler);

  private native boolean containsNative(int handler, Object item);

  private native boolean addNative(int handler, Object item);

  private native void clearNative(int handler);

  private native int createNativeIterator(int handler);

  private native void destroyNativeIterator(int iter);

  private native E getNativeNext(int handler, int iter);

  private native boolean hasNativeNext(int handler, int iter);

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

    protected void finalize()
    {
      destroyNativeIterator(mNativeIter);
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return hasNativeNext(mNativeHandler, mNativeIter);
    }

    public E next()
    {
      return getNativeNext(mNativeHandler, mNativeIter);
    }

    public void remove()
    {
      throw new UnsupportedOperationException
	("NativeHashSet iterator does not support removal of elements!");
    }

    //#######################################################################
    //# Data Members
    private final int mNativeIter;
  }


  //#########################################################################
  //# Data Members
  private final int mNativeHandler;

}
