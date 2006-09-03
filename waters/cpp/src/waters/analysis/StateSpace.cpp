//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   StateSpace
//###########################################################################
//# $Id: StateSpace.cpp,v 1.1 2006-09-03 06:38:42 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/StateSpace.h"


namespace waters {

//############################################################################
//# class StateSpace
//############################################################################

//############################################################################
//# StateSpace: Constructors & Destructors

StateSpace::
StateSpace(const AutomatonEncoding* encoding)
  : mEncodingSize(encoding->getNumWords()),
    mNumStates(0),
    mBlocks(INITBLOCKS),
    mLookupTable(this, BLOCKSIZE)
{
}

StateSpace::
~StateSpace()
{
  for (uint32 i = 0; i < mBlocks.size(); i++) {
    uint32* block = mBlocks.get(i);
    delete [] block;
  }
}


//############################################################################
//# StateSpace: Access

uint32* StateSpace::
get(const uint32 index)
  const
{
  uint32* block = mBlocks.get(index >> BLOCKSHIFT);
  return &block[mEncodingSize * (index & BLOCKMASK)];
}

uint32* StateSpace::
prepare()
{
  uint32* block;
  uint32 blockno = mNumStates >> BLOCKSHIFT;
  if (blockno >= mBlocks.size()) {
    block = new uint32[mEncodingSize * BLOCKSIZE];
    mBlocks.add(block);
  } else {
    block = mBlocks.get(blockno);
  }
  return &block[mEncodingSize * (mNumStates & BLOCKMASK)];
}

uint32* StateSpace::
prepare(const uint32 index)
{
  uint32* source = get(index);
  uint32* target = prepare();
  for (int i = 0; i < mEncodingSize; i++) {
    target[i] = source[i];
  }
  return target;
}

uint32 StateSpace::
add()
{
  uint32 added = mLookupTable.add(mNumStates);
  if (added == mNumStates) {
    mNumStates++;
  }
  return added;
}

uint32 StateSpace::
find()
  const
{
  return mLookupTable.get(mNumStates);
}

void StateSpace::
clear()
{
  for (uint32 i = 0; i < mBlocks.size(); i++) {
    uint32* block = mBlocks.get(i);
    delete [] block;
  }
  mBlocks.clear();
  mLookupTable.clear();
}


//############################################################################
//# StateSpace: Hash Methods

uint32 StateSpace::
hash(const void* key)
  const
{
  const uint32 index = (uint32) key;
  const uint32* tuple = get(index);
  return hashIntArray(tuple, mEncodingSize);
}

bool StateSpace::
equals(const void* key1, const void* key2)
  const
{
  const uint32 index1 = (uint32) key1;
  const uint32* tuple1 = get(index1);
  const uint32 index2 = (uint32) key2;
  const uint32* tuple2 = get(index2);
  for (int i = 0; i < mEncodingSize; i++) {
    if (tuple1[i] != tuple2[i]) {
      return false;
    }
  }
  return true;
}


}  /* namespace waters */
