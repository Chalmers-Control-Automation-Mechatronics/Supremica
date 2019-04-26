//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

uint64_t EventRecordHashAccessor::
hash(intptr_t key)
  const
{
  const jni::EventGlue* event = (const jni::EventGlue*) key;
  return event->hashCode();
}


bool EventRecordHashAccessor::
equals(intptr_t key1, intptr_t key2)
  const
{
  const jni::EventGlue* event1 = (const jni::EventGlue*) key1;
  const jni::EventGlue* event2 = (const jni::EventGlue*) key2;
  return event1->equals(event2);
}


intptr_t EventRecordHashAccessor::
getKey(intptr_t value)
  const
{
  const EventRecord* record = (const EventRecord*) value;
  return (intptr_t) &record->getJavaEvent();
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

EventRecord::
EventRecord(const EventRecord& record)
  : mJavaEvent(record.mJavaEvent),
    mIsControllable(record.mIsControllable)
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
