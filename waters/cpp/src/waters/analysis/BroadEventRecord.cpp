//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadEventRecord
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>
#include <iostream>

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/analysis/TransitionUpdateRecord.h"


namespace waters {

//############################################################################
//# class BroadEventRecord
//############################################################################

//############################################################################
//# BroadEventRecord: Constructors & Destructors

BroadEventRecord::
BroadEventRecord(jni::EventGlue event, bool controllable, int numwords)
  : EventRecord(event, controllable),
    mIsGloballyDisabled(false),
    mIsOnlySelfloops(true),
    mIsDisabledInSpec(false),
    mIsDeterministic(true),
    mNumberOfWords(numwords),
    mNumberOfUpdates(0),
    mUsedSearchRecords(0),
    mUnusedSearchRecords(0)
{
  mUpdateRecords = new TransitionUpdateRecord*[numwords];
  for (int w = 0; w < numwords; w++) {
    mUpdateRecords[w] = 0;
  }
}

BroadEventRecord::
~BroadEventRecord()
{
  for (int w = 0; w < mNumberOfWords; w++) {
    delete mUpdateRecords[w];
  }
  delete [] mUpdateRecords;
  delete mUsedSearchRecords;
  delete mUnusedSearchRecords;
}


//############################################################################
//# BroadEventRecord: Simple Access

bool BroadEventRecord::
isSkippable(bool safety)
  const
{
  if (mIsGloballyDisabled) {
    return true;
  } else if (mUsedSearchRecords == 0 && mUnusedSearchRecords == 0) {
    return true;
  } else if (!mIsOnlySelfloops) {
    return false;
  } else if (safety) {
    return isControllable() ? true : !mIsDisabledInSpec;
  } else {
    return true;
  }
}


//############################################################################
//# BroadEventRecord: Comparing

int BroadEventRecord::
compareToForForwardSearch(const BroadEventRecord* partner)
  const
{
  const int cont1 = isControllable() ? 1 : 0;
  const int cont2 = partner->isControllable() ? 1 : 0;
  if (cont1 != cont2) {
    return cont1 - cont2;
  } else {
    return EventRecord::compareTo(partner);
  }
}

int BroadEventRecord::
compareToForBackwardSearch(const BroadEventRecord* partner)
  const
{
  const float prob1 = mProbability;
  const float prob2 = partner->mProbability;
  if (prob1 < prob2) {
    return 1;
  } else if (prob2 < prob1) {
    return -1;
  } else {
    return EventRecord::compareTo(partner);
  }
}

int BroadEventRecord::
compareForForwardSearch(const void* elem1, const void* elem2)
{
  const BroadEventRecord* val1 = *((const BroadEventRecord**) elem1);
  const BroadEventRecord* val2 = *((const BroadEventRecord**) elem2);
  return val1->compareToForForwardSearch(val2);
}

int BroadEventRecord::
compareForBackwardSearch(const void* elem1, const void* elem2)
{
  const BroadEventRecord* val1 = *((const BroadEventRecord**) elem1);
  const BroadEventRecord* val2 = *((const BroadEventRecord**) elem2);
  return val1->compareToForBackwardSearch(val2);
}


//############################################################################
//# BroadEventRecord: Set up

bool BroadEventRecord::
addDeterministicTransition(const AutomatonRecord* aut,
                           uint32 source, uint32 target)
{
  if (mIsGloballyDisabled) {
    return true;
  } else {
    if (mUsedSearchRecords == 0 ||
        mUsedSearchRecords->getAutomaton() != aut) {
      mUsedSearchRecords = new TransitionRecord(aut, mUsedSearchRecords);
    }
    return mUsedSearchRecords->addDeterministicTransition(source, target);
  }
}

void BroadEventRecord::
addNondeterministicTransition(const AutomatonRecord* aut,
                              uint32 source, uint32 target)
{
  if (mUsedSearchRecords != 0 && mUsedSearchRecords->getAutomaton() == aut) {
    mUsedSearchRecords->addNondeterministicTransition(source, target);
  }
}

void BroadEventRecord::
normalize(const AutomatonRecord* aut)
{
  TransitionRecord* trans = mUsedSearchRecords;
  if (trans != 0 && trans->getAutomaton() == aut) {
    trans->normalize();
    mIsDeterministic &= trans->isDeterministic();
    const bool unlinked = trans->isAlwaysEnabled();
    if (unlinked) {
      mUsedSearchRecords = trans->getNextInSearch();
    } else {
      mIsDisabledInSpec |= !aut->isPlant();
    }
    if (trans->isOnlySelfloops()) {
      if (unlinked) {
        trans->setNextInSearch(0);
        delete trans;
      }
    } else {
      const int wordindex = aut->getWordIndex();
      TransitionUpdateRecord* update = createUpdateRecord(wordindex);
      update->addTransition(trans);
      if (unlinked) {
        trans->setNextInSearch(mUnusedSearchRecords);
        mUnusedSearchRecords = trans;
      }
      mIsOnlySelfloops = false;
      mNumberOfUpdates++;
    }
  } else if (!mIsGloballyDisabled) {
    if (isControllable() || aut->isPlant()) {
      delete mUsedSearchRecords;
      delete mUnusedSearchRecords;
      mUsedSearchRecords = mUnusedSearchRecords = 0;
      mIsGloballyDisabled = true;
    } else {
      mUsedSearchRecords = new TransitionRecord(aut, mUsedSearchRecords);
      mIsDisabledInSpec = true;
    }
  }
}

TransitionUpdateRecord* BroadEventRecord::
createUpdateRecord(int wordindex)
{
  TransitionUpdateRecord* update = mUpdateRecords[wordindex];
  if (update == 0) {
    update = mUpdateRecords[wordindex] = new TransitionUpdateRecord();
  }
  return update;
}

void BroadEventRecord::
sortTransitionRecordsForSearch()
{
  const LinkedRecordAccessor<TransitionRecord>* accessor =
    TransitionRecord::getSearchAccessor();
  LinkedRecordList<TransitionRecord> list(accessor, mUsedSearchRecords);
  list.qsort();
  mUsedSearchRecords = list.getHead();
}

bool BroadEventRecord::
reverse()
{
  if (mIsGloballyDisabled) {
    mProbability = 0.0;
    return false;
  } else {
    TransitionRecord* used = mUsedSearchRecords;
    TransitionRecord* unused = mUnusedSearchRecords;
    clearSearchAndUpdateRecords();
    mIsDeterministic = true;
    mProbability = 1.0;
    addReversedList(used);
    addReversedList(unused);
    if (mIsGloballyDisabled) {
      return false;
    } else {
      const LinkedRecordAccessor<TransitionRecord>* accessor =
        TransitionRecord::getTraceAccessor();
      LinkedRecordList<TransitionRecord> list(accessor, mUsedSearchRecords);
      list.qsort();
      mUsedSearchRecords = list.getHead();
      return true;
    }
  }
}


//############################################################################
//# BroadEventRecord: Set up

void BroadEventRecord::
addReversedList(TransitionRecord* trans)
{
  if (mIsGloballyDisabled) {
    delete trans;
  } else {
    while (trans != 0) {
      TransitionRecord* next = trans->getNextInSearch();
      if (trans->isAlwaysDisabled()) {
        mIsGloballyDisabled = true;
        mProbability = 0.0;
        delete trans;
        delete mUsedSearchRecords;
        delete mUnusedSearchRecords;
        clearSearchAndUpdateRecords();
        return;
      } else if (trans->isOnlySelfloops()) {
        enqueueSearchRecord(trans);
      } else {
        const AutomatonRecord* aut = trans->getAutomaton();
        const uint32 numstates = aut->getNumberOfStates();
        const int shift = aut->getShift();
        TransitionRecord* reversed = new TransitionRecord(aut, 0);
        int maxpass = 1;
        for (int pass = 1; pass <= maxpass; pass++) {
          for (uint32 source = 0; source < numstates; source++) {
            const uint32 numsucc = trans->getNumberOfSuccessors(source);
            for (uint32 offset = 0; offset < numsucc; offset++) {
              const uint32 shiftedtarget =
                trans->getSuccessorShifted(source, offset);
              const uint32 target = shiftedtarget >> shift;
              if (pass == 1) {
                if (!reversed->addDeterministicTransition(target, source)) {
                  maxpass = 2;
                }
              } else {
                reversed->addNondeterministicTransition(target, source);
              }
            }
          }
        }
        trans->setNextInSearch(0);
        delete trans;
        reversed->normalize();
        mIsDeterministic &= reversed->isDeterministic();
        enqueueSearchRecord(reversed);
        const int wordindex = aut->getWordIndex();
        TransitionUpdateRecord* update = createUpdateRecord(wordindex);
        update->addTransition(reversed);
      }
      trans = next;
    }
  }
}

void BroadEventRecord::
enqueueSearchRecord(TransitionRecord* trans)
{
  if (trans->isAlwaysEnabled()) {
    trans->setNextInSearch(mUnusedSearchRecords);
    mUnusedSearchRecords = trans;
  } else {
    trans->setNextInSearch(mUsedSearchRecords);
    mUsedSearchRecords = trans;
  }
  mProbability *= trans->getProbability();
}

void BroadEventRecord::
clearSearchAndUpdateRecords()
{
  mUsedSearchRecords = mUnusedSearchRecords = 0;
  for (int w = 0; w < mNumberOfWords; w++) {
    delete mUpdateRecords[w];
    mUpdateRecords[w] = 0;
  }
}


}  /* namespace waters */
