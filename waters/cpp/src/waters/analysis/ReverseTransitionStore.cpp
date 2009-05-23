//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ReverseTransitionStore
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include "jni/cache/PreJavaException.h"
#include "jni/glue/Glue.h"

#include "waters/analysis/ReverseTransitionStore.h"


namespace waters {

//############################################################################
//# class ReverseTransitionStore
//############################################################################

//############################################################################
//# ReverseTransitionStore: Constructors & Destructors

ReverseTransitionStore::
ReverseTransitionStore(uint32 limit)
  : mTransitionLimit(limit),
    mNumTransitions(0),
    mNextLocalIndex(BLOCKSIZE),
    mNextGlobalIndex(0),
    mCurrentNodeBlock(0),
    mHeadBlocks(INITBLOCKS),
    mNodeBlocks(INITBLOCKS)
{
}

ReverseTransitionStore::
~ReverseTransitionStore()
{
  for (uint32 i = 0; i < mHeadBlocks.size(); i++) {
    uint32* block = mHeadBlocks.get(i);
    delete [] block;
  }
  for (uint32 j = 0; j < mNodeBlocks.size(); j++) {
    uint32* block = mNodeBlocks.get(j);
    delete [] block;
  }
}


//############################################################################
//# ReverseTransitionStore: Access

void ReverseTransitionStore::
addTransition(uint32 source, uint32 target)
{
  // suppress selflloops
  if (source == target) {
    return;
  }

  // find or allocate block
  const uint32 headblockno = target >> BLOCKSHIFT;
  const uint32 headindex = target & BLOCKMASK;
  uint32* headblock;
  uint32 head;
  if (headblockno < mHeadBlocks.size()) {
    headblock = mHeadBlocks.get(headblockno);
    head = headblock[headindex];
  } else {
    do {
      headblock = new uint32[BLOCKSIZE];
      for (uint32 i = 0; i < BLOCKSIZE; i++) {
        headblock[i] = UNDEF_UINT32;
      }
      mHeadBlocks.add(headblock);
    } while (headblockno >= mHeadBlocks.size());
    head = UNDEF_UINT32;
  }

  // suppress duplicate transitions
  if (head & TAG_DATA) {
    if (head == UNDEF_UINT32) {
      headblock[headindex] = TAG_DATA | source;
      mNumTransitions++;
      return;
    } else if ((head & ~TAG_DATA) == source) {
      return;
    }
  } else {
    const uint32 nodeblockno = head >> BLOCKSHIFT;
    const uint32* nodeblock = mNodeBlocks.get(nodeblockno);
    const uint32 nodeindex = head & BLOCKMASK;
    if (nodeblock[nodeindex] == source) {
      return;
    }
  }

  // allocate and populate new node
  if (mNumTransitions >= mTransitionLimit) {
    throw jni::PreJavaException(jni::CLASS_OverflowException,
                                "Transition limit exceeded!",
                                true);
  }
  mNumTransitions++;
  if (mNextLocalIndex == BLOCKSIZE) {
    mCurrentNodeBlock = new uint32[BLOCKSIZE];
    mNodeBlocks.add(mCurrentNodeBlock);
    mNextLocalIndex = 0;
  }
  headblock[headindex] = mNextGlobalIndex;
  mNextGlobalIndex += 2;
  mCurrentNodeBlock[mNextLocalIndex++] = source;
  mCurrentNodeBlock[mNextLocalIndex++] = head;
}

uint32 ReverseTransitionStore::
iterator(uint32 target)
  const
{
  const uint32 headblockno = target >> BLOCKSHIFT;
  if (headblockno < mHeadBlocks.size()) {
    const uint32* headblock = mHeadBlocks.get(headblockno);
    const uint32 headindex = target & BLOCKMASK;
    return headblock[headindex];
  } else {
    return UNDEF_UINT32;
  }
}

uint32 ReverseTransitionStore::
hasNext(uint32 iterator)
  const
{
  return iterator != UNDEF_UINT32;
}

uint32 ReverseTransitionStore::
next(uint32& iterator)
  const
{
  if (iterator & TAG_DATA) {
    const uint32 data = iterator & ~TAG_DATA;
    iterator = UNDEF_UINT32;
    return data;
  } else {
    const uint32 blockno = iterator >> BLOCKSHIFT;
    const uint32* block = mNodeBlocks.get(blockno);
    const uint32 index = iterator & BLOCKMASK;
    iterator = block[index + 1];
    return block[index];
  }
}


}  /* namespace waters */
