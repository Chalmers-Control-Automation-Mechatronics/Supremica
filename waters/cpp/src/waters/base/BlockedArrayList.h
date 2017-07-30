//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

#ifndef _BlockedArrayList_h_
#define _BlockedArrayList_h_

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
//# template BlockedArrayList <typed>
//############################################################################

template <class Value>
class BlockedArrayList
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BlockedArrayList(uint32_t blockSize = 256,
			    uint32_t headSize = 16)
  {
    mBlockShift = log2(blockSize - 1);
    mBlockSize = 1 << mBlockShift;
    mBlockMask = mBlockSize - 1;
    mHeadArraySize = headSize;
    mBlocks = new Value*[headSize];
    memset(mBlocks, 0, headSize * sizeof(Value*));
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

  uint32_t add()
  {
    uint32_t bindex = mNumElements & mBlockMask;
    if (bindex == 0) {
      uint32_t blockno = mNumElements >> mBlockShift;
      if (blockno >= mHeadArraySize) {
	growHeadArray();
      }
      if (mBlocks[blockno] == 0) {
	mBlocks[blockno] = new Value[mBlockSize];
      }
    }
    return mNumElements++;
  }

  void add(const Value value)
  {
    uint32_t blockno = mNumElements >> mBlockShift;
    uint32_t bindex = mNumElements & mBlockMask;
    if (bindex == 0) {
      if (blockno >= mHeadArraySize) {
	growHeadArray();
      }
      if (mBlocks[blockno] == 0) {
	mBlocks[blockno] = new Value[mBlockSize];
      }
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
    uint32_t newSize = mHeadArraySize << 1;
    Value** newBlocks = new Value*[newSize];
    memcpy(newBlocks, mBlocks, mHeadArraySize * sizeof(Value*));
    memset(&newBlocks[mHeadArraySize], 0, mHeadArraySize * sizeof(Value*));
    delete [] mBlocks;
    mBlocks = newBlocks;
    mHeadArraySize = newSize;
  }

  void deleteBlocks()
  {
    if (mNumElements > 0) {
      uint32_t bindex = (mNumElements - 1) >> mBlockShift;
      delete [] mBlocks[bindex];
      mBlocks[bindex] = 0;
      while (bindex > 0) {
	bindex--;
	delete [] mBlocks[bindex];
	mBlocks[bindex] = 0;
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
