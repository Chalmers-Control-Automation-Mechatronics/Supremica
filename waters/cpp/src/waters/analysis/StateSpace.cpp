//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
StateSpace(const AutomatonEncoding* encoding,
           uint32_t limit,
           int extraWords)
  : mExtendedTupleSize(encoding->getEncodingSize() + extraWords),
    mSignificantTupleSize(encoding->getEncodingSize()),
    mNumStates(0),
    mStateLimit(limit),
    mBlocks(INITBLOCKS),
    mLookupTable(this, BLOCKSIZE)
{
  initHashFactors32(mSignificantTupleSize);
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
  return &block[mExtendedTupleSize * (index & BLOCKMASK)];
}

uint32_t* StateSpace::
prepare()
{
  uint32_t* block;
  uint32_t blockno = mNumStates >> BLOCKSHIFT;
  if (blockno >= mBlocks.size()) {
    block = new uint32_t[mExtendedTupleSize * BLOCKSIZE];
    mBlocks.add(block);
  } else {
    block = mBlocks.get(blockno);
  }
  return &block[mExtendedTupleSize * (mNumStates & BLOCKMASK)];
}

uint32_t* StateSpace::
prepare(uint32_t index)
{
  uint32_t* source = get(index);
  uint32_t* target = prepare();
  for (int i = 0; i < mSignificantTupleSize; i++) {
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
  return hashInt32Array(tuple, mSignificantTupleSize);
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
  for (int i = 0; i < mSignificantTupleSize; i++) {
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
  return hashInt32Array(tuple, getSignificantTupleSize(), mMask0);
}

bool TaggedStateSpace::
equalTuples(const uint32_t* tuple1, const uint32_t* tuple2)
  const
{
  const int esize = getSignificantTupleSize();
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
