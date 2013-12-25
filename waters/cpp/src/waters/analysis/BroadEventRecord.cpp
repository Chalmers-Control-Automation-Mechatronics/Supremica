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

#ifdef DEBUG
#include "jni/cache/JavaString.h"
#endif /* DEBUG */

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
    mIsDisabledInSpec(false),
    mNumNonSelfloopingRecords(0),
    mNumNondeterministicRecords(0),
    mNumberOfWords(numwords),
    mNumberOfUpdates(0),
    mUsedSearchRecords(0),
    mUnusedSearchRecords(0),
    mNotTakenSearchRecords(0),
    mNonSelfloopingRecord(0),
    mForwardRecord(0)
{
  mUpdateRecords = new TransitionUpdateRecord*[numwords];
  for (int w = 0; w < numwords; w++) {
    mUpdateRecords[w] = 0;
  }
}

BroadEventRecord::
BroadEventRecord(const BroadEventRecord& fwd)
  : EventRecord(fwd),
    mIsGloballyDisabled(fwd.mIsGloballyDisabled),
    mIsDisabledInSpec(fwd.mIsDisabledInSpec),
    mNumNonSelfloopingRecords(0),
    mNumNondeterministicRecords(0),
    mNumberOfWords(fwd.mNumberOfWords),
    mNumberOfUpdates(0),
    mUsedSearchRecords(0),
    mUnusedSearchRecords(0),
    mNotTakenSearchRecords(0),
    mNonSelfloopingRecord(0),
    mForwardRecord(&fwd)
{
  mUpdateRecords = new TransitionUpdateRecord*[mNumberOfWords];
  for (int w = 0; w < mNumberOfWords; w++) {
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
isSkippable(ExplorerMode mode)
  const
{
  if (mIsGloballyDisabled) {
    return true;
  } else if (mUsedSearchRecords == 0 && mUnusedSearchRecords == 0) {
    return true;
  } else if (mNumNonSelfloopingRecords) {
    return false;
  } else if (mode == EXPLORER_MODE_SAFETY) {
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
                           uint32_t source, uint32_t target)
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
                              uint32_t source, uint32_t target)
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
    if (!trans->isDeterministic()) {
      mNumNondeterministicRecords++;
    }
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
      mNonSelfloopingRecord = trans;
      mNumNonSelfloopingRecords++;
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
optimizeTransitionRecordsForSearch(ExplorerMode mode)
{
  bool safety = mode == EXPLORER_MODE_SAFETY;
  bool controllable;
  if (safety && !mIsDisabledInSpec) {
    setControllable(true);
    controllable = true;
  } else {
    controllable = isControllable();
  }
  if (mNumNonSelfloopingRecords == 1) {
    if (!safety || controllable) {
      const bool unlinked = mNonSelfloopingRecord->isAlwaysEnabled();
      const bool det = mNonSelfloopingRecord->isDeterministic();
      mNonSelfloopingRecord->removeSelfloops();
      if (unlinked && !mNonSelfloopingRecord->isAlwaysEnabled()) {
        relink(mNonSelfloopingRecord);
        const bool plant = mNonSelfloopingRecord->getAutomaton()->isPlant();
        mIsDisabledInSpec &= !plant;
      }
      if (!det && mNonSelfloopingRecord->isDeterministic()) {
        mNumNondeterministicRecords--;
      }
    }
  }
  const LinkedRecordAccessor<TransitionRecord>* accessor =
    TransitionRecord::getSearchAccessor(controllable);
  LinkedRecordList<TransitionRecord> list(accessor, mUsedSearchRecords);
  list.qsort();
  mUsedSearchRecords = list.getHead();
}

void BroadEventRecord::
setupNotTakenSearchRecords()
{
  TransitionRecord* list = 0;
  for (TransitionRecord* trans = mUsedSearchRecords;
       trans != 0;
       trans = trans->getNextInSearch()) {
    if (!trans->isOnlySelfloops()) {
      trans->setNextInNotTaken(list);
      list = trans;
    }
  }
  for (TransitionRecord* trans = mUnusedSearchRecords;
       trans != 0;
       trans = trans->getNextInSearch()) {
    if (!trans->isOnlySelfloops()) {
      trans->setNextInNotTaken(list);
      list = trans;
    }
  }
  mNotTakenSearchRecords = list;
}

void BroadEventRecord::
markTransitionsTaken(const uint32_t* tuple)
{
  TransitionRecord* prev = 0;
  TransitionRecord* trans = mNotTakenSearchRecords;
  while (trans != 0) {
    bool alltaken = trans->markTransitionTaken(tuple);
    if (!alltaken) {
      prev = trans;
      trans = trans->getNextInNotTaken();
    } else if (prev) {
      trans = trans->getNextInNotTaken();
      prev->setNextInNotTaken(trans);
    } else {
      mNotTakenSearchRecords = trans = trans->getNextInNotTaken();
    }
  }
}

int BroadEventRecord::
removeTransitionsNotTaken()
{
  int result = 0;
  for (TransitionRecord* trans = mNotTakenSearchRecords;
       trans != 0;
       trans = trans->getNextInNotTaken()) {
    bool det = trans->isDeterministic();
    result += trans->removeTransitionsNotTaken();
    if (trans->isAlwaysDisabled()) {
      mIsGloballyDisabled = true;
      return result;
    }
    if (!mIsDisabledInSpec && !trans->isAlwaysEnabled()) {
      const AutomatonRecord* aut = trans->getAutomaton();
      mIsDisabledInSpec = !aut->isPlant();
    }
    if (!det && trans->isDeterministic()) {
      mNumNondeterministicRecords--;
    }
  }
  return result;
}

BroadEventRecord* BroadEventRecord::
createReversedRecord()
  const
{
  if (mIsGloballyDisabled) {
    return 0;
  } else {
    BroadEventRecord* reversed = new BroadEventRecord(*this);
    reversed->mProbability = 1.0;
    reversed->addReversedList(mUsedSearchRecords);
    reversed->addReversedList(mUnusedSearchRecords);
    const LinkedRecordAccessor<TransitionRecord>* accessor =
      TransitionRecord::getTraceAccessor();
    LinkedRecordList<TransitionRecord> list(accessor,
                                            reversed->mUsedSearchRecords);
    list.qsort();
    reversed->mUsedSearchRecords = list.getHead();
    return reversed;
  }
}


//############################################################################
//# BroadEventRecord: Trace Computation

void BroadEventRecord::
storeNondeterministicTargets(const uint32_t* sourcetuple,
                             const uint32_t* targettuple,
                             const jni::MapGlue& map)
  const
{
  if (mForwardRecord == 0) {
    storeNondeterministicTargets
      (mUsedSearchRecords, sourcetuple, targettuple, map);
    storeNondeterministicTargets
      (mUnusedSearchRecords, sourcetuple, targettuple, map);
  } else {
    mForwardRecord->storeNondeterministicTargets(sourcetuple, targettuple, map);
  }
}


//############################################################################
//# BroadEventRecord: Auxiliary Methods

void BroadEventRecord::
relink(TransitionRecord* trans)
{
  TransitionRecord* newnext = trans->getNextInSearch();
  if (mUnusedSearchRecords == trans) {
    mUnusedSearchRecords = newnext;
  } else {
    TransitionRecord* prev = mUnusedSearchRecords;
    TransitionRecord* next = prev->getNextInSearch();
    while (next != trans) {
      prev = next;
      next = prev->getNextInSearch();
    }
    prev->setNextInSearch(newnext);
  }
  trans->setNextInSearch(mUsedSearchRecords);
  mUsedSearchRecords = trans;
}

void BroadEventRecord::
addReversedList(const TransitionRecord* trans)
{
  while (trans != 0) {
    if (trans->isOnlySelfloops()) {
      TransitionRecord* reversed = new TransitionRecord(*trans);
      enqueueSearchRecord(reversed);
    } else {
      const AutomatonRecord* aut = trans->getAutomaton();
      const uint32_t numstates = aut->getNumberOfStates();
      const int shift = aut->getShift();
      TransitionRecord* reversed = new TransitionRecord(aut, 0, trans);
      int maxpass = 1;
      for (int pass = 1; pass <= maxpass; pass++) {
        for (uint32_t source = 0; source < numstates; source++) {
          const uint32_t numsucc = trans->getNumberOfSuccessors(source);
          for (uint32_t offset = 0; offset < numsucc; offset++) {
            const uint32_t shiftedtarget =
              trans->getSuccessorShifted(source, offset);
            const uint32_t target = shiftedtarget >> shift;
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
      reversed->normalize();
      if (!reversed->isDeterministic()) {
        mNumNondeterministicRecords++;
      }
      enqueueSearchRecord(reversed);
      const int wordindex = aut->getWordIndex();
      TransitionUpdateRecord* update = createUpdateRecord(wordindex);
      update->addTransition(reversed);
    }
    trans = trans->getNextInSearch();
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

void BroadEventRecord::
storeNondeterministicTargets(TransitionRecord* trans,
                             const uint32_t* sourcetuple,
                             const uint32_t* targettuple,
                             const jni::MapGlue& map)
  const
{
  while (trans != 0) {
    trans->storeNondeterministicTarget(sourcetuple, targettuple, map);
    trans = trans->getNextInSearch();
  }
}


//############################################################################
//# BroadEventRecord: Debug Output

#ifdef DEBUG

void BroadEventRecord::
dumpTransitionRecords()
  const
{
  std::cerr << (const char*) (getName()) << " {" << std::endl;
  for (TransitionRecord* trans = getTransitionRecord();
       trans != 0;                                                      
       trans = trans->getNextInSearch()) {
    const AutomatonRecord* aut = trans->getAutomaton();
    std::cerr << "  " << (const char*) aut->getName()
              << " (" << aut->getAutomatonIndex() << ") [";
    uint32_t numstates = aut->getNumberOfStates();
    for (uint32_t state = 0; state < numstates; state++) {
      if (state > 0) {
        std::cerr << ',';
      }
      uint32_t shifted = trans->getDeterministicSuccessorShifted(state);
      switch (shifted) {
      case TransitionRecord::NO_TRANSITION:
        std::cerr << '-';
        break;
      case TransitionRecord::MULTIPLE_TRANSITIONS:
        std::cerr << '+';
        break;
      default:
        int displacement = aut->getShift();
        std::cerr << (shifted >> displacement);
        break;
      }
    }
    std::cerr << ']' << std::endl;
  }  
  std::cerr << "}" << std::endl;
}

#endif /* DEBUG */

}  /* namespace waters */
