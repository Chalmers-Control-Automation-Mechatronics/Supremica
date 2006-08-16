//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.cpp,v 1.2 2006-08-16 02:56:42 robi Exp $
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
//# EventRecord: Constructors & Destructors

EventRecord::
EventRecord(jni::EventGlue event,
            bool controllable,
            jni::ClassCache* cache)
  : mJavaEvent(event),
    mIsControllable(controllable),
    mTransitionRecords(0)
{
}


}  /* namespace waters */
