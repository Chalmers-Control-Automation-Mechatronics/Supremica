//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
                 const TransitionRecord* fwd)
  : mAutomaton(aut),
    mWeight(0),
    mIsReversedCopy(false),
    mIsOnlySelfloops(true),
    mNumNondeterministicSuccessors(0),
    mNondeterministicBuffer(0),
    mNondeterministicSuccessorsShifted(0),
    mNumNotTaken(0),
    mNextInSearch(next),
    mNextInUpdate(0),
    mNextInNotTaken(0)
{
  const uint32_t numstates = aut->getNumberOfStates();
  mFlags = new uint32_t[numstates];
  mDeterministicSuccessorsShifted = new uint32_t[numstates];
  for (uint32_t code = 0; code < numstates; code++) {
    mFlags[code] = fwd == 0 ? 0 : fwd->mFlags[code];
    mDeterministicSuccessorsShifted[code] = NO_TRANSITION;
  }
}

TransitionRecord::
TransitionRecord(const TransitionRecord& fwd)
  : mAutomaton(fwd.mAutomaton),
    mWeight(fwd.mWeight),
    mIsReversedCopy(true),
    mIsOnlySelfloops(fwd.mIsOnlySelfloops),
    mFlags(fwd.mFlags),
    mDeterministicSuccessorsShifted(fwd.mDeterministicSuccessorsShifted),
    mNumNondeterministicSuccessors(fwd.mNumNondeterministicSuccessors),
    mNondeterministicBuffer(fwd.mNondeterministicBuffer),
    mNondeterministicSuccessorsShifted(fwd.mNondeterministicSuccessorsShifted),
    mNumNotTaken(0),
    mNextInSearch(0),
    mNextInUpdate(0),
    mNextInNotTaken(0)
{
}

TransitionRecord::
~TransitionRecord()
{
  if (!mIsReversedCopy) {
    delete[] mFlags;
    delete[] mDeterministicSuccessorsShifted;
    delete[] mNumNondeterministicSuccessors;
    delete[] mNondeterministicBuffer;
    delete[] mNondeterministicSuccessorsShifted;
  }
  delete mNextInSearch;
}


//############################################################################
//# TransitionRecord: Simple Access

uint32_t TransitionRecord::
getNumberOfSuccessors(uint32_t source)
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

uint32_t TransitionRecord::
getSuccessorShifted(uint32_t source, uint32_t index)
  const
{
  const uint32_t lookup = mDeterministicSuccessorsShifted[source];
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
addDeterministicTransition(uint32_t source, uint32_t target)
{
  const uint32_t lookup = mDeterministicSuccessorsShifted[source];
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
      const uint32_t numstates = mAutomaton->getNumberOfStates();
      mNumNondeterministicSuccessors = new uint32_t[numstates];
    }
    mFlags[source] |= FLAG_NONDET;
    mDeterministicSuccessorsShifted[source] = MULTIPLE_TRANSITIONS;
    mNumNondeterministicSuccessors[source] = 2;
    mIsOnlySelfloops = false;
    return false;
  }
}

void TransitionRecord::
addNondeterministicTransition(uint32_t source, uint32_t target)
{
  if (mDeterministicSuccessorsShifted[source] == MULTIPLE_TRANSITIONS) {
    setupNondeterministicBuffers();
    const int shift = mAutomaton->getShift();
    const uint32_t shiftedtarget = target << shift;
    const uint32_t offset = mNumNondeterministicSuccessors[source]++;
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

uint32_t TransitionRecord::
getCommonTarget()
  const
{
  if (mNondeterministicBuffer != 0) {
    return UINT32_MAX;
  }
  const uint32_t numstates = mAutomaton->getNumberOfStates();
  uint32_t result = UINT32_MAX;
  for (uint32_t code = 0; code < numstates; code++) {
    const uint32_t succ = mDeterministicSuccessorsShifted[code];
    if (succ != NO_TRANSITION) {
      if (result == UINT32_MAX) {
        result = succ;
      } else if (result != succ) {
        return UINT32_MAX;
      }
    }      
  }
  return result;
}

bool TransitionRecord::
markTransitionTaken(const uint32_t* tuple)
{
  uint32_t index = mAutomaton->getAutomatonIndex();
  uint32_t code = tuple[index];
  uint32_t flags = mFlags[code];
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
    uint32_t numstates = mAutomaton->getNumberOfStates();
    int shift = mAutomaton->getShift();
    bool keepnd = false;
    bool onlyself = true;
    int newweight = 0;
    for (uint32_t source = 0; source < numstates; source++) {
      if (mFlags[source] & FLAG_TAKEN) {
        uint32_t succ = mDeterministicSuccessorsShifted[source];
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
  uint32_t numstates = mAutomaton->getNumberOfStates();
  int shift = mAutomaton->getShift();
  bool keepnd = false;
  bool renorm = false;
  int newweight = 0;
  for (uint32_t source = 0; source < numstates; source++) {
    uint32_t shiftedsource = source << shift;
    uint32_t succ = mDeterministicSuccessorsShifted[source];
    if (succ == shiftedsource) {
      mDeterministicSuccessorsShifted[source] = NO_TRANSITION;
      renorm = true;
    } else if (succ == MULTIPLE_TRANSITIONS) {
      uint32_t* ndlist = mNondeterministicSuccessorsShifted[source];
      uint32_t ndcount = mNumNondeterministicSuccessors[source];
      uint32_t writeindex = UINT32_MAX;
      for (uint32_t readindex = 0; readindex < ndcount; readindex++) {
        succ = ndlist[readindex];
        if (succ == shiftedsource) {
          if (writeindex == UINT32_MAX) {
            writeindex = readindex;
          }
        } else if (writeindex != UINT32_MAX) {
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
      case UINT32_MAX:
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
storeNondeterministicTarget(const uint32_t* sourcetuple,
                            const uint32_t* targettuple,
                            const jni::MapGlue& statemap)
  const
{
  const uint32_t index = mAutomaton->getAutomatonIndex();
  const uint32_t source = sourcetuple[index];
  if (mFlags[source] & FLAG_NONDET) {
    const uint32_t target = targettuple[index];
    const jni::StateGlue& state = mAutomaton->getJavaState(target);
    const jni::AutomatonGlue& aut = mAutomaton->getJavaAutomaton();
    statemap.put(&aut, &state);
  }
}


//############################################################################
//# TransitionRecord: Auxiliary Methods

void TransitionRecord::
setupNondeterministicBuffers()
{
  if (mNondeterministicBuffer == 0) {
    const uint32_t numstates = mAutomaton->getNumberOfStates();
    uint32_t buffersize = 0;
    uint32_t state;
    for (state = 0; state < numstates; state++) {
      if (mDeterministicSuccessorsShifted[state] == MULTIPLE_TRANSITIONS) {
        buffersize += mNumNondeterministicSuccessors[state];
      }
    }
    mNondeterministicBuffer = new uint32_t[buffersize];
    mNondeterministicSuccessorsShifted = new uint32_t*[numstates];
    uint32_t next = 0;
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
//# class FastEligibilityTestRecord
//############################################################################

//############################################################################
//# FastEligibilityTestRecord : Simple Access

void FastEligibilityTestRecord::
setup(const TransitionRecord* trans, uint32_t event)
{
  const AutomatonRecord* aut = trans->getAutomaton();
  mAutomatonIndex = aut->getAutomatonIndex();
  mEventIndex = event;
  mDeterministicSuccessorsShifted = trans->mDeterministicSuccessorsShifted;
}


//############################################################################
//# FastEligibilityTestRecord : Comparing & Hashing

bool FastEligibilityTestRecord::
equals(const FastEligibilityTestRecord& record)
  const
{
  return mAutomatonIndex == record.mAutomatonIndex &&
         mEventIndex == record.mEventIndex;
}

uint64_t FastEligibilityTestRecord::
hash()
  const
{
  uint64_t key = (uint64_t) mAutomatonIndex | (uint64_t) mEventIndex << 32;
  return hashInt(key);
}



//############################################################################
//# class NondeterministicTransitionIterator
//############################################################################

//############################################################################
//# NondeterministicTransitionIterator: Constructors & Destructors

NondeterministicTransitionIterator::
NondeterministicTransitionIterator() :
  mAutomatonRecord(0), mTransitionRecord(0), mSource(0), mIndex(UINT32_MAX)
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
advanceInit(uint32_t* tuple)
{
  const int w = mAutomatonRecord->getWordIndex();
  tuple[w] &= ~mAutomatonRecord->getBitMask();
  uint32_t next = mIndex + 1;
  if (next == mAutomatonRecord->getEndOfInitialStates1()) {
    next = mAutomatonRecord->getFirstInitialState2();
  }
  bool result;
  if (next < mAutomatonRecord->getEndOfInitialStates2()) {
    mIndex = next;
    result = false;
  } else {
    mIndex = mAutomatonRecord->getFirstInitialState1();
    result = true;  // indicates end of iteration
  }
  tuple[w] |= mIndex << mAutomatonRecord->getShift();
  return result;
}


//############################################################################
//# NondeterministicTransitionIterator: Transition Iteration

uint32_t NondeterministicTransitionIterator::
setup(const TransitionRecord* trans, uint32_t source)
{
  mAutomatonRecord = trans->getAutomaton();
  mTransitionRecord = trans;
  mSource = source;
  return reset();
}

bool NondeterministicTransitionIterator::
advance(uint32_t* tuple)
{
  const AutomatonRecord* aut = mAutomatonRecord;
  const int w = aut->getWordIndex();
  tuple[w] &= ~aut->getBitMask();
  uint32_t succ = next();
  if (succ == TransitionRecord::NO_TRANSITION) {
    tuple[w] |= reset();
    return true;
  } else {
    tuple[w] |= succ;
    return false;
  }
}

uint32_t NondeterministicTransitionIterator::
reset()
{
  mIndex =
    mTransitionRecord->getNumberOfNondeterministicSuccessors(mSource) - 1;
  return current();
}

uint32_t NondeterministicTransitionIterator::
next()
{
  if (mIndex > 0) {
    mIndex--;
    return current();
  } else {
    return TransitionRecord::NO_TRANSITION;
  }
}

uint32_t NondeterministicTransitionIterator::
current()
{
  return
    mTransitionRecord->getNondeterministicSuccessorShifted(mSource, mIndex);
}


}  /* namespace waters */
