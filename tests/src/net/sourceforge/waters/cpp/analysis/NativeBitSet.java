//###########################################################################
//# PROJECT: Waters C++
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeBitSet
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;



/**
 * A Java encapsulation of the C++ bit set for the purpose of unit
 * testing with JUnit. This tests the C++ class <CODE>BitSet</CODE>
 * declared in <CODE>waters/base/BitSet.h</CODE>.
 *
 * @author Robi Malik
 */

public class NativeBitSet
{

  //#########################################################################
  //# Constructors
  public NativeBitSet(final int initialSize)
  {
    mNativeHandler = createNativeBitSet(initialSize);
  }

  public NativeBitSet(final int initialSize, final boolean initialValue)
  {
    mNativeHandler = createNativeBitSet(initialSize, initialValue);
  }

  @Override
  protected void finalize()
  {
    destroyNativeBitSet(mNativeHandler);
  }


  //#########################################################################
  //# Access
  public void clear()
  {
    clearNative(mNativeHandler);
  }

  public void clear(final int newSize)
  {
    clearNative(mNativeHandler, newSize);
  }

  public boolean get(final int index)
  {
    return getNative(mNativeHandler, index);
  }

  public void setBit(final int index)
  {
    setBitNative(mNativeHandler, index);
  }

  public void clearBit(final int index)
  {
    clearBitNative(mNativeHandler, index);
  }

  @Override
  public boolean equals(final Object other)
  {
    if (other != null && other.getClass() == getClass()) {
      final NativeBitSet bitSet = (NativeBitSet) other;
      return equalsNative(mNativeHandler, bitSet.mNativeHandler);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Native Methods
  private native long createNativeBitSet(int size);

  private native long createNativeBitSet(int size, boolean initialValue);

  private native void destroyNativeBitSet(long handler);

  private native void clearNative(long handler);

  private native void clearNative(long handler, int newSize);

  private native boolean getNative(long handler, int index);

  private native void setBitNative(long handler, int index);

  private native void clearBitNative(long handler, int index);

  private native boolean equalsNative(long handler1, long handler2);

  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Data Members
  private final long mNativeHandler;

}
