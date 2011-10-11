//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowPreTransitionTable
//###########################################################################
//# $Id: NarrowPreTransitionTable.h 4788 2009-10-27 09:17:36Z robi $
//###########################################################################

#ifndef _NarrowPreTransitionTable_h_
#define _NarrowPreTransitionTable_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/HashAccessor.h"
#include "waters/base/HashTable.h"
#include <stdint.h>
#include "waters/base/LinkedRecordList.h"


namespace jni {
  class ClassCache;
  class EventGlue;
  class SetGlue;
  class StateGlue;
}


namespace waters {

class AutomatonRecord;
class NarrowEventRecord;
class NarrowPreTransitionTable;  // forward
class NarrowStateRecord;         // forward
class NarrowTransitionRecord;    // forward


//###########################################################################
//# Class NarrowTransitionRecordHashAccessor
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
  virtual uint64_t hash(intptr_t key) const;
  virtual bool equals(intptr_t key1, intptr_t key2) const;
};


//############################################################################
//# Class NarrowTransitionRecordListAccessor
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
//# class NarrowTransitionRecord
//############################################################################

class NarrowTransitionRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowTransitionRecord(uint32_t state = UINT32_MAX,
                                  const NarrowEventRecord* event = 0) :
    mState(state), mEvent(event), mNumSuccessors(1), mNext(0) {}
  ~NarrowTransitionRecord() {delete mNext;}
  void init(uint32_t state, const NarrowEventRecord* event)
    {mState = state; mEvent = event;}

  //##########################################################################
  //# Simple Access
  const NarrowEventRecord* getEvent() const {return mEvent;}
  uint32_t getEventCode() const;
  bool isDeterministic() const {return mNumSuccessors <= 1;}
  uint32_t getNumberOfSuccessors() const {return mNumSuccessors;}
  NarrowTransitionRecord* getNext() const {return mNext;}
  void addSuccessor() {mNumSuccessors++;}
  void setNext(NarrowTransitionRecord* next) {mNext = next;}
  void setBufferPos(uint32_t pos) {mBufferPos = pos;}

  //##########################################################################
  //# Building the Transition Table
  void putSuccessor(uint32_t* buffer, uint32_t code, uint32_t endtag);

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const NarrowTransitionRecord* record) const;
  static const NarrowTransitionRecordHashAccessor* getHashAccessor()
    {return &theHashAccessor;}
  static const LinkedRecordAccessor<NarrowTransitionRecord>* getListAccessor()
    {return &theListAccessor;}

private:
  //##########################################################################
  //# Data Members
  uint32_t mState;
  const NarrowEventRecord* mEvent;
  uint32_t mNumSuccessors;
  NarrowTransitionRecord* mNext;
  uint32_t mBufferPos;

  //##########################################################################
  //# Class Variables
  static const NarrowTransitionRecordHashAccessor theHashAccessor;
  static const NarrowTransitionRecordListAccessor theListAccessor;

  friend class NarrowStateRecord;
  friend class NarrowTransitionRecordHashAccessor;
  friend class NarrowTransitionRecordListAccessor;
};


//############################################################################
//# class NarrowStateRecord
//############################################################################

class NarrowStateRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowStateRecord(uint32_t code) :
    mNumEvents(0),
    mTransitionRecords(0)
  {}
  ~NarrowStateRecord() {delete mTransitionRecords;}

  //##########################################################################
  //# Simple Access
  uint32_t getNumberOfEnabledEvents() const {return mNumEvents;}
  uint32_t getNumberOfNondeterministicTransitions() const;
  NarrowTransitionRecord* getTransitions() const {return mTransitionRecords;}
  void addTransition(NarrowTransitionRecord* trans);
  void removeSkippable(const NarrowPreTransitionTable* pre);
  void sort();

private:
  //##########################################################################
  //# Data Members
  uint32_t mNumEvents;
  NarrowTransitionRecord* mTransitionRecords;
};


//############################################################################
//# class NarrowPreTransitionTable
//############################################################################

class NarrowPreTransitionTable
{
public:
  //##########################################################################
  //# Constructors & Destructors
  NarrowPreTransitionTable(AutomatonRecord* aut,
			   jni::ClassCache* cache,
			   const PtrHashTable<const jni::EventGlue*,
			                      NarrowEventRecord*>& eventmap);
  ~NarrowPreTransitionTable();

  //##########################################################################
  //# Simple Access
  AutomatonRecord* getAutomaton() const {return mAutomaton;}
  NarrowStateRecord* getNarrowStateRecord(uint32_t code) const
    {return &mNarrowStates[code];}
  uint32_t getStateCode(const jni::StateGlue& state) const
    {return mStateMap->get(&state);}
  bool isLocallySelflooped(const NarrowEventRecord *event) const;
  const jni::SetGlue& getUniqueTransitions() const
    {return *mUniqueTransitions;}

private:
  //##########################################################################
  //# Data Members
  AutomatonRecord* mAutomaton;
  NarrowStateRecord* mNarrowStates;
  Int32PtrHashTable<const jni::StateGlue*,uint32_t>* mStateMap;
  PtrHashTable<const jni::EventGlue*,NarrowEventRecord*>* mSelfloopMap;
  jni::SetGlue* mUniqueTransitions;
};


}   /* namespace waters */

#endif  /* !_NarrowTransitionTable_h_ */
