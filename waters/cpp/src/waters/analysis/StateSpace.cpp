//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   StateSpace
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>

#include "jni/cache/PreOverflowException.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/StateSpace.h"


namespace waters {

//############################################################################
//# class StateSpace
//############################################################################

//############################################################################
//# StateSpace: Constructors & Destructors

StateSpace::
StateSpace(const AutomatonEncoding* encoding, uint32_t limit)
  : mEncodingSize(encoding->getEncodingSize()),
    mNumSignificantWords(encoding->getNumberOfSignificantWords()),
    mNumStates(0),
    mStateLimit(limit),
    mBlocks(INITBLOCKS),
    mLookupTable(this, BLOCKSIZE)
{
  initHashFactors(mNumSignificantWords);
}

StateSpace::
~StateSpace()
{
  for (uint32_t i = 0; i < mBlocks.size(); i++) {
    uint32_t* block = mBlocks.get(i);
    delete [] block;
  }
}


//############################################################################
//# StateSpace: Access

uint32_t* StateSpace::
get(uint32_t index)
  const
{
  uint32_t* block = mBlocks.get(index >> BLOCKSHIFT);
  return &block[mEncodingSize * (index & BLOCKMASK)];
}

uint32_t* StateSpace::
prepare()
{
  uint32_t* block;
  uint32_t blockno = mNumStates >> BLOCKSHIFT;
  if (blockno >= mBlocks.size()) {
    block = new uint32_t[mEncodingSize * BLOCKSIZE];
    mBlocks.add(block);
  } else {
    block = mBlocks.get(blockno);
  }
  return &block[mEncodingSize * (mNumStates & BLOCKMASK)];
}

uint32_t* StateSpace::
prepare(uint32_t index)
{
  uint32_t* source = get(index);
  uint32_t* target = prepare();
  for (int i = 0; i < mEncodingSize; i++) {
    target[i] = source[i];
  }
  return target;
}

uint32_t StateSpace::
add()
{
  uint32_t added = mLookupTable.add(mNumStates);
  if (added == mNumStates) {
    if (++mNumStates > mStateLimit) {
      throw jni::PreOverflowException(jni::OverflowKind_STATE, mStateLimit);
    }
  }
  return added;
}

void StateSpace::
clear()
{
  for (uint32_t i = 0; i < mBlocks.size(); i++) {
    uint32_t* block = mBlocks.get(i);
    delete [] block;
  }
  mBlocks.clear();
  mLookupTable.clear();
}


//############################################################################
//# StateSpace: Hash Methods

uint64_t StateSpace::
hash(int32_t key)
  const
{
  const uint32_t* tuple = get(key);
  return hashIntArray(tuple, mNumSignificantWords);
}

bool StateSpace::
equals(int32_t key1, int32_t key2)
  const
{
  const uint32_t* tuple1 = get(key1);
  const uint32_t* tuple2 = get(key2);
  return equalTuples(tuple1, tuple2);
}

bool StateSpace::
equalTuples(const uint32_t* tuple1, const uint32_t* tuple2)
  const
{
  for (int i = 0; i < mNumSignificantWords; i++) {
    if (tuple1[i] != tuple2[i]) {
      return false;
    }
  }
  return true;
}


//############################################################################
//# class TaggedStateSpace
//############################################################################

//############################################################################
//# TaggedStateSpace: Constructors & Destructors

TaggedStateSpace::
TaggedStateSpace(const AutomatonEncoding* encoding, uint32_t limit)
  : StateSpace(encoding, limit),
    mMask0(encoding->getInverseTagMask())
{
}


//############################################################################
//# TaggedStateSpace: Hash Methods

uint64_t TaggedStateSpace::
hash(int32_t key)
  const
{
  const uint32_t* tuple = get(key);
  return hashIntArray(tuple, getNumberOfSignificantWords(), mMask0);
}

bool TaggedStateSpace::
equalTuples(const uint32_t* tuple1, const uint32_t* tuple2)
  const
{
  const int esize = getNumberOfSignificantWords();
  if (esize > 0) {
    if (((tuple1[0] ^ tuple2[0]) & mMask0) != 0) {
      return false;
    }
    for (int i = 1; i < esize; i++) {
      if (tuple1[i] != tuple2[i]) {
        return false;
      }
    }
  }
  return true;
}


}  /* namespace waters */
