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
    mNumberOfWords(numwords),
    mSearchRecords(0),
    mTraceSearchRecords(0)
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
  delete mSearchRecords;
  delete mTraceSearchRecords;
}


//############################################################################
//# EventRecord: Simple Access

bool EventRecord::
isSkippable()
  const
{
  if (mIsGloballyDisabled) {
    return true;
  } else if (mSearchRecords == 0 && mTraceSearchRecords == 0) {
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
compareTo(const EventRecord* partner)
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
compare(const void* elem1, const void* elem2)
{
  const EventRecord* val1 = *((const EventRecord**) elem1);
  const EventRecord* val2 = *((const EventRecord**) elem2);
  return val1->compareTo(val2);
}


//############################################################################
//# Set up

bool EventRecord::
addTransition(const AutomatonRecord* aut,
              const StateRecord* source,
              const StateRecord* target)
{
  if (mIsGloballyDisabled) {
    return true;
  } else {
    if (mSearchRecords == 0 ||
        mSearchRecords->getAutomaton() != aut) {
      mSearchRecords = new TransitionRecord(aut, mSearchRecords);
    }
    return mSearchRecords->addTransition(source, target);
  }
}

void EventRecord::
normalize(const AutomatonRecord* aut)
{
  TransitionRecord* trans = mSearchRecords;
  if (trans != 0 && trans->getAutomaton() == aut) {
    trans->normalize();
    const bool unlinked = trans->isAlwaysEnabled();
    if (unlinked) {
      mSearchRecords = trans->getNextInSearch();
    } else {
      mIsDisabledInSpec |= !aut->isPlant();
    }
    if (trans->isOnlySelfloops()) {
      if (unlinked) {
        trans->setNextInSearch(0);
        delete trans;
      }
    } else {
      const AutomatonRecord* aut = trans->getAutomaton();
      const int wordindex = aut->getWordIndex();
      TransitionUpdateRecord* update = createUpdateRecord(wordindex);
      update->addTransition(trans);
      if (unlinked) {
        trans->setNextInSearch(mTraceSearchRecords);
        mTraceSearchRecords = trans;
      }
      mIsOnlySelfloops = false;
    }
  } else if (!mIsGloballyDisabled) {
    if (mIsControllable || aut->isPlant()) {
      delete mSearchRecords;
      delete mTraceSearchRecords;
      mSearchRecords = mTraceSearchRecords = 0;
      mIsGloballyDisabled = true;
    } else {
      mSearchRecords = new TransitionRecord(aut, mSearchRecords);
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
  TransitionRecordList list(mSearchRecords);
  list.qsort(TransitionRecord::compareForSearch);
  mSearchRecords = list.getHead();
}

void EventRecord::
sortTransitionRecordsForTrace()
{
  if (mSearchRecords == 0) {
    mSearchRecords = mTraceSearchRecords;
  } else {
    TransitionRecordList list(mSearchRecords);
    list.append(mTraceSearchRecords);
  }
  mTraceSearchRecords = 0;
}


}  /* namespace waters */
