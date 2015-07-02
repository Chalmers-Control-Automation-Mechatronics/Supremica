//# This may look like C code, but it really is -*- C++ -*-
//############################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   BlockedArrayList
//############################################################################
//# $Id$
//############################################################################


#ifndef _BlockedArrayList_h_
#define _BlockedArrayList_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "waters/base/HashAccessor.h"


namespace waters {


//############################################################################
//# template BlockedArrayList <typed>
//############################################################################

template <class Value>
class BlockedArrayList
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BlockedArrayList(uint32_t blocksize = 256,
			    uint32_t headsize = 16)
  {
    mBlockShift = log2(blocksize - 1);
    mBlockSize = 1 << mBlockShift;
    mBlockMask = mBlockSize - 1;
    mHeadArraySize = headsize;
    mBlocks = new Value*[headsize];
    mNumElements = 0;
  }

  ~BlockedArrayList()
  {
    deleteBlocks();
    delete [] mBlocks;
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
    uint32_t blockno = index >> mBlockShift;
    uint32_t bindex = index & mBlockMask;
    return mBlocks[blockno][bindex];
  }

  Value& getref(uint32_t index)
    const
  {
    uint32_t blockno = index >> mBlockShift;
    uint32_t bindex = index & mBlockMask;
    return mBlocks[blockno][bindex];
  }

  void add()
  {
    uint32_t bindex = mNumElements & mBlockMask;
    if (bindex == 0) {
      uint32_t blockno = mNumElements >> mBlockShift;
      if (blockno >= mHeadArraySize) {
	growHeadArray();
      }
      mBlocks[blockno] = new Value[mBlockSize];
    }
    mNumElements++;
  }

  void add(const Value value)
  {
    uint32_t blockno = mNumElements >> mBlockShift;
    uint32_t bindex = mNumElements & mBlockMask;
    if (bindex == 0) {
      if (blockno >= mHeadArraySize) {
	growHeadArray();
      }
      mBlocks[blockno] = new Value[mBlockSize];
    }
    mBlocks[blockno][bindex] = value;
    mNumElements++;
  }

  void removeLast(int count)
  {
    mNumElements -= count;
  }

  void clear()
  {
    deleteBlocks();
    mNumElements = 0;
  }

private:
  //##########################################################################
  //# Auxiliary Methods
  void growHeadArray()
  {
    uint32_t newsize = mHeadArraySize << 1;
    Value** newblocks = new Value*[newsize];
    for (uint32_t i = 0; i < newsize; i++) {
      newblocks[i] = mBlocks[i];
    }
    delete [] mBlocks;
    mBlocks = newblocks;
    mHeadArraySize = newsize;
  }

  void deleteBlocks()
  {
    if (mNumElements > 0) {
      for (uint32_t bindex = (mNumElements - 1) >> mBlockShift;
	   bindex >=0; bindex--) {
	delete [] mBlocks[bindex];
      }
    }
  }

  //##########################################################################
  //# Data Members
  uint32_t mBlockSize;
  uint32_t mBlockMask;
  uint32_t mBlockShift;
  uint32_t mHeadArraySize;
  Value** mBlocks;
  uint32_t mNumElements;

};


}   /* namespace waters */

#endif  /* !_BlockedArrayList_h_ */
