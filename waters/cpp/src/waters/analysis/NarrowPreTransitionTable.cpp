//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowPreTransitionTable
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/TreeSetGlue.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/NarrowEventRecord.h"
#include "waters/analysis/NarrowPreTransitionTable.h"


namespace waters {


//############################################################################
//# class NarrowTransitionRecordHashAccessor
//############################################################################

//############################################################################
//# NarrowTransitionRecordHashAccessor: Hash Methods

uint32 NarrowTransitionRecordHashAccessor::
hash(const void* key)
  const
{
  const NarrowTransitionRecord* trans = (const NarrowTransitionRecord*) key;
  const jni::EventGlue& jevent = trans->mEvent->getJavaEvent();
  uint32 code[] = {trans->mState, (uint32) jevent.hashCode()};
  return hashIntArray(code, 2);
}


bool NarrowTransitionRecordHashAccessor::
equals(const void* key1, const void* key2)
  const
{
  const NarrowTransitionRecord* trans1 = (const NarrowTransitionRecord*) key1;
  const NarrowTransitionRecord* trans2 = (const NarrowTransitionRecord*) key2;
  return trans1->mState == trans2->mState && trans1->mEvent == trans2->mEvent;
}


//############################################################################
//# class NarrowTransitionRecordListAccessor
//############################################################################

//############################################################################
//# NarrowTransitionRecordListAccessor: List Accessor Methods

NarrowTransitionRecord* NarrowTransitionRecordListAccessor::
getNext(const NarrowTransitionRecord* record)
  const
{
  return record->getNext();
}


void NarrowTransitionRecordListAccessor::
setNext(NarrowTransitionRecord* record, NarrowTransitionRecord* next)
  const
{
  record->setNext(next);
}


int NarrowTransitionRecordListAccessor::
compare(const NarrowTransitionRecord* record1,
        const NarrowTransitionRecord* record2)
  const
{
  return record1->compareTo(record2);
}


//############################################################################
//# class NarrowTransitionRecord
//############################################################################

//############################################################################
//# NarrowTransitionRecord: Simple Access

uint32 NarrowTransitionRecord::
getEventCode()
  const
{
  return mEvent->getEventCode();
}


//##########################################################################
//# NarrowTransitionRecord: Comparing and Hashing

int NarrowTransitionRecord::
compareTo(const NarrowTransitionRecord* record)
  const
{
  int ecode1 = (int) getEventCode();
  int ecode2 = (int) record->getEventCode();
  return ecode1 - ecode2;
}


//############################################################################
//# NarrowTransitionRecord: Building the Transition Table

void NarrowTransitionRecord::
putSuccessor(uint32* buffer, uint32 code, uint32 endtag)
{
  // std::cerr << "putsucc:" << mNumSuccessors << ":"
  //           << mBufferPos << "->" << code << std::endl;
  if (--mNumSuccessors == 0) {
    buffer[mBufferPos] = code | endtag;
  } else {
    buffer[mBufferPos++] = code;
  }
}

//############################################################################
//# NarrowTransitionRecord: Class Variables

const NarrowTransitionRecordHashAccessor
  NarrowTransitionRecord::theHashAccessor;
const NarrowTransitionRecordListAccessor
  NarrowTransitionRecord::theListAccessor;


//############################################################################
//# class NarrowStateRecord
//############################################################################

//############################################################################
//# NarrowStateRecord: Simple Access

uint32 NarrowStateRecord::
getNumberOfNondeterministicTransitions()
  const
{
  uint32 result = 0;
  for (const NarrowTransitionRecord* current = mTransitionRecords;
       current != 0;
       current = current->getNext()) {
    const uint32 numsucc = current->getNumberOfSuccessors();
    if (numsucc > 1) {
      result += numsucc;
    }
  }
  return result;
}


void NarrowStateRecord::
addTransition(NarrowTransitionRecord* trans)
{
  trans->setNext(mTransitionRecords);
  mTransitionRecords = trans;
  mNumEvents++;
}


void NarrowStateRecord::
removeSkippable(const NarrowPreTransitionTable* pre)
{
  NarrowTransitionRecord** ref = &mTransitionRecords;
  NarrowTransitionRecord* current = *ref;
  while (current != 0) {
    const NarrowEventRecord* event = current->getEvent();
    if (event->isSkippable() || pre->isLocallySelflooped(event)) {
      mNumEvents--;
      *ref = current->mNext;
      current->mNext = 0;
      delete current;
    } else {
      ref = &current->mNext;
    }
    current = *ref;
  }
}


void NarrowStateRecord::
sort()
{
  const LinkedRecordAccessor<NarrowTransitionRecord>* accessor =
    NarrowTransitionRecord::getListAccessor();
  LinkedRecordList<NarrowTransitionRecord> list(accessor, mTransitionRecords);
  list.qsort();
  mTransitionRecords = list.getHead();
}


//############################################################################
//# class NarrowPreTransitionTable
//############################################################################

//############################################################################
//# NarrowPreTransitionTable: Constructors & Destructors

NarrowPreTransitionTable::
NarrowPreTransitionTable(AutomatonRecord* aut,
			 jni::ClassCache* cache,
			 const HashTable<const jni::EventGlue*,
			                 NarrowEventRecord*>& eventmap)
  : mAutomaton(aut),
    mNarrowStates(0),
    mStateMap(0),
    mSelfloopMap(0),
    mUniqueTransitions(0)
{
  const uint32 numstates = mAutomaton->getNumberOfStates();
  mNarrowStates =
    (NarrowStateRecord*) new char[numstates * sizeof(NarrowStateRecord)];
  for (uint32 code = 0; code < numstates; code++) {
    new (&mNarrowStates[code]) NarrowStateRecord(code);
  }

  const jni::AutomatonGlue& autglue = mAutomaton->getJavaAutomaton();
  const jni::CollectionGlue events = autglue.getEventsGlue(cache);
  const jni::IteratorGlue eventiter1 = events.iteratorGlue(cache);
  while (eventiter1.hasNext()) {
    jobject javaobject = eventiter1.next();
    jni::EventGlue event(javaobject, cache);
    NarrowEventRecord* eventrecord = eventmap.get(&event);
    if (eventrecord != 0) {
      eventrecord->resetLocalTransitions();
    }
  }

  const jni::CollectionGlue transitions = autglue.getTransitionsGlue(cache);
  mUniqueTransitions = new jni::TreeSetGlue(&transitions, cache);
  const int numtrans = mUniqueTransitions->size();
  const HashAccessor* taccessor = NarrowTransitionRecord::getHashAccessor();
  HashTable<NarrowTransitionRecord*,NarrowTransitionRecord*>
    transmap(taccessor, numtrans);

  mStateMap = mAutomaton->createStateMap();
  NarrowTransitionRecord* newtrans = 0;
  const jni::IteratorGlue iter = mUniqueTransitions->iteratorGlue(cache);
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::TransitionGlue trans(javaobject, cache);
    const jni::EventGlue& event = trans.getEventGlue(cache);
    NarrowEventRecord* eventrecord = eventmap.get(&event);
    if (eventrecord->isGloballyDisabled()) {
      continue;
    }
    const jni::StateGlue& source = trans.getSourceGlue(cache);
    const uint32 sourcecode = mStateMap->get(&source);
    const jni::StateGlue& target = trans.getTargetGlue(cache);
    const uint32 targetcode = mStateMap->get(&target);
    eventrecord->countLocalTransition(sourcecode == targetcode);
    if (newtrans == 0) {
      newtrans = new NarrowTransitionRecord(sourcecode, eventrecord);
    } else {
      newtrans->init(sourcecode, eventrecord);
    }
    NarrowTransitionRecord* oldtrans = transmap.add(newtrans);
    if (oldtrans == newtrans) {
      mNarrowStates[sourcecode].addTransition(newtrans);
      newtrans = 0;
    } else {
      oldtrans->addSuccessor();
    }
  }

  const int numevents = events.size();
  const HashAccessor* eaccessor = EventRecord::getHashAccessor();
  mSelfloopMap = new HashTable<const jni::EventGlue*,NarrowEventRecord*>
    (eaccessor, numevents);
  const bool isplant = mAutomaton->isPlant();
  const jni::IteratorGlue eventiter2 = events.iteratorGlue(cache);
  while (eventiter2.hasNext()) {
    jobject javaobject = eventiter2.next();
    jni::EventGlue event(javaobject, cache);
    NarrowEventRecord* eventrecord = eventmap.get(&event);
    if (eventrecord != 0) {
      eventrecord->mergeLocalToGlobal(isplant, numstates);
      if (eventrecord->isLocallySelflooped(numstates)) {
	mSelfloopMap->add(eventrecord);
      }
    }
  }
}


NarrowPreTransitionTable::
~NarrowPreTransitionTable()
{
  const uint32 numstates = mAutomaton->getNumberOfStates();
  for (uint32 code = 0; code < numstates; code++) {
    mNarrowStates[code].~NarrowStateRecord();
  }
  delete (const char*) mNarrowStates;
  mAutomaton->deleteStateMap(mStateMap);
  delete mSelfloopMap;
  delete mUniqueTransitions;
}


//############################################################################
//# NarrowPreTransitionTable: Simple Access

bool NarrowPreTransitionTable::
isLocallySelflooped(const NarrowEventRecord* event)
  const
{
  const jni::EventGlue& jevent = event->getJavaEvent();
  return mSelfloopMap->get(&jevent) != 0;
}


}  /* namespace waters */
