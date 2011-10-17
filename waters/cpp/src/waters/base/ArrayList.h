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

  ~ArrayList()
  {
    delete [] mArray;
  }

  //##########################################################################
  //# Access
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

  void add(const Value value)
  {
    grow(mNumElements + 1);
    mArray[mNumElements++] = value;
  }

  void clear()
  {
    mNumElements = 0;
  }

private:
  //##########################################################################
  //# Auxiliary Methods
  void grow(uint32_t newsize)
  {
    if (newsize > mArraySize) {
      mArraySize <<= 1;
      Value* newarray = new Value[mArraySize];
      for (uint32_t i = 0; i < mNumElements; i++) {
	newarray[i] = mArray[i];
      }
      delete [] mArray;
      mArray = newarray;
    }      
  }

  //##########################################################################
  //# Data Members
  uint32_t mNumElements;
  uint32_t mArraySize;
  Value* mArray;
};


}   /* namespace waters */

#endif  /* !_ArrayList_h_ */
