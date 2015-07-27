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
#include <string.h>

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
  explicit ArrayList(uint32_t size = 16) :
    mNumElements(0),
    mArraySize(size),
    mArray(new Value[size])
  {
  }

  explicit ArrayList(Value* values, uint32_t count, bool copy = true) :
    mNumElements(count),
    mArraySize(count)
  {
    if (copy) {
      mArray = new Value[count];
      memcpy(mArray, values, count * sizeof(Value));
    } else {
      mArray = values;
    }
  }

  ArrayList(const ArrayList<Value>& list) :
    mNumElements(list.mNumElements),
    mArraySize(list.mNumElements),
    mArray(new Value[list.mNumElements])
  {
    memcpy(mArray, list.mArray, mNumElements * sizeof(Value));
  }

  virtual ~ArrayList()
  {
    delete [] mArray;
  }

  //##########################################################################
  //# Access
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

  void set(uint32_t index, Value value)
  {
    mArray[index] = value;
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

  void clear()
  {
    mNumElements = 0;
  }

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
      mArraySize <<= 1;
      Value* newArray = new Value[mArraySize];
      memcpy(newArray, mArray, mNumElements * sizeof(Value));
      delete [] mArray;
      mArray = newArray;
      return true;
    } else {
      return false;
    }
  }

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
  explicit IntArrayList(uint32_t size = 16) :
    ArrayList<Value>(size) {ArrayList<Value>::initHashFactors();}
  explicit IntArrayList(Value* values, uint32_t count) : 
    ArrayList<Value>(values, count) {ArrayList<Value>::initHashFactors();}

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
