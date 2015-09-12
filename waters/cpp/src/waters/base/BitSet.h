//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   BitSet
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _BitSet_h_
#define _BitSet_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>
#include <string.h>

#include "waters/base/WordSize.h"


namespace waters {


//############################################################################
//# Bit Size Constants
//############################################################################

#if __WORDSIZE == 64
  typedef uint64_t entry_t;
#else
  typedef uint32_t entry_t;
#endif


//############################################################################
//# class BitSet
//############################################################################

class BitSet
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BitSet(uint32_t size = 16, bool initValue = false);
  ~BitSet();

  //##########################################################################
  //# Access
  void clear();
  void clear(uint32_t newSize);
  bool get(uint32_t index) const;
  inline void setBit(uint32_t index, bool value)
    {if (value) setBit(index); else clearBit(index);}
  void setBit(uint32_t index);
  void clearBit(uint32_t index);
  bool equals(const BitSet& bitSet) const;
  uint64_t hash() const;

private:
  //##########################################################################
  //# Auxiliary Methods
  void grow(uint32_t newArraySize);

  //##########################################################################
  //# Data Members
  size_t mArraySize;
  entry_t* mArray;
};


}   /* namespace waters */

#endif  /* !_BitSet_h_ */
