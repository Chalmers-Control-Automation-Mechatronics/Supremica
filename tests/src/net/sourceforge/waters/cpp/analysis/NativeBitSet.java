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
