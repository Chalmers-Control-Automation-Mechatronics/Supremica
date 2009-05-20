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

#include "waters/base/IntTypes.h"


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
  explicit ArrayList(uint32 size = 16) :
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
  uint32 size()
    const
  {
    return mNumElements;
  }

  Value get(uint32 index)
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
  void grow(uint32 newsize)
  {
    if (newsize > mArraySize) {
      mArraySize <<= 1;
      Value* newarray = new Value[mArraySize];
      for (uint32 i = 0; i < mNumElements; i++) {
	newarray[i] = mArray[i];
      }
      delete [] mArray;
      mArray = newarray;
    }      
  }

  //##########################################################################
  //# Data Members
  uint32 mNumElements;
  uint32 mArraySize;
  Value* mArray;
};


}   /* namespace waters */

#endif  /* !_ArrayList_h_ */
