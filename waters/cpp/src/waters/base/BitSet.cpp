//# -*- indent-tabs-mode: nil -*-
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

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include <jni.h>

#include "waters/base/BitSet.h"
#include "waters/base/HashAccessor.h"
#include "waters/javah/Invocations.h"


namespace waters {


//############################################################################
//# BitSet: Bit Size Constants
//############################################################################

#define INDEX_MASK (__WORDSIZE - 1)

#if __WORDSIZE == 64
#  define ONE 1L
#  define INDEX_SHIFT 6
#  define initHashFactors initHashFactors64
#  define hashIntArray hashInt64Array
#else
#  define ONE 1
#  define INDEX_SHIFT 5
#  define initHashFactors initHashFactors32
#  define hashIntArray hashInt32Array
#endif


//############################################################################
//# BitSet: Constructors & Destructors
//############################################################################

BitSet::
BitSet(uint32_t size, bool initValue) :
  mArraySize((size + INDEX_MASK) >> INDEX_SHIFT),
  mArray(new entry_t[mArraySize])
{
  initHashFactors(mArraySize);
  if (size > 0) {
    if (initValue) {
      uint32_t extraBits = size & INDEX_MASK;
      if (extraBits > 0) {
        memset(mArray, ~0, (mArraySize - 1) * sizeof(entry_t));
        mArray[mArraySize - 1] = (ONE << extraBits) - 1;
      } else {
        memset(mArray, ~0, mArraySize * sizeof(entry_t));
      }
    } else {
      clear();
    }
  }
}

BitSet::
~BitSet()
{
  delete [] mArray;
}


//############################################################################
//# BitSet: Access
//############################################################################

void BitSet::
clear()
{
  memset(mArray, 0, mArraySize * sizeof(entry_t));
}

void BitSet::
clear(uint32_t newSize)
{
  uint32_t newArraySize = (newSize + INDEX_MASK) >> INDEX_SHIFT;
  if (newArraySize != mArraySize) {
    delete [] mArray;
    mArray = new entry_t[newArraySize];
    mArraySize = newArraySize;
    initHashFactors(mArraySize);
  }
  clear();
}

bool BitSet::
get(uint32_t index)
  const
{
  uint32_t wordIndex = index >> INDEX_SHIFT;
  BitSet* nonConst = (BitSet*) this;
  nonConst->grow(wordIndex + 1);
  entry_t word = mArray[wordIndex];
  entry_t bit = ONE << (index & INDEX_MASK);
  return (word & bit) != 0;
}

void BitSet::
setBit(uint32_t index)
{
  uint32_t wordIndex = index >> INDEX_SHIFT;
  grow(wordIndex + 1);
  mArray[wordIndex] |= ONE << (index & INDEX_MASK);
}

void BitSet::
clearBit(uint32_t index)
{
  uint32_t wordIndex = index >> INDEX_SHIFT;
  grow(wordIndex + 1);
  mArray[wordIndex] &= ~(ONE << (index & INDEX_MASK));
}

bool BitSet::
equals(const BitSet& bitSet)
  const
{
  uint32_t size1 = mArraySize;
  uint32_t size2 = bitSet.mArraySize;
  uint32_t end = size1 <= size2 ? size1 : size2;
  for (uint32_t index = 0; index < end; index++) {
    if (mArray[index] != bitSet.mArray[index]) {
      return false;
    }
  }
  if (size1 > size2) {
    for (uint32_t index = end; index < size1; index++) {
      if (mArray[index] != 0) {
        return false;
      }
    }
  } else if (size1 < size2) {
    for (uint32_t index = end; index < size2; index++) {
      if (bitSet.mArray[index] != 0) {
        return false;
      }
    }
  }
  return true;
}

uint64_t BitSet::
hash()
  const
{
  return hashIntArray(mArray, mArraySize);
}


//############################################################################
//# Auxiliary Methods
//############################################################################

void BitSet::
grow(uint32_t newArraySize)
{
  if (newArraySize > mArraySize) {
    if (newArraySize < (mArraySize << 1)) {
      newArraySize = mArraySize << 1;
    }
    entry_t* newArray = new entry_t[newArraySize];
    memcpy(newArray, mArray, mArraySize * sizeof(entry_t));
    memset(&newArray[mArraySize], 0,
           (newArraySize - mArraySize) * sizeof(entry_t)); 
    delete [] mArray;
    mArray = newArray;
    mArraySize = newArraySize;
  }      
}


}   /* namespace waters */


//############################################################################
//# BitSet: Invocation through JNI

JNIEXPORT jlong JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_createNativeBitSet__I
  (JNIEnv* env, jobject /* bitset */, jint initialSize)
{
  return (jlong) new waters::BitSet(initialSize);
}

JNIEXPORT jlong JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_createNativeBitSet__IZ
  (JNIEnv* env, jobject /* bitset */, jint initialSize, jboolean initialValue)
{
  return (jlong) new waters::BitSet(initialSize, initialValue);
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_destroyNativeBitSet
  (JNIEnv* env, jobject /* bitset */, jlong handler)
{
  waters::BitSet *bitSet = (waters::BitSet*) handler;
  delete bitSet;
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_clearNative__J
  (JNIEnv* env, jobject /* bitset */, jlong handler)
{
  waters::BitSet *bitSet = (waters::BitSet*) handler;
  bitSet->clear();
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_clearNative__JI
  (JNIEnv* env, jobject /* bitset */, jlong handler, jint newSize)
{
  waters::BitSet *bitSet = (waters::BitSet*) handler;
  bitSet->clear(newSize);
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_getNative
  (JNIEnv* env, jobject /* bitset */, jlong handler, jint index)
{
  const waters::BitSet *bitSet = (const waters::BitSet*) handler;
  return bitSet->get(index) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_setBitNative
  (JNIEnv* env, jobject /* bitset */, jlong handler, jint index)
{
  waters::BitSet *bitSet = (waters::BitSet*) handler;
  bitSet->setBit(index);
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_clearBitNative
  (JNIEnv* env, jobject /* bitset */, jlong handler, jint index)
{
  waters::BitSet *bitSet = (waters::BitSet*) handler;
  bitSet->clearBit(index);
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeBitSet_equalsNative
  (JNIEnv* env, jobject /* bitset */, jlong handler1, jlong handler2)
{
  const waters::BitSet *bitSet1 = (const waters::BitSet*) handler1;
  const waters::BitSet *bitSet2 = (const waters::BitSet*) handler2;
  return bitSet1->equals(*bitSet2) ? JNI_TRUE : JNI_FALSE;
}
