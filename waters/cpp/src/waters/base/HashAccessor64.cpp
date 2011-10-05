//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashAccessor64
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "waters/base/HashAccessor64.h"


namespace waters {


//############################################################################
//# Some Hash Functions
//############################################################################


//############################################################################
//# class UInt32ArrayHashAccessor
//############################################################################

//############################################################################
//# UInt32ArrayHashAccessor: Hash Methods

uint64_t UInt32ArrayHashAccessor::
hash(intptr_t key)
  const
{
  const uint32_t* array = (const uint32_t*) key;
  return hashIntArray(array, mSize);
}


bool UInt32ArrayHashAccessor::
equals(intptr_t key1, intptr_t key2)
  const
{
  const uint32_t* array1 = (const uint32_t*) key1;
  const uint32_t* array2 = (const uint32_t*) key2;
  for (uint32_t i = 0; i < mSize; i++) {
    if (array1[i] != array2[i]) {
      return false;
    }
  }
  return true;
}


}  /* namespace waters */
