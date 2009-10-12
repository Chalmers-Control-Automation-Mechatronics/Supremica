//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowTransitionTable
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
#include "waters/analysis/NarrowTransitionTable.h"
#include "waters/base/LinkedRecordList.h"


namespace waters {

class NarrowTransitionRecord;  // forward


//###########################################################################
//# Class NarrowTransitionRecordHashAccessor (local)
//###########################################################################

class NarrowTransitionRecordHashAccessor : public PtrHashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowTransitionRecordHashAccessor() {};
  friend class NarrowTransitionRecord;

public:
  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const {return value;}
};


//############################################################################
//# Class NarrowTransitionRecordListAccessor (local)
//############################################################################

class NarrowTransitionRecordListAccessor :
  public LinkedRecordAccessor<NarrowTransitionRecord>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  NarrowTransitionRecordListAccessor() {}

  //##########################################################################
  //# Override for LinkedRecordAccessor
  virtual NarrowTransitionRecord* getNext(const NarrowTransitionRecord* record)
    const;
  virtual void setNext(NarrowTransitionRecord* record,
                       NarrowTransitionRecord* next) const;
  virtual int compare(const NarrowTransitionRecord* record1,
		      const NarrowTransitionRecord* record2) const;
};


//############################################################################
//# class NarrowTransitionRecord (local)
//############################################################################

class NarrowTransitionRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowTransitionRecord(uint32 state = UNDEF_UINT32,
                                  uint32 event = UNDEF_UINT32) :
    mState(state), mEvent(event), mNumSuccessors(1), mNext(0) {}
  ~NarrowTransitionRecord() {delete mNext;}
  void init(uint32 state, uint32 event) {mState = state; mEvent = event;}

  //##########################################################################
  //# Simple Access
  uint32 getEvent() const {return mEvent;}
  bool isDeterministic() const {return mNumSuccessors <= 1;}
  uint32 getNumberOfSuccessors() const {return mNumSuccessors;}
  NarrowTransitionRecord* getNext() const {return mNext;}
  void addSuccessor() {mNumSuccessors++;}
  void setNext(NarrowTransitionRecord* next) {mNext = next;}
  void setBufferPos(uint32 pos) {mBufferPos = pos;}

  //##########################################################################
  //# Building the Transition Table
  void putSuccessor(uint32* buffer, uint32 code);

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const NarrowTransitionRecord* record) const
    {return (int) mEvent - (int) record->mEvent;}
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}
  static const LinkedRecordAccessor<NarrowTransitionRecord>* getListAccessor()
    {return &theListAccessor;}

private:
  //##########################################################################
  //# Data Members
  uint32 mState;
  uint32 mEvent;
  uint32 mNumSuccessors;
  NarrowTransitionRecord* mNext;
  uint32 mBufferPos;

  //##########################################################################
  //# Class Variables
  static const NarrowTransitionRecordHashAccessor theHashAccessor;
  friend class NarrowTransitionRecordHashAccessor;
  static const NarrowTransitionRecordListAccessor theListAccessor;
  friend class NarrowTransitionRecordListAccessor;
};


//############################################################################
//# NarrowTransitionRecord: Building the Transition Table

void NarrowTransitionRecord::
putSuccessor(uint32* buffer, uint32 code)
{
  if (--mNumSuccessors == 0) {
    buffer[mBufferPos] = code | NarrowTransitionTable::TAG_END_OF_LIST;
  } else {
    buffer[mBufferPos++] = code;
  }
}


//############################################################################
//# NarrowTransitionRecordHashAccessor: Hash Methods

uint32 NarrowTransitionRecordHashAccessor::
hash(const void* key)
  const
{
  const NarrowTransitionRecord* trans = (const NarrowTransitionRecord*) key;
  return hashIntArray(&trans->mState, 2);
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
//# NarrowTransitionRecord: Class Variables

const NarrowTransitionRecordHashAccessor
  NarrowTransitionRecord::theHashAccessor;
const NarrowTransitionRecordListAccessor
  NarrowTransitionRecord::theListAccessor;



//############################################################################
//# class NarrowStateRecord (local)
//############################################################################

class NarrowStateRecord // : public StateRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowStateRecord
      (const jni::StateGlue& state, uint32 code, jni::ClassCache* cache) :
    mNumEvents(0),
    mTransitionRecords(0)
  {}
  ~NarrowStateRecord() {delete mTransitionRecords;}

  //##########################################################################
  //# Simple Access
  uint32 getNumberOfEnabledEvents() const {return mNumEvents;}
  NarrowTransitionRecord* getTransitions() const {return mTransitionRecords;}
  void addTransition(NarrowTransitionRecord* trans);
  void sort();

private:
  //##########################################################################
  //# Data Members
  uint32 mNumEvents;
  NarrowTransitionRecord* mTransitionRecords;
};


//############################################################################
//# NarrowStateRecord: Simple Access

void NarrowStateRecord::
addTransition(NarrowTransitionRecord* trans)
{
  trans->setNext(mTransitionRecords);
  mTransitionRecords = trans;
  mNumEvents++;
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
//# class NarrowTransitionTable
//############################################################################

//############################################################################
//# NarrowTransitionTable: Constructors & Destructors

NarrowTransitionTable::
NarrowTransitionTable(AutomatonRecord* aut,
                      jni::ClassCache* cache,
                      const HashTable<const jni::EventGlue*,
                                      NarrowEventRecord*>& eventmap)
  : mAutomaton(aut),
    mStateTable(0),
    mBuffers(0)
{
  const uint32 numstates = mAutomaton->getNumberOfStates();
  NarrowStateRecord* narrowstates =
    (NarrowStateRecord*) new char[numstates * sizeof(NarrowStateRecord)];
  for (uint32 code = 0; code < numstates; code++) {
    const jni::StateGlue& state = mAutomaton->getJavaState(code);
    new (&narrowstates[code]) NarrowStateRecord(state, code, cache);
  }

  const jni::AutomatonGlue& autglue = mAutomaton->getJavaAutomaton();
  const jni::CollectionGlue events = autglue.getEventsGlue(cache);
  const jni::IteratorGlue eventiter1 = events.iteratorGlue(cache);
  while (eventiter1.hasNext()) {
    jobject javaobject = eventiter1.next();
    jni::EventGlue event(javaobject, cache);
    NarrowEventRecord* eventrecord = eventmap.get(&event);
    eventrecord->resetLocalTransitions();
  }

  const jni::CollectionGlue transitions = autglue.getTransitionsGlue(cache);
  const jni::TreeSetGlue uniqtrans(&transitions, cache);
  const int numtrans = uniqtrans.size();
  const HashAccessor* accessor = NarrowTransitionRecord::getHashAccessor();
  HashTable<NarrowTransitionRecord*,NarrowTransitionRecord*>
    narrowtransmap(accessor, numtrans);

  HashTable<const jni::StateGlue*,uint32>* statemap =
    mAutomaton->createStateMap();
  int transcount = 0;
  int ndcount = 0;
  NarrowTransitionRecord* newtrans = 0;
  const jni::IteratorGlue iter1 = uniqtrans.iteratorGlue(cache);
  while (iter1.hasNext()) {
    jobject javaobject = iter1.next();
    jni::TransitionGlue trans(javaobject, cache);
    const jni::EventGlue& event = trans.getEventGlue(cache);
    NarrowEventRecord* eventrecord = eventmap.get(&event);
    if (eventrecord->isGloballyDisabled()) {
      continue;
    }
    const uint32 eventcode = eventrecord->getEventCode();
    const jni::StateGlue& source = trans.getSourceGlue(cache);
    const uint32 sourcecode = statemap->get(&source);
    const jni::StateGlue& target = trans.getTargetGlue(cache);
    const uint32 targetcode = statemap->get(&target);
    eventrecord->countLocalTransition(sourcecode == targetcode);
    if (newtrans == 0) {
      newtrans = new NarrowTransitionRecord(sourcecode, eventcode);
    } else {
      newtrans->init(sourcecode, eventcode);
    }
    NarrowTransitionRecord* oldtrans = narrowtransmap.add(newtrans);
    if (oldtrans == newtrans) {
      narrowstates[sourcecode].addTransition(newtrans);
      newtrans = 0;
      transcount++;
    } else if (oldtrans->isDeterministic()) {
      oldtrans->addSuccessor();
      ndcount += 2;
    } else {
      oldtrans->addSuccessor();
      ndcount++;
    }
  }

  const bool isplant = aut->isPlant();
  const jni::IteratorGlue eventiter2 = events.iteratorGlue(cache);
  while (eventiter2.hasNext()) {
    jobject javaobject = eventiter2.next();
    jni::EventGlue event(javaobject, cache);
    NarrowEventRecord* eventrecord = eventmap.get(&event);
    eventrecord->mergeLocalToGlobal(isplant, numstates);
  }

  mStateTable = new uint32[numstates];
  mBuffers = new uint32[2 * transcount + numstates + ndcount];
  uint32 next = 0;
  for (uint32 code = 0; code < numstates; code++) {
    NarrowStateRecord& narrowstate = narrowstates[code];
    narrowstate.sort();
    mStateTable[code] = next;
    uint32 pos = next;
    next += narrowstate.getNumberOfEnabledEvents() + 1;
    for (NarrowTransitionRecord* narrowtrans = narrowstate.getTransitions();
         narrowtrans != 0;
         narrowtrans = narrowtrans->getNext()) {
      mBuffers[pos++] = narrowtrans->getEvent();
      if (narrowtrans->isDeterministic()) {
        narrowtrans->setBufferPos(pos++);
      } else {
        mBuffers[pos++] = next;
        next += narrowtrans->getNumberOfSuccessors();
      }
    }
    mBuffers[pos] = UNDEF_UINT32;
  }

  if (newtrans == 0) {
    newtrans = new NarrowTransitionRecord();
  }
  const jni::IteratorGlue iter2 = uniqtrans.iteratorGlue(cache);
  while (iter2.hasNext()) {
    jobject javaobject = iter2.next();
    jni::TransitionGlue trans(javaobject, cache);
    const jni::EventGlue& event = trans.getEventGlue(cache);
    const NarrowEventRecord* eventrecord = eventmap.get(&event);
    if (eventrecord->isGloballyDisabled() ||
        eventrecord->isLocallySelflooped(numstates)) {
      continue;
    }
    const uint32 eventcode = eventrecord->getEventCode();
    const jni::StateGlue& source = trans.getSourceGlue(cache);
    const uint32 sourcecode = statemap->get(&source);
    newtrans->init(sourcecode, eventcode);
    NarrowTransitionRecord* oldtrans = narrowtransmap.get(newtrans);
    const jni::StateGlue& target = trans.getTargetGlue(cache);
    const uint32 targetcode = statemap->get(&target);
    oldtrans->putSuccessor(mBuffers, targetcode);
  }

  mAutomaton->deleteStateMap(statemap);
  for (uint32 code = 0; code < numstates; code++) {
    narrowstates[code].~NarrowStateRecord();
  }
  delete (const char*) narrowstates;
}


NarrowTransitionTable::
~NarrowTransitionTable()
{
  delete[] mStateTable;
  delete[] mBuffers;
}


//############################################################################
//# NarrowTransitionTable: Auxiliary Methods



}  /* namespace waters */
