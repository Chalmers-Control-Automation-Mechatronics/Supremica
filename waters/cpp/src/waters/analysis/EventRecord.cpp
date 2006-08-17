//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.cpp,v 1.3 2006-08-17 10:15:12 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/glue/EventGlue.h"

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
EventRecord(jni::EventGlue event,
            bool controllable,
            jni::ClassCache* /* cache */)
  : mJavaEvent(event),
    mIsControllable(controllable),
    mTransitionRecords(0)
{
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
