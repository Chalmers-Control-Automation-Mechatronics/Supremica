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
//# class TarjanStackFrame
//############################################################################

//############################################################################
//# TarjanStackFrame: Constructors & Destructors

TarjanStackFrame::
TarjanStackFrame()
  : mNondeterministicTransitionIterators(0)
{
}

TarjanStackFrame::
~TarjanStackFrame()
{
  delete [] mNondeterministicTransitionIterators;
}
  

//############################################################################
//# TarjanStackFrame: Advanced Access

void TarjanStackFrame::
reset(uint32 state)
{
  mIsRoot = true;
  mStateCode = state;
  mEventCode = 0;
  mNondeterministicTransitionIteratorEnd = 0;
}

void TarjanStackFrame::
createNondeterministicTransitionIterators(int max)
{
  if (mNondeterministicTransitionIterators == 0) {
    mNondeterministicTransitionIterators =
      new NondeterministicTransitionIterator[max];
  }
}

uint32 TarjanStackFrame::
setupNondeterministicTransitionIterator(const TransitionRecord* trans,
					uint32 source)
{
  int ndend = mNondeterministicTransitionIteratorEnd++;
  return mNondeterministicTransitionIterators[ndend].setup(trans, source);
}

bool TarjanStackFrame::
advanceNondeterministicTransitionIterators(uint32* bufferpacked)
{
  for (int index = 0;
       index < mNondeterministicTransitionIteratorEnd;
       index++) {
    if (!mNondeterministicTransitionIterators[index].advance(bufferpacked)) {
      return true;
    }
  }
  mNondeterministicTransitionIteratorEnd = 0;
  return false;
}


//############################################################################
//# class TarjanControlStack
//############################################################################

//############################################################################
//# TarjanControlStack: Access

TarjanStackFrame& TarjanControlStack::
push(uint32 state)
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
push(uint32 state)
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
  BlockedArrayList<uint32>::clear();
  mStackPointer = 0;
}


}  /* namespace waters */
