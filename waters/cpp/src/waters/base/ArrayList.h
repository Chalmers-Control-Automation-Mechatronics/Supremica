//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   ArrayList
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _ArrayList_h_
#define _ArrayList_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#ifdef DEBUG
#include <iostream>
#endif /* DEBUG */

#include "waters/base/HashAccessor.h"


namespace waters {


//############################################################################
//# template ArrayList <typed>
//############################################################################

template <class Value>
class ArrayList
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ArrayList(uint32_t size = MIN_SIZE) :
    mNumElements(0),
    mArraySize(size),
    mArray(size == 0 ? 0 : new Value[size])
  {
  }

  explicit ArrayList(Value* values, uint32_t count, bool copy = true) :
    mNumElements(count),
    mArraySize(count)
  {
    if (count == 0) {
      mArray = 0;
    } else if (copy) {
      mArray = new Value[count];
      memcpy(mArray, values, count * sizeof(Value));
    } else {
      mArray = values;
    }
  }

  ArrayList(const ArrayList<Value>& list) :
    mNumElements(list.mNumElements),
    mArraySize(mNumElements),
    mArray(mNumElements == 0 ? 0 : new Value[mNumElements])
  {
    memcpy(mArray, list.mArray, mNumElements * sizeof(Value));
  }

  virtual ~ArrayList()
  {
    delete [] mArray;
  }

  //##########################################################################
  //# Access
  void clear(uint32_t size = 0)
  {
    mNumElements = 0;
    grow(size);
  }

  bool isEmpty()
    const
  {
    return size() == 0;
  }

  uint32_t size()
    const
  {
    return mNumElements;
  }

  Value get(uint32_t index)
    const
  {
    return mArray[index];
  }

  Value& getref(uint32_t index)
  {
    return mArray[index];
  }

  const Value& getref(uint32_t index) const
  {
    return mArray[index];
  }

  void set(uint32_t index, Value value)
  {
    mArray[index] = value;
  }

  uint32_t prepare()
  {
    grow(mNumElements + 1);
    return mNumElements;
  }

  uint32_t add()
  {
    grow(mNumElements + 1);
    return mNumElements++;
  }

  void add(const Value value)
  {
    grow(mNumElements + 1);
    mArray[mNumElements++] = value;
  }

  Value removeLast()
  {
    return mArray[--mNumElements];
  }

  void sort(int (*comparator)(const void*,const void*))
  {
    qsort(mArray, mNumElements, sizeof(Value), comparator);
  }

#ifdef DEBUG
  //##########################################################################
  //# Debugging
  void dump()
    const
  {
    char ch = '[';
    for (uint32_t i = 0; i < mNumElements; i++) {
      std::cerr << ch << mArray[i];
      ch = ',';
    }
    std::cerr << ']' << std::endl;
  }
#endif /* DEBUG */

protected:
  //##########################################################################
  //# Hash Functions
  bool equals(const ArrayList<Value>& list) const
  {
    if (mNumElements != list.mNumElements) {
      return false;
    } else {
      for (uint32_t i = 0; i < mNumElements; i++) {
	if (mArray[i] != list.mArray[i]) {
	  return false;
	}
      }
      return true;
    }
  }

  uint64_t hash() const
  {
    switch (sizeof(Value)) {
    case 8:
      return hashInt64Array((const uint64_t*) mArray, mArraySize);
    case 4:
      return hashInt32Array((const uint32_t*) mArray, mArraySize);
    default:
      return 0;
    }
  }

  void initHashFactors()
  {
    switch (sizeof(Value)) {
    case 8:
      initHashFactors64(mArraySize);
      break;
    case 4:
      initHashFactors32(mArraySize);
      break;
    default:
      break;
    }
  }    

  //##########################################################################
  //# Auxiliary Methods
  virtual bool grow(uint32_t newSize)
  {
    if (newSize > mArraySize) {
      if (newSize < MIN_SIZE) {
	newSize = MIN_SIZE;
      }
      if (newSize < (mArraySize << 1)) {
	mArraySize <<= 1;
      } else {
	mArraySize = newSize;
      }
      Value* newArray = new Value[mArraySize];
      memcpy(newArray, mArray, mNumElements * sizeof(Value));
      delete [] mArray;
      mArray = newArray;
      return true;
    } else {
      return false;
    }
  }

  //##########################################################################
  //# Class Constants
  static const uint32_t MIN_SIZE = 16;

private:
  //##########################################################################
  //# Data Members
  uint32_t mNumElements;
  uint32_t mArraySize;
  Value* mArray;
};


//############################################################################
//# template class IntArrayList <typed>
//############################################################################

template <class Value>
class IntArrayList : public ArrayList<Value>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit IntArrayList(uint32_t size = ArrayList<Value>::MIN_SIZE) :
    ArrayList<Value>(size) {ArrayList<Value>::initHashFactors();}
  explicit IntArrayList(Value* values, uint32_t count) : 
    ArrayList<Value>(values, count) {ArrayList<Value>::initHashFactors();}
  IntArrayList(const IntArrayList<Value>& list) :
    ArrayList<Value>(list) {ArrayList<Value>::initHashFactors();}

  //##########################################################################
  //# Hash Functions
  bool equals(const IntArrayList<Value>& list) const
    {return ArrayList<Value>::equals(list);}
  uint64_t hash() const {return ArrayList<Value>::hash();}

protected:
  //##########################################################################
  //# Auxiliary Methods
  virtual bool grow(uint32_t newSize)
  {
    if (ArrayList<Value>::grow(newSize)) {
      ArrayList<Value>::initHashFactors();
      return true;
    } else {
      return false;
    }
  }
};


}   /* namespace waters */

#endif  /* !_ArrayList_h_ */
