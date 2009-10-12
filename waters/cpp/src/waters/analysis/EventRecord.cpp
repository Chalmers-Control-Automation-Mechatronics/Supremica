//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <jni.h>

#include "jni/cache/ClassGlue.h"
#include "jni/cache/JavaString.h"

#include "waters/analysis/EventRecord.h"


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
EventRecord(jni::EventGlue event, bool controllable)
  : mJavaEvent(event),
    mIsControllable(controllable)
{
}


//############################################################################
//# EventRecord: Simple Access

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
  return mJavaEvent.compareTo(&partner->mJavaEvent);
}

int EventRecord::
compare(const void* elem1, const void* elem2)
{
  const EventRecord* val1 = *((const EventRecord**) elem1);
  const EventRecord* val2 = *((const EventRecord**) elem2);
  return val1->compareTo(val2);
}


}  /* namespace waters */
