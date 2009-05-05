//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.cpp,v 1.12 2006-12-01 03:26:36 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/JavaString.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/EventRecord.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/analysis/TransitionUpdateRecord.h"


namespace waters {

//############################################################################
//# class EventRecordHashAccessor
//############################################################################

//############################################################################
//# EventRecordHashAccessor: Hash Methods

uint32 EventRecordHashAccessor::
hash(const void* key)
  const
{
  const jni::EventGlue* event = (const jni::EventGlue*) key;
  return (uint32) event->hashCode();
}


bool EventRecordHashAccessor::
equals(const void* key1, const void* key2)
  const
{
  const jni::EventGlue* event1 = (const jni::EventGlue*) key1;
  const jni::EventGlue* event2 = (const jni::EventGlue*) key2;
  return event1->equals(event2);
}


const void* EventRecordHashAccessor::
getKey(const void* value)
  const
{
  const EventRecord* record = (const EventRecord*) value;
  return &record->getJavaEvent();
}


//############################################################################
//# class EventRecord
//############################################################################

//############################################################################
//# EventRecord: Class Variables

const EventRecordHashAccessor EventRecord::theHashAccessor;


//############################################################################
//# EventRecord: Constructors & Destructors

EventRecord::
EventRecord(jni::EventGlue event, bool controllable, int numwords)
  : mJavaEvent(event),
    mIsControllable(controllable),
    mIsGloballyDisabled(false),
    mIsOnlySelfloops(true),
    mIsDisabledInSpec(false),
    mIsDeterministic(true),
    mNumberOfWords(numwords),
    mUsedSearchRecords(0),
    mUnusedSearchRecords(0)
{
  mUpdateRecords = new TransitionUpdateRecord*[numwords];
  for (int w = 0; w < numwords; w++) {
    mUpdateRecords[w] = 0;
  }
}

EventRecord::
~EventRecord()
{
  for (int w = 0; w < mNumberOfWords; w++) {
    delete mUpdateRecords[w];
  }
  delete [] mUpdateRecords;
  delete mUsedSearchRecords;
  delete mUnusedSearchRecords;
}


//############################################################################
//# EventRecord: Simple Access

bool EventRecord::
isSkippable()
  const
{
  if (mIsGloballyDisabled) {
    return true;
  } else if (mUsedSearchRecords == 0 && mUnusedSearchRecords == 0) {
    return true;
  } else if (mIsOnlySelfloops) {
    return mIsControllable ? true : !mIsDisabledInSpec;
  } else {
    return false;
  }
}

jni::JavaString EventRecord::
getName()
  const
{
  const jni::ClassGlue* cls = mJavaEvent.getClass();
  JNIEnv* env = cls->getEnvironment();
  jstring jname = mJavaEvent.getName();
  return jni::JavaString(env, jname);
}


//############################################################################
//# EventRecord: Comparing

int EventRecord::
compareToForForwardSearch(const EventRecord* partner)
  const
{
  const int cont1 = mIsControllable ? 1 : 0;
  const int cont2 = partner->mIsControllable ? 1 : 0;
  if (cont1 != cont2) {
    return cont1 - cont2;
  } else {
    return mJavaEvent.compareTo(&partner->mJavaEvent);
  }
}

int EventRecord::
compareToForBackwardSearch(const EventRecord* partner)
  const
{
  const float prob1 = mProbability;
  const float prob2 = partner->mProbability;
  if (prob1 < prob2) {
    return 1;
  } else if (prob2 < prob1) {
    return -1;
  } else {
    return mJavaEvent.compareTo(&partner->mJavaEvent);
  }
}

int EventRecord::
compareForForwardSearch(const void* elem1, const void* elem2)
{
  const EventRecord* val1 = *((const EventRecord**) elem1);
  const EventRecord* val2 = *((const EventRecord**) elem2);
  return val1->compareToForForwardSearch(val2);
}

int EventRecord::
compareForBackwardSearch(const void* elem1, const void* elem2)
{
  const EventRecord* val1 = *((const EventRecord**) elem1);
  const EventRecord* val2 = *((const EventRecord**) elem2);
  return val1->compareToForBackwardSearch(val2);
}


//############################################################################
//# EventRecord: Set up

bool EventRecord::
addDeterministicTransition(const AutomatonRecord* aut,
                           const StateRecord* source,
                           const StateRecord* target)
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

void EventRecord::
addNondeterministicTransition(const AutomatonRecord* aut,
                              const StateRecord* source,
                              const StateRecord* target)
{
  if (mUsedSearchRecords != 0 && mUsedSearchRecords->getAutomaton() == aut) {
    mUsedSearchRecords->addNondeterministicTransition(source, target);
  }
}

void EventRecord::
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
    }
  } else if (!mIsGloballyDisabled) {
    if (mIsControllable || aut->isPlant()) {
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

TransitionUpdateRecord* EventRecord::
createUpdateRecord(int wordindex)
{
  TransitionUpdateRecord* update = mUpdateRecords[wordindex];
  if (update == 0) {
    update = mUpdateRecords[wordindex] = new TransitionUpdateRecord();
  }
  return update;
}

void EventRecord::
sortTransitionRecordsForSearch()
{
  TransitionRecordList list(mUsedSearchRecords);
  list.qsort(TransitionRecord::compareForSearch);
  mUsedSearchRecords = list.getHead();
}

bool EventRecord::
reverse()
{
  if (mIsGloballyDisabled) {
    mProbability = 0.0;
    return false;
  } else {
    TransitionRecord* used = mUsedSearchRecords;
    TransitionRecord* unused = mUnusedSearchRecords;
    clearSearchAndUpdateRecords();
    mIsDeterministic = 1;
    mProbability = 1.0;
    addReversedList(used);
    addReversedList(unused);
    if (mIsGloballyDisabled) {
      return false;
    } else {
      TransitionRecordList list(mUsedSearchRecords);
      list.qsort(TransitionRecord::compareForTrace);
      mUsedSearchRecords = list.getHead();
      return true;
    }
  }
}


//############################################################################
//# EventRecord: Set up

void EventRecord::
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

void EventRecord::
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

void EventRecord::
clearSearchAndUpdateRecords()
{
  mUsedSearchRecords = mUnusedSearchRecords = 0;
  for (int w = 0; w < mNumberOfWords; w++) {
    delete mUpdateRecords[w];
    mUpdateRecords[w] = 0;
  }
}


}  /* namespace waters */
