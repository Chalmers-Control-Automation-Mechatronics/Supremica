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
  // suppress selfloops
  if (source == target) {
    return;
  }
  // find or allocate block
  const uint32 headblockno = target >> BLOCK_SHIFT;
  const uint32 headindex = target & BLOCK_MASK;
  uint32* headblock;
  uint32 head;
  if (headblockno < mHeadBlocks.size()) {
    headblock = mHeadBlocks.get(headblockno);
    head = headblock[headindex];
  } else {
    do {
      headblock = new uint32[BLOCK_SIZE];
      for (uint32 i = 0; i < BLOCK_SIZE; i++) {
        headblock[i] = UNDEF_UINT32;
      }
      mHeadBlocks.add(headblock);
    } while (headblockno >= mHeadBlocks.size());
    head = UNDEF_UINT32;
  }

  // suppress duplicate transitions
  bool alloc;
  uint32 nodeindex;
  uint32* nodeblock;
  if ((head & TAG_DATA) == 0) {
    const uint32 nodeblockno = head >> BLOCK_SHIFT;
    nodeblock = mNodeBlocks.get(nodeblockno);
    nodeindex = head & BLOCK_MASK;
    if (nodeblock[nodeindex] == source) {
      return;
    }
    if ((nodeindex & NODE_MASK) != 0) {
      if (mNumTransitions >= mTransitionLimit) {
        throw jni::PreJavaException(jni::CLASS_OverflowException,
                                    "Transition limit exceeded!",
                                    true);
      }
      mNumTransitions++;
      headblock[headindex] = head - 1;
      nodeblock[nodeindex - 1] = source;
      return;
    }
  } else if (head == UNDEF_UINT32) {
    headblock[headindex] = TAG_DATA | source;
    mNumTransitions++;
    return;
  } else if ((head & ~TAG_DATA) == source) {
    return;
  } else {
    nodeindex = UNDEF_UINT32;
    nodeblock = 0;
    alloc = true;
  }

  // allocate and/or populate node
  if (mNumTransitions >= mTransitionLimit) {
    throw jni::PreJavaException(jni::CLASS_OverflowException,
                                "Transition limit exceeded!",
                                true);
  }
  mNumTransitions++;
  if (mNextLocalIndex == BLOCK_SIZE) {
    mCurrentNodeBlock = new uint32[BLOCK_SIZE];
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

uint32 ReverseTransitionStore::
iterator(uint32 target)
  const
{
  const uint32 headblockno = target >> BLOCK_SHIFT;
  if (headblockno < mHeadBlocks.size()) {
    const uint32* headblock = mHeadBlocks.get(headblockno);
    const uint32 headindex = target & BLOCK_MASK;
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
    const uint32 blockno = iterator >> BLOCK_SHIFT;
    const uint32* block = mNodeBlocks.get(blockno);
    const uint32 index = iterator & BLOCK_MASK;
    if ((index & NODE_MASK) == NODE_SIZE - 2) {
      iterator = block[index + 1];
    } else {
      iterator++;
    }
    return block[index];
  }
}


//############################################################################
//# ReverseTransitionStore: Debug Output

#ifdef DEBUG

#define DUMPING(code) \
  (code & TAG_DATA ? "+" : "") << (code & ~TAG_DATA)

void ReverseTransitionStore::
dump(uint32 numstates)
  const
{
  std::cerr << "HEADS:" << std::endl;
  for (uint32 hindex = 0; hindex < numstates; hindex++) {
    const uint32 headblockno = hindex >> BLOCK_SHIFT;
    const uint32 headindex = hindex & BLOCK_MASK;
    const uint32* headblock = mHeadBlocks.get(headblockno);
    const uint32 head = headblock[headindex];
    std::cerr << "H" << hindex << ": " << DUMPING(head) << std::endl;
  }
  for (uint32 bindex = 0; bindex < mNextGlobalIndex; bindex++) {
    const uint32 blockno = bindex >> BLOCK_SHIFT;
    const uint32 index = bindex & BLOCK_MASK;
    const uint32* block = mNodeBlocks.get(blockno);
    const uint32 data = block[index];
    std::cerr << "N" << bindex << ": " << DUMPING(data) << std::endl;
  }
}
    
#endif /* DEBUG */


}  /* namespace waters */
