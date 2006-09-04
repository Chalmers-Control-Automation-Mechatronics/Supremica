//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.cpp,v 1.6 2006-09-04 11:04:41 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/JavaString.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/EventRecord.h"
#include "waters/analysis/TransitionRecord.h"


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
EventRecord(jni::EventGlue event,
            bool controllable,
            jni::ClassCache* /* cache */)
  : mJavaEvent(event),
    mIsControllable(controllable),
    mIsGloballyDisabled(false),
    mTransitionRecords(0)
{
}

EventRecord::
~EventRecord()
{
  delete mTransitionRecords;
}


//############################################################################
//# EventRecord: Simple Access

bool EventRecord::
isSkippable()
  const
{
  return mIsGloballyDisabled || mTransitionRecords == 0;
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
    if (mTransitionRecords == 0 ||
        mTransitionRecords->getAutomaton() != aut) {
      mTransitionRecords = new TransitionRecord(aut, mTransitionRecords);
    }
    return mTransitionRecords->addTransition(source, target);
  }
}

void EventRecord::
normalize(const AutomatonRecord* aut)
{
  if (mTransitionRecords != 0 &&
      mTransitionRecords->getAutomaton() == aut) {
    mTransitionRecords->normalize();
    if (mTransitionRecords->isAllSelfloops()) {
      TransitionRecord* victim = mTransitionRecords;
      mTransitionRecords = mTransitionRecords->getNext();
      victim->setNext(0);
      delete victim;
    }
  } else {
    if (mIsControllable || aut->isPlant()) {
      delete mTransitionRecords;
      mTransitionRecords = 0;
      mIsGloballyDisabled = true;
    } else {
      mTransitionRecords = new TransitionRecord(aut, mTransitionRecords);
    }
  }
}

void EventRecord::
sortTransitionRecords()
{
  TransitionRecordList list(mTransitionRecords);
  list.qsort();
  mTransitionRecords = list.getHead();
}


}  /* namespace waters */
