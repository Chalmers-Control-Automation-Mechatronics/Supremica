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

#include "waters/base/IntTypes.h"


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
  explicit BlockedArrayList(uint32 blocksize = 256,
			    uint32 headsize = 16)
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
  uint32 size()
    const
  {
    return mNumElements;
  }

  Value get(uint32 index)
    const
  {
    uint32 blockno = index >> mBlockShift;
    uint32 bindex = index & mBlockMask;
    return mBlocks[blockno][bindex];
  }

  Value& getref(uint32 index)
    const
  {
    uint32 blockno = index >> mBlockShift;
    uint32 bindex = index & mBlockMask;
    return mBlocks[blockno][bindex];
  }

  void add()
  {
    uint32 bindex = mNumElements & mBlockMask;
    if (bindex == 0) {
      uint32 blockno = mNumElements >> mBlockShift;
      if (blockno >= mHeadArraySize) {
	growHeadArray();
      }
      mBlocks[blockno] = new Value[mBlockSize];
    }
    mNumElements++;
  }

  void add(const Value value)
  {
    uint32 blockno = mNumElements >> mBlockShift;
    uint32 bindex = mNumElements & mBlockMask;
    if (bindex == 0) {
      if (blockno >= mHeadArraySize) {
	growHeadArray();
      }
      mBlocks[blockno] = new Value[mBlockSize];
    }
    mBlocks[blockno][bindex] = value;
    mNumElements++;
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
    uint32 newsize = mHeadArraySize << 1;
    Value** newblocks = new Value*[newsize];
    for (uint32 i = 0; i < newsize; i++) {
      newblocks[i] = mBlocks[i];
    }
    delete [] mBlocks;
    mBlocks = newblocks;
    mHeadArraySize = newsize;
  }

  void deleteBlocks()
  {
    if (mNumElements > 0) {
      for (uint32 bindex = (mNumElements - 1) >> mBlockShift;
	   bindex >=0; bindex--) {
	delete [] mBlocks[bindex];
      }
    }
  }

  //##########################################################################
  //# Data Members
  uint32 mBlockSize;
  uint32 mBlockMask;
  uint32 mBlockShift;
  uint32 mHeadArraySize;
  Value** mBlocks;
  uint32 mNumElements;

};


}   /* namespace waters */

#endif  /* !_BlockedArrayList_h_ */
