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

#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/MapGlue.h"
#include "jni/glue/StateGlue.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/TransitionRecord.h"


namespace waters {

//############################################################################
//# class TransitionRecord
//############################################################################

//############################################################################
//# TransitionRecord: Class Variables

const TransitionRecordAccessorForSearch
  TransitionRecord::theControllableSearchAccessor(true);
const TransitionRecordAccessorForSearch
  TransitionRecord::theUncontrollableSearchAccessor(false);
const TransitionRecordAccessorForTrace TransitionRecord::theTraceAccessor;


//############################################################################
//# TransitionRecord: Constructors & Destructors

TransitionRecord::
TransitionRecord(const AutomatonRecord* aut,
                 TransitionRecord* next,
                 TransitionRecord* fwd)
  : mAutomaton(aut),
    mWeight(0),
    mIsOnlySelfloops(true),
    mNumNondeterministicSuccessors(0),
    mNondeterministicBuffer(0),
    mNondeterministicSuccessorsShifted(0),
    mNumNotTaken(0),
    mNextInSearch(next),
    mNextInUpdate(0),
    mNextInNotTaken(0)
{
  const uint32 numstates = aut->getNumberOfStates();
  mFlags = new uint32[numstates];
  mDeterministicSuccessorsShifted = new uint32[numstates];
  if (fwd == 0) {
    mForwardRecord = this;
    for (uint32 code = 0; code < numstates; code++) {
      mFlags[code] = 0;
      mDeterministicSuccessorsShifted[code] = NO_TRANSITION;
    }
  } else {
    mForwardRecord = fwd;
    for (uint32 code = 0; code < numstates; code++) {
      mFlags[code] = fwd->mFlags[code];
      mDeterministicSuccessorsShifted[code] = NO_TRANSITION;
    }
  }
}

TransitionRecord::
~TransitionRecord()
{
  delete[] mFlags;
  delete[] mDeterministicSuccessorsShifted;
  delete[] mNumNondeterministicSuccessors;
  delete[] mNondeterministicBuffer;
  delete[] mNondeterministicSuccessorsShifted;
  if (mForwardRecord != this) {
    delete mForwardRecord;
  }
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
compareToForSearch(const TransitionRecord* partner, bool controllable)
  const
{
  const AutomatonRecord* aut1 = mAutomaton;
  const AutomatonRecord* aut2 = partner->mAutomaton;
  if (!controllable) {
    const bool isplant1 = aut1->isPlant();
    const bool isplant2 = aut2->isPlant();
    if (isplant1 && !isplant2) {
      return -1;
    } else if (!isplant1 && isplant2) {
      return 1;
    }
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

const TransitionRecordAccessorForSearch* TransitionRecord::
getSearchAccessor(bool controllable)
{
  if (controllable) {
    return &theControllableSearchAccessor;
  } else {
    return &theUncontrollableSearchAccessor;
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
    mFlags[source] |= FLAG_NONDET;
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
    const uint32 offset = mNumNondeterministicSuccessors[source]++;
    mNondeterministicSuccessorsShifted[source][offset] = shiftedtarget;
  }
}

void TransitionRecord::
normalize()
{
  const int numstates = mAutomaton->getNumberOfStates();
  mNumNotTaken = mWeight;
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
  const uint32 numstates = mAutomaton->getNumberOfStates();
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

bool TransitionRecord::
markTransitionTaken(const uint32* tuple)
{
  uint32 index = mAutomaton->getAutomatonIndex();
  uint32 code = tuple[index];
  uint32 flags = mFlags[code];
  if ((flags & FLAG_TAKEN) == 0) {
    mFlags[code] = flags | FLAG_TAKEN;
    return --mNumNotTaken == 0;
  } else {
    return false;
  }
}

int TransitionRecord::
removeTransitionsNotTaken()
{
  if (mNumNotTaken) {
    uint32 numstates = mAutomaton->getNumberOfStates();
    int shift = mAutomaton->getShift();
    bool keepnd = false;
    bool onlyself = true;
    int newweight = 0;
    for (uint32 source = 0; source < numstates; source++) {
      if (mFlags[source] & FLAG_TAKEN) {
        uint32 succ = mDeterministicSuccessorsShifted[source];
        switch (succ) {
        case NO_TRANSITION:
          break;
        case MULTIPLE_TRANSITIONS:
          keepnd = true;
          onlyself = false;
          newweight++;
          break;
        default:
          onlyself &= (succ == (source << shift));
          newweight++;
          break;
        }      
      } else {
        mDeterministicSuccessorsShifted[source] = NO_TRANSITION;
      }
    }
    int removed = mNumNotTaken;
    mWeight = newweight;
    mIsOnlySelfloops = onlyself;
    normalize();
    if (!keepnd) {
      delete [] mNondeterministicBuffer;
      delete [] mNondeterministicSuccessorsShifted;
      mNumNondeterministicSuccessors = mNondeterministicBuffer = 0;
      mNondeterministicBuffer = 0;
      mNondeterministicSuccessorsShifted = 0;
    }
    return removed;
  } else {
    return 0;
  }
}

void TransitionRecord::
removeSelfloops()
{
  uint32 numstates = mAutomaton->getNumberOfStates();
  int shift = mAutomaton->getShift();
  bool keepnd = false;
  bool renorm = false;
  int newweight = 0;
  for (uint32 source = 0; source < numstates; source++) {
    uint32 shiftedsource = source << shift;
    uint32 succ = mDeterministicSuccessorsShifted[source];
    if (succ == shiftedsource) {
      mDeterministicSuccessorsShifted[source] = NO_TRANSITION;
      renorm = true;
    } else if (succ == MULTIPLE_TRANSITIONS) {
      uint32* ndlist = mNondeterministicSuccessorsShifted[source];
      uint32 ndcount = mNumNondeterministicSuccessors[source];
      uint32 writeindex = UNDEF_UINT32;
      for (uint32 readindex = 0; readindex < ndcount; readindex++) {
        succ = ndlist[readindex];
        if (succ == shiftedsource) {
          if (writeindex == UNDEF_UINT32) {
            writeindex = readindex;
          }
        } else if (writeindex != UNDEF_UINT32) {
          ndlist[writeindex++] = succ;
        }
      }
      switch (writeindex) {
      case 0:
        mDeterministicSuccessorsShifted[source] = NO_TRANSITION;
        renorm = true;
        break;
      case 1:
        mDeterministicSuccessorsShifted[source] = ndlist[0];
        newweight++;
        break;
      default:
        mNumNondeterministicSuccessors[source] = writeindex;
        // fall through ...
      case UNDEF_UINT32:
        keepnd = true;
        newweight++;
        break;
      }
    } else if (succ != NO_TRANSITION) {
      newweight++;
    }
  }
  if (renorm) {
    mWeight = newweight;
    normalize();
  }
  if (!keepnd) {
    delete [] mNondeterministicBuffer;
    delete [] mNondeterministicSuccessorsShifted;
    mNumNondeterministicSuccessors = mNondeterministicBuffer = 0;
    mNondeterministicBuffer = 0;
    mNondeterministicSuccessorsShifted = 0;
  }
}


//############################################################################
//# TransitionRecord: Trace Computation

void TransitionRecord::
storeNondeterministicTarget(const uint32* sourcetuple,
                            const uint32* targettuple,
                            const jni::MapGlue& statemap)
  const
{
  if (mForwardRecord != this) {
    mForwardRecord->storeNondeterministicTarget
      (sourcetuple, targettuple, statemap);
  } else {
    const uint32 index = mAutomaton->getAutomatonIndex();
    const uint32 source = sourcetuple[index];
    if (mFlags[source] & FLAG_NONDET) {
      const uint32 target = targettuple[index];
      const jni::StateGlue& state = mAutomaton->getJavaState(target);
      const jni::AutomatonGlue& aut = mAutomaton->getJavaAutomaton();
      statemap.put(&aut, &state);
    }
  }
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
  mIndex = aut->getFirstInitialState1();
}

bool NondeterministicTransitionIterator::
advanceInit(uint32* tuple)
{
  const int w = mAutomatonRecord->getWordIndex();
  tuple[w] &= ~mAutomatonRecord->getBitMask();
  const uint32 next = mIndex + 1;
  bool result;
  if (next >= mAutomatonRecord->getEndOfInitialStates2()) {
    mIndex = mAutomatonRecord->getFirstInitialState1();
    result = true;
  } else if (next >= mAutomatonRecord->getEndOfInitialStates1()) {
    mIndex = mAutomatonRecord->getFirstInitialState2();
    result = false;
  } else {
    mIndex = next;
    result = false;
  }
  tuple[w] |= mIndex << mAutomatonRecord->getShift();
  return result;
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
