//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   Tarjan
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "waters/analysis/Tarjan.h"
#include "waters/analysis/TransitionRecord.h"


namespace waters {

//############################################################################
//# class TarjanStackFrameNondeterministic
//############################################################################

//############################################################################
//# TarjanStackFrameNondeterministic: Constructors & Destructors

TarjanStackFrameNondeterministic::
TarjanStackFrameNondeterministic(int size)
  : mTransitionIterators(new NondeterministicTransitionIterator[size]),
    mTransitionIteratorEnd(0)
{
}

TarjanStackFrameNondeterministic::
~TarjanStackFrameNondeterministic()
{
  delete [] mTransitionIterators;
}
  

//############################################################################
//# TarjanStackFrameNondeterministic: Advanced Access

uint32_t TarjanStackFrameNondeterministic::
setupTransitionIterator(const TransitionRecord* trans, uint32_t source)
{
  int ndend = mTransitionIteratorEnd++;
  return mTransitionIterators[ndend].setup(trans, source);
}

bool TarjanStackFrameNondeterministic::
advanceTransitionIterators(uint32_t* bufferpacked)
{
  for (int index = 0; index < mTransitionIteratorEnd; index++) {
    if (!mTransitionIterators[index].advance(bufferpacked)) {
      return true;
    }
  }
  mTransitionIteratorEnd = 0;
  return false;
}


//############################################################################
//# class TarjanStackFrame
//############################################################################

//############################################################################
//# TarjanStackFrame: Constructors & Destructors

TarjanStackFrame::
TarjanStackFrame()
  : mNondeterministicInfo(0)
{
}

TarjanStackFrame::
~TarjanStackFrame()
{
  delete mNondeterministicInfo;
}
  

//############################################################################
//# TarjanStackFrame: Advanced Access

void TarjanStackFrame::
reset(uint32_t state)
{
  mIsRoot = true;
  mStateCode = state;
  mEventCode = 0;
  if (mNondeterministicInfo != 0) {
    mNondeterministicInfo->reset();
  }
}

void TarjanStackFrame::
createNondeterministicTransitionIterators(int max)
{
  if (mNondeterministicInfo == 0) {
    mNondeterministicInfo = new TarjanStackFrameNondeterministic(max);
  }
}


//############################################################################
//# class TarjanControlStack
//############################################################################

//############################################################################
//# TarjanControlStack: Access

TarjanStackFrame& TarjanControlStack::
push(uint32_t state)
{
  if (mStackPointer == size()) {
    add();
  }
  TarjanStackFrame& frame = getref(mStackPointer++);
  frame.reset(state);
  return frame;
}

void TarjanControlStack::
clear()
{
  BlockedArrayList<TarjanStackFrame>::clear();
  mStackPointer = 0;
}


//############################################################################
//# class TarjanStateStack
//############################################################################

//############################################################################
//# TarjanStateStack: Access

void TarjanStateStack::
push(uint32_t state)
{
  if (mStackPointer == size()) {
    add(state);
    mStackPointer++;
  } else {
    getref(mStackPointer++) = state;
  }
}

void TarjanStateStack::
clear()
{
  BlockedArrayList<uint32_t>::clear();
  mStackPointer = 0;
}


}  /* namespace waters */
