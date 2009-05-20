//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionRecord
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/TransitionRecord.h"


namespace waters {

//############################################################################
//# class TransitionRecord
//############################################################################

//############################################################################
//# TransitionRecord: Class Variables

const TransitionRecordAccessorForSearch TransitionRecord::theSearchAccessor;
const TransitionRecordAccessorForTrace TransitionRecord::theTraceAccessor;


//############################################################################
//# TransitionRecord: Constructors & Destructors

TransitionRecord::
TransitionRecord(const AutomatonRecord* aut, TransitionRecord* next)
  : mAutomaton(aut),
    mWeight(0),
    mIsOnlySelfloops(true),
    mNumNondeterministicSuccessors(0),
    mNondeterministicBuffer(0),
    mNondeterministicSuccessorsShifted(0),
    mNextInSearch(next),
    mNextInUpdate(0)
{
  const uint32 numstates = (uint32) aut->getNumberOfStates();
  mDeterministicSuccessorsShifted = new uint32[numstates];
  for (uint32 code = 0; code < numstates; code++) {
    mDeterministicSuccessorsShifted[code] = NO_TRANSITION;
  }
}

TransitionRecord::
~TransitionRecord()
{
  delete[] mDeterministicSuccessorsShifted;
  delete[] mNumNondeterministicSuccessors;
  delete[] mNondeterministicBuffer;
  delete[] mNondeterministicSuccessorsShifted;
  delete mNextInSearch;
}


//############################################################################
//# TransitionRecord: Simple Access

uint32 TransitionRecord::
getNumberOfSuccessors(uint32 source)
  const
{
  switch (mDeterministicSuccessorsShifted[source]) {
  case NO_TRANSITION:
    return 0;
  case MULTIPLE_TRANSITIONS:
    return mNumNondeterministicSuccessors[source];
  default:
    return 1;
  }  
}

uint32 TransitionRecord::
getSuccessorShifted(uint32 source, uint32 index)
  const
{
  const uint32 lookup = mDeterministicSuccessorsShifted[source];
  if (lookup != MULTIPLE_TRANSITIONS) {
    return lookup;
  } else if (index < mNumNondeterministicSuccessors[source]) {
    return mNondeterministicSuccessorsShifted[source][index];
  } else {
    return NO_TRANSITION;
  }
}

float TransitionRecord::
getProbability()
  const
{
  return PROBABILITY_ADJUST * mWeight;
}


//############################################################################
//# TransitionRecord: Comparing

int TransitionRecord::
compareToForSearch(const TransitionRecord* partner)
  const
{
  const AutomatonRecord* aut1 = mAutomaton;
  const bool isplant1 = aut1->isPlant();
  const AutomatonRecord* aut2 = partner->mAutomaton;
  const bool isplant2 = aut2->isPlant();
  if (isplant1 && !isplant2) {
    return -1;
  } else if (!isplant1 && isplant2) {
    return 1;
  }
  const int weight1 = mWeight;
  const int weight2 = partner->mWeight;
  if (weight1 != weight2) {
    return weight1 - weight2;
  } else {
    return aut1->compareTo(aut2);
  }
}

int TransitionRecord::
compareToForTrace(const TransitionRecord* partner)
  const
{
  const int weight1 = mWeight;
  const int weight2 = partner->mWeight;
  if (weight1 != weight2) {
    return weight1 - weight2;
  } else {
    const AutomatonRecord* aut1 = mAutomaton;
    const AutomatonRecord* aut2 = partner->mAutomaton;
    return aut1->compareTo(aut2);
  }
}


//############################################################################
//# TransitionRecord: Set up

bool TransitionRecord::
addDeterministicTransition(uint32 source, uint32 target)
{
  const uint32 lookup = mDeterministicSuccessorsShifted[source];
  if (lookup == NO_TRANSITION) {
    const int shift = mAutomaton->getShift();
    mDeterministicSuccessorsShifted[source] = target << shift;
    mWeight++;
    if (source != target) {
      mIsOnlySelfloops = false;
    }
    return true;
  } else if (lookup == MULTIPLE_TRANSITIONS) {
    mNumNondeterministicSuccessors[source]++;
    return false;
  } else {
    if (mNumNondeterministicSuccessors == 0) {
      const uint32 numstates = mAutomaton->getNumberOfStates();
      mNumNondeterministicSuccessors = new uint32[numstates];
    }
    mDeterministicSuccessorsShifted[source] = MULTIPLE_TRANSITIONS;
    mNumNondeterministicSuccessors[source] = 2;
    mIsOnlySelfloops = false;
    return false;
  }
}

void TransitionRecord::
addNondeterministicTransition(uint32 source, uint32 target)
{
  if (mDeterministicSuccessorsShifted[source] == MULTIPLE_TRANSITIONS) {
    setupNondeterministicBuffers();
    const int shift = mAutomaton->getShift();
    const uint32 shiftedtarget = target << shift;
    // Maybe add check for duplicates here ...
    const uint32 offset = mNumNondeterministicSuccessors[source]++;
    mNondeterministicSuccessorsShifted[source][offset] = shiftedtarget;
  }
}

void TransitionRecord::
normalize()
{
  const int numstates = mAutomaton->getNumberOfStates();
  if (mWeight == numstates) {
    mWeight = PROBABILITY_1;
  } else {
    mWeight *= PROBABILITY_1 / numstates;
  }
}

uint32 TransitionRecord::
getCommonTarget()
  const
{
  if (mNondeterministicBuffer != 0) {
    return UNDEF_UINT32;
  }
  const uint32 numstates = (uint32) mAutomaton->getNumberOfStates();
  uint32 result = UNDEF_UINT32;
  for (uint32 code = 0; code < numstates; code++) {
    const uint32 succ = mDeterministicSuccessorsShifted[code];
    if (succ != NO_TRANSITION) {
      if (result == UNDEF_UINT32) {
        result = succ;
      } else if (result != succ) {
        return UNDEF_UINT32;
      }
    }      
  }
  return result;
}


//############################################################################
//# TransitionRecord: Auxiliary Methods

void TransitionRecord::
setupNondeterministicBuffers()
{
  if (mNondeterministicBuffer == 0) {
    const uint32 numstates = mAutomaton->getNumberOfStates();
    uint32 buffersize = 0;
    uint32 state;
    for (state = 0; state < numstates; state++) {
      if (mDeterministicSuccessorsShifted[state] == MULTIPLE_TRANSITIONS) {
        buffersize += mNumNondeterministicSuccessors[state];
      }
    }
    mNondeterministicBuffer = new uint32[buffersize];
    mNondeterministicSuccessorsShifted = new uint32*[numstates];
    uint32 next = 0;
    for (state = 0; state < numstates; state++) {
      switch (mDeterministicSuccessorsShifted[state]) {
      case NO_TRANSITION:
        mNondeterministicSuccessorsShifted[state] = 0;
        mNumNondeterministicSuccessors[state] = 0;
        break;
      case MULTIPLE_TRANSITIONS:
        mNondeterministicSuccessorsShifted[state] =
          &mNondeterministicBuffer[next];
        next += mNumNondeterministicSuccessors[state];
        mNumNondeterministicSuccessors[state] = 0;
        break;       
      default:
        mNondeterministicSuccessorsShifted[state] = 0;
        mNumNondeterministicSuccessors[state] = 1;
        break;
      }
    }
  }
}


//############################################################################
//# class NondeterministicTransitionIterator
//############################################################################

//############################################################################
//# NondeterministicTransitionIterator: Constructors & Destructors
  
NondeterministicTransitionIterator::
NondeterministicTransitionIterator() :
  mAutomatonRecord(0), mTransitionRecord(0), mSource(0), mIndex(UNDEF_UINT32)
{
}


//############################################################################
//# NondeterministicTransitionIterator: Initial State Iteration

void NondeterministicTransitionIterator::
setupInit(const AutomatonRecord* aut)
{
  mAutomatonRecord = aut;
  mIndex = aut->getFirstInitialState();
}

bool NondeterministicTransitionIterator::
advanceInit(uint32* tuple)
{
  const int w = mAutomatonRecord->getWordIndex();
  tuple[w] &= ~mAutomatonRecord->getBitMask();
  const uint32 next = mIndex + 1;
  if (next < mAutomatonRecord->getEndOfInitialStates()) {
    mIndex = next;
    tuple[w] |= next;
    return false;
  } else {
    mIndex = mAutomatonRecord->getFirstInitialState();
    return true;
  }
}


//############################################################################
//# NondeterministicTransitionIterator: Transition Iteration

uint32 NondeterministicTransitionIterator::
setup(const TransitionRecord* trans, uint32 source)
{
  mAutomatonRecord = trans->getAutomaton();
  mTransitionRecord = trans;
  mSource = source;
  return reset();
}

bool NondeterministicTransitionIterator::
advance(uint32* tuple)
{
  const AutomatonRecord* aut = mAutomatonRecord;
  const int w = aut->getWordIndex();
  tuple[w] &= ~aut->getBitMask();
  uint32 succ = next();
  if (succ == TransitionRecord::NO_TRANSITION) {
    tuple[w] |= reset();
    return true;
  } else {
    tuple[w] |= succ;
    return false;
  }
}

uint32 NondeterministicTransitionIterator::
reset()
{
  mIndex =
    mTransitionRecord->getNumberOfNondeterministicSuccessors(mSource) - 1;
  return current();
}

uint32 NondeterministicTransitionIterator::
next()
{
  if (mIndex > 0) {
    mIndex--;
    return current();
  } else {
    return TransitionRecord::NO_TRANSITION;
  }
}

uint32 NondeterministicTransitionIterator::
current()
{
  return
    mTransitionRecord->getNondeterministicSuccessorShifted(mSource, mIndex);
}


}  /* namespace waters */
