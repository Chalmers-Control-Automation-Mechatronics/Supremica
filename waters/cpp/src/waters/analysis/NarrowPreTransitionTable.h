//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowPreTransitionTable
//###########################################################################
//# $Id$
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
#include "waters/base/IntTypes.h"
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
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const {return value;}
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
  explicit NarrowTransitionRecord(uint32 state = UNDEF_UINT32,
                                  const NarrowEventRecord* event = 0) :
    mState(state), mEvent(event), mNumSuccessors(1), mNext(0) {}
  ~NarrowTransitionRecord() {delete mNext;}
  void init(uint32 state, const NarrowEventRecord* event)
    {mState = state; mEvent = event;}

  //##########################################################################
  //# Simple Access
  const NarrowEventRecord* getEvent() const {return mEvent;}
  uint32 getEventCode() const;
  bool isDeterministic() const {return mNumSuccessors <= 1;}
  uint32 getNumberOfSuccessors() const {return mNumSuccessors;}
  NarrowTransitionRecord* getNext() const {return mNext;}
  void addSuccessor() {mNumSuccessors++;}
  void setNext(NarrowTransitionRecord* next) {mNext = next;}
  void setBufferPos(uint32 pos) {mBufferPos = pos;}

  //##########################################################################
  //# Building the Transition Table
  void putSuccessor(uint32* buffer, uint32 code, uint32 endtag);

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const NarrowTransitionRecord* record) const;
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}
  static const LinkedRecordAccessor<NarrowTransitionRecord>* getListAccessor()
    {return &theListAccessor;}

private:
  //##########################################################################
  //# Data Members
  uint32 mState;
  const NarrowEventRecord* mEvent;
  uint32 mNumSuccessors;
  NarrowTransitionRecord* mNext;
  uint32 mBufferPos;

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
  explicit NarrowStateRecord(uint32 code) :
    mNumEvents(0),
    mTransitionRecords(0)
  {}
  ~NarrowStateRecord() {delete mTransitionRecords;}

  //##########################################################################
  //# Simple Access
  uint32 getNumberOfEnabledEvents() const {return mNumEvents;}
  uint32 getNumberOfNondeterministicTransitions() const;
  NarrowTransitionRecord* getTransitions() const {return mTransitionRecords;}
  void addTransition(NarrowTransitionRecord* trans);
  void removeSkippable(const NarrowPreTransitionTable* pre);
  void sort();

private:
  //##########################################################################
  //# Data Members
  uint32 mNumEvents;
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
			   const HashTable<const jni::EventGlue*,
			                   NarrowEventRecord*>& eventmap);
  ~NarrowPreTransitionTable();

  //##########################################################################
  //# Simple Access
  AutomatonRecord* getAutomaton() const {return mAutomaton;}
  NarrowStateRecord* getNarrowStateRecord(uint32 code) const
    {return &mNarrowStates[code];}
  uint32 getStateCode(const jni::StateGlue& state) const
    {return mStateMap->get(&state);}
  bool isLocallySelflooped(const NarrowEventRecord *event) const;
  const jni::SetGlue& getUniqueTransitions() const
    {return *mUniqueTransitions;}

private:
  //##########################################################################
  //# Data Members
  AutomatonRecord* mAutomaton;
  NarrowStateRecord* mNarrowStates;
  HashTable<const jni::StateGlue*,uint32>* mStateMap;
  HashTable<const jni::EventGlue*,NarrowEventRecord*>* mSelfloopMap;
  jni::SetGlue* mUniqueTransitions;
};


}   /* namespace waters */

#endif  /* !_NarrowTransitionTable_h_ */
