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

#include "jni/cache/PreOverflowException.h"

#include "waters/analysis/ReverseTransitionStore.h"


namespace waters {

//############################################################################
//# class ReverseTransitionStore
//############################################################################

//############################################################################
//# ReverseTransitionStore: Constructors & Destructors

ReverseTransitionStore::
ReverseTransitionStore(uint32_t limit)
  : mTransitionLimit(limit),
    mNumTransitions(0),
    mNextLocalIndex(BLOCK_SIZE),
    mNextGlobalIndex(0),
    mCurrentNodeBlock(0),
    mHeadBlocks(INIT_BLOCKS),
    mNodeBlocks(INIT_BLOCKS)
{
}

ReverseTransitionStore::
~ReverseTransitionStore()
{
  for (uint32_t i = 0; i < mHeadBlocks.size(); i++) {
    uint32_t* block = mHeadBlocks.get(i);
    delete [] block;
  }
  for (uint32_t j = 0; j < mNodeBlocks.size(); j++) {
    uint32_t* block = mNodeBlocks.get(j);
    delete [] block;
  }
}


//############################################################################
//# ReverseTransitionStore: Access

void ReverseTransitionStore::
addTransition(uint32_t source, uint32_t target)
{
  // suppress selfloops
  if (source == target) {
    return;
  }
  // find or allocate block
  const uint32_t headblockno = target >> BLOCK_SHIFT;
  const uint32_t headindex = target & BLOCK_MASK;
  uint32_t* headblock;
  uint32_t head;
  if (headblockno < mHeadBlocks.size()) {
    headblock = mHeadBlocks.get(headblockno);
    head = headblock[headindex];
  } else {
    do {
      headblock = new uint32_t[BLOCK_SIZE];
      for (uint32_t i = 0; i < BLOCK_SIZE; i++) {
        headblock[i] = UINT32_MAX;
      }
      mHeadBlocks.add(headblock);
    } while (headblockno >= mHeadBlocks.size());
    head = UINT32_MAX;
  }

  // suppress duplicate transitions
  uint32_t nodeindex;
  uint32_t* nodeblock;
  if ((head & TAG_DATA) == 0) {
    const uint32_t nodeblockno = head >> BLOCK_SHIFT;
    nodeblock = mNodeBlocks.get(nodeblockno);
    nodeindex = head & BLOCK_MASK;
    if (nodeblock[nodeindex] == source) {
      return;
    }
    if ((nodeindex & NODE_MASK) != 0) {
      if (mNumTransitions >= mTransitionLimit) {
        throw jni::PreOverflowException(jni::OverflowKind_TRANSITION,
                                        mTransitionLimit);
      }
      mNumTransitions++;
      headblock[headindex] = head - 1;
      nodeblock[nodeindex - 1] = source;
      return;
    }
  } else if (head == UINT32_MAX) {
    headblock[headindex] = TAG_DATA | source;
    mNumTransitions++;
    return;
  } else if ((head & ~TAG_DATA) == source) {
    return;
  } else {
    nodeindex = UINT32_MAX;
    nodeblock = 0;
  }

  // allocate and/or populate node
  if (mNumTransitions >= mTransitionLimit) {
    throw jni::PreOverflowException(jni::OverflowKind_TRANSITION,
                                    mTransitionLimit);
  }
  mNumTransitions++;
  if (mNextLocalIndex == BLOCK_SIZE) {
    mCurrentNodeBlock = new uint32_t[BLOCK_SIZE];
    mNodeBlocks.add(mCurrentNodeBlock);
    mNextLocalIndex = 0;
  }
  headblock[headindex] = mNextGlobalIndex + NODE_SIZE - 2;
  mNextGlobalIndex += NODE_SIZE;
  nodeindex = mNextLocalIndex + NODE_SIZE - 2; 
  mCurrentNodeBlock[nodeindex++] = source;
  mCurrentNodeBlock[nodeindex++] = head;
  mNextLocalIndex = nodeindex;
}

uint32_t ReverseTransitionStore::
iterator(uint32_t target)
  const
{
  const uint32_t headblockno = target >> BLOCK_SHIFT;
  if (headblockno < mHeadBlocks.size()) {
    const uint32_t* headblock = mHeadBlocks.get(headblockno);
    const uint32_t headindex = target & BLOCK_MASK;
    return headblock[headindex];
  } else {
    return UINT32_MAX;
  }
}

uint32_t ReverseTransitionStore::
hasNext(uint32_t iterator)
  const
{
  return iterator != UINT32_MAX;
}

uint32_t ReverseTransitionStore::
next(uint32_t& iterator)
  const
{
  if (iterator & TAG_DATA) {
    const uint32_t data = iterator & ~TAG_DATA;
    iterator = UINT32_MAX;
    return data;
  } else {
    const uint32_t blockno = iterator >> BLOCK_SHIFT;
    const uint32_t* block = mNodeBlocks.get(blockno);
    const uint32_t index = iterator & BLOCK_MASK;
    if ((index & NODE_MASK) == NODE_SIZE - 2) {
      iterator = block[index + 1];
    } else {
      iterator++;
    }
    return block[index];
  }
}

uint32_t ReverseTransitionStore::
getFirstPredecessor(uint32_t target)
  const
{
  uint32_t source = UINT32_MAX;
  uint32_t iter = iterator(target);
  while (hasNext(iter)) {
    source = next(iter);
  }
  return source;
}


//############################################################################
//# ReverseTransitionStore: Debug Output

#ifdef DEBUG

#define DUMPING(code) \
  (code & TAG_DATA ? "+" : "") << (code & ~TAG_DATA)

void ReverseTransitionStore::
dump(uint32_t numstates)
  const
{
  std::cerr << "HEADS:" << std::endl;
  for (uint32_t hindex = 0; hindex < numstates; hindex++) {
    const uint32_t headblockno = hindex >> BLOCK_SHIFT;
    const uint32_t headindex = hindex & BLOCK_MASK;
    const uint32_t* headblock = mHeadBlocks.get(headblockno);
    const uint32_t head = headblock[headindex];
    std::cerr << "H" << hindex << ": " << DUMPING(head) << std::endl;
  }
  for (uint32_t bindex = 0; bindex < mNextGlobalIndex; bindex++) {
    const uint32_t blockno = bindex >> BLOCK_SHIFT;
    const uint32_t index = bindex & BLOCK_MASK;
    const uint32_t* block = mNodeBlocks.get(blockno);
    const uint32_t data = block[index];
    std::cerr << "N" << bindex << ": " << DUMPING(data) << std::endl;
  }
}

#endif /* DEBUG */


}  /* namespace waters */
