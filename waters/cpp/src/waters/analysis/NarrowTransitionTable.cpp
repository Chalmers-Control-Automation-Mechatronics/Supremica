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

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/NarrowEventRecord.h"
#include "waters/analysis/NarrowTransitionTable.h"


namespace waters {

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
//# class NarrowTransitionRecord (local)
//############################################################################

class NarrowTransitionRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowTransitionRecord(uint32 state, uint32 event) :
    mState(state), mEvent(event), mNumSuccessors(1), mNext(0) {}
  void init(uint32 state, uint32 event) {mState = state; mEvent = event;}

  //##########################################################################
  //# Simple Access
  bool isDeterministic() const {return mNumSuccessors <= 1;}
  void addSuccessor() {mNumSuccessors++;}
  void setNext(NarrowTransitionRecord* next) {mNext = next;}

  //##########################################################################
  //# Comparing and Hashing
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}

private:
  //##########################################################################
  //# Data Members
  uint32 mState;
  uint32 mEvent;
  uint32 mNumSuccessors;
  NarrowTransitionRecord* mNext;

  //##########################################################################
  //# Class Variables
  static const NarrowTransitionRecordHashAccessor theHashAccessor;
  friend class NarrowTransitionRecordHashAccessor;
};


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
//# NarrowTransitionRecord: Class Variables

const NarrowTransitionRecordHashAccessor
  NarrowTransitionRecord::theHashAccessor;




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
        //   StateRecord(state, code, cache),
    mNumEvents(0),
    mTransitionRecords(0)
  {}

  //##########################################################################
  //# Simple Access
  void addTransition(NarrowTransitionRecord* trans);

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


//############################################################################
//# class NarrowTransitionTable
//############################################################################

//############################################################################
//# NarrowTransitionTable: Constructors & Destructors

NarrowTransitionTable::
NarrowTransitionTable(AutomatonRecord* aut)
  : mAutomaton(aut),
    mStateTable(0),
    mBuffers(0)
{
}

NarrowTransitionTable::
~NarrowTransitionTable()
{
  delete[] mBuffers;
}


//############################################################################
//# NarrowTransitionTable: Auxiliary Methods

void NarrowTransitionTable::
setup(jni::ClassCache* cache,
      const HashTable<const jni::EventGlue*,NarrowEventRecord*>& eventmap)
{
  //AutomatonStateMap statemap(cache, mAutomaton);

  const uint32 numstates = mAutomaton->getNumberOfStates();
  NarrowStateRecord* narrowstates =
    (NarrowStateRecord*) new char[numstates * sizeof(NarrowStateRecord)];
  for (uint32 code = 0; code < numstates; code++) {
    const jni::StateGlue& state = mAutomaton->getJavaState(code);
    new (&narrowstates[code]) NarrowStateRecord(state, code, cache);
  }

  const jni::AutomatonGlue& autglue = mAutomaton->getJavaAutomaton();
  const jni::CollectionGlue transitions = autglue.getTransitionsGlue(cache);
  const int numtrans = transitions.size();
  const HashAccessor* accessor = NarrowTransitionRecord::getHashAccessor();
  HashTable<NarrowTransitionRecord*,NarrowTransitionRecord*>
    narrowtrans(accessor, numtrans);

  int transcount = 0;
  int ndcount = 0;
  NarrowTransitionRecord* newtrans = 0;
  const jni::IteratorGlue iter1 = transitions.iteratorGlue(cache);
  while (iter1.hasNext()) {
    jobject javaobject = iter1.next();
    jni::TransitionGlue trans(javaobject, cache);
    const jni::EventGlue& event = trans.getEventGlue(cache);
    const NarrowEventRecord* eventrecord = eventmap.get(&event);
    const uint32 eventcode = eventrecord->getEventCode();
    const jni::StateGlue& source = trans.getSourceGlue(cache);
    const uint32 sourcecode = 0; // statemap.getStateCode(source);
    if (newtrans == 0) {
      newtrans = new NarrowTransitionRecord(sourcecode, eventcode);
    } else {
      newtrans->init(sourcecode, eventcode);
    }
    NarrowTransitionRecord* oldtrans = narrowtrans.add(newtrans);
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

}


}  /* namespace waters */
