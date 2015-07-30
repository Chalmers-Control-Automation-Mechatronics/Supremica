//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   BitSet
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "waters/base/BitSet.h"
#include "waters/base/HashAccessor.h"


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
      memset(mArray, ~0, (mArraySize - 1) * sizeof(entry_t));
      uint32_t extraBits = size & INDEX_MASK;
      if (extraBits > 0) {
        mArray[mArraySize - 1] = (ONE << extraBits) - 1;
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
    delete mArray;
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
  entry_t word = mArray[index >> INDEX_SHIFT];
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
