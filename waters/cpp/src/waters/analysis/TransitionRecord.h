//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

#ifndef _TransitionRecord_h_
#define _TransitionRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "waters/base/LinkedRecordList.h"
#include "waters/base/WordSize.h"


namespace jni {
  class MapGlue;
}

namespace waters {

class AutomatonRecord;


//############################################################################
//# class TransitionRecord
//############################################################################

class TransitionRecordAccessorForSearch;
class TransitionRecordAccessorForTrace;


class TransitionRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TransitionRecord(const AutomatonRecord* aut,
			    TransitionRecord* next = 0,
			    const TransitionRecord* fwd = 0);
  explicit TransitionRecord(const TransitionRecord& fwd);
  ~TransitionRecord();

  //##########################################################################
  //# Simple Access
  inline const AutomatonRecord* getAutomaton() const {return mAutomaton;}
  inline uint32_t getDeterministicSuccessorShifted(uint32_t source) const
    {return mDeterministicSuccessorsShifted[source];}
  inline uint32_t getNumberOfNondeterministicSuccessors(uint32_t source) const
    {return mNumNondeterministicSuccessors[source];} // unsafe
  inline uint32_t getNondeterministicSuccessorShifted
    (uint32_t source, uint32_t index) const
    {return mNondeterministicSuccessorsShifted[source][index];} // unsafe
  inline bool isEnabled(uint32_t source) const
    {return mDeterministicSuccessorsShifted[source] != NO_TRANSITION;}
  uint32_t getNumberOfSuccessors(uint32_t source) const; // slow
  uint32_t getSuccessorShifted(uint32_t source, uint32_t index) const; // slow
  inline bool isAlwaysDisabled() const {return mWeight == 0;}
  inline bool isAlwaysEnabled() const {return mWeight == PROBABILITY_1;}
  inline bool isOnlySelfloops() const {return mIsOnlySelfloops;}
  inline bool isDeterministic() const
    {return mNumNondeterministicSuccessors == 0;}
  float getProbability() const;
  inline TransitionRecord* getNextInSearch() const {return mNextInSearch;}
  inline void setNextInSearch(TransitionRecord* next) {mNextInSearch = next;}
  inline TransitionRecord* getNextInUpdate() const {return mNextInUpdate;}
  inline void setNextInUpdate(TransitionRecord* next) {mNextInUpdate = next;}
  inline TransitionRecord* getNextInNotTaken() const {return mNextInNotTaken;}
  inline void setNextInNotTaken(TransitionRecord* next)
    {mNextInNotTaken = next;}

  //##########################################################################
  //# Comparing and Hashing
  int compareToForSearch
    (const TransitionRecord* partner, bool controllable) const;
  int compareToForTrace(const TransitionRecord* partner) const;
  static const TransitionRecordAccessorForSearch* getSearchAccessor
    (bool controllable);
  static const TransitionRecordAccessorForTrace* getTraceAccessor()
    {return &theTraceAccessor;}

  //##########################################################################
  //# Set up
  bool addDeterministicTransition(uint32_t source, uint32_t target);
  void addNondeterministicTransition(uint32_t source, uint32_t target);
  void normalize();
  uint32_t getCommonTarget() const;
  bool markTransitionTaken(const uint32_t* tuple);
  int removeTransitionsNotTaken();
  void removeSelfloops();

  //##########################################################################
  //# Trace Computation
  void storeNondeterministicTarget(const uint32_t* sourcetuple,
				   const uint32_t* targettuple,
				   const jni::MapGlue& statemap) const;

  //##########################################################################
  //# Class Constants
  static const uint32_t NO_TRANSITION = 0xffffffff;
  static const uint32_t MULTIPLE_TRANSITIONS = 0xfffffffe;

private:
  //##########################################################################
  //# Auxiliary Methods
  void setupNondeterministicBuffers();

  //##########################################################################
  //# Data Members
  const AutomatonRecord* mAutomaton;
  int mWeight;
  bool mIsReversedCopy;
  bool mIsOnlySelfloops;
  uint32_t* mFlags;
  uint32_t* mDeterministicSuccessorsShifted;
  uint32_t* mNumNondeterministicSuccessors;
  uint32_t* mNondeterministicBuffer;
  uint32_t** mNondeterministicSuccessorsShifted;
  uint32_t mNumNotTaken;
  TransitionRecord* mNextInSearch;
  TransitionRecord* mNextInUpdate;
  TransitionRecord* mNextInNotTaken;

  //##########################################################################
  //# Class Constants
  static const uint32_t FLAG_NONDET = 0x00000001;
  static const uint32_t FLAG_TAKEN = 0x00000002;

  static const int PROBABILITY_1 = 0x40000000;
  STATIC_FLOATCONST float PROBABILITY_ADJUST = 1.0f / PROBABILITY_1;

  static const TransitionRecordAccessorForSearch theControllableSearchAccessor;
  static const TransitionRecordAccessorForSearch
    theUncontrollableSearchAccessor;
  static const TransitionRecordAccessorForTrace theTraceAccessor;

  //##########################################################################
  //# Friends
  friend class FastEligibilityTestRecord;
};


//############################################################################
//# class FastEligibilityTestRecord
//############################################################################

class FastEligibilityTestRecord
{
public:
  //##########################################################################
  //# Simple Access
  void setup(const TransitionRecord* trans, uint32_t event);
  inline int getAutomatonIndex() const {return mAutomatonIndex;}
  inline uint32_t getEventIndex() const {return mEventIndex;}
  inline bool isEnabled(uint32_t source) const
  {
    return mDeterministicSuccessorsShifted[source] != 
	   TransitionRecord::NO_TRANSITION;
  }

  //##########################################################################
  //# Comparing & Hashing
  bool equals(const FastEligibilityTestRecord& record) const;
  uint64_t hash() const;

private:
  //##########################################################################
  //# Data Members
  int mAutomatonIndex;
  uint32_t mEventIndex;
  uint32_t* mDeterministicSuccessorsShifted;
};


//############################################################################
//# class TransitionRecordAccessorForSearch
//############################################################################

class TransitionRecordAccessorForSearch :
  public LinkedRecordAccessor<TransitionRecord>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  TransitionRecordAccessorForSearch(bool controllable) :
    mControllable(controllable) {}

  //##########################################################################
  //# Override for LinkedRecordAccessor
  virtual TransitionRecord* getNext(const TransitionRecord* record) const
    {return record->getNextInSearch();}
  virtual void setNext(TransitionRecord* record, TransitionRecord* next) const
    {record->setNextInSearch(next);}
  virtual int compare(const TransitionRecord* record1,
		      const TransitionRecord* record2)
    const
    {return record1->compareToForSearch(record2, mControllable);}

private:
  //##########################################################################
  //# Data Members
  bool mControllable;
};


//############################################################################
//# class TransitionRecordAccessorForTrace
//############################################################################

class TransitionRecordAccessorForTrace :
  public LinkedRecordAccessor<TransitionRecord>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  TransitionRecordAccessorForTrace() {}

  //##########################################################################
  //# Override for LinkedRecordAccessor
  virtual TransitionRecord* getNext(const TransitionRecord* record) const
    {return record->getNextInSearch();}
  virtual void setNext(TransitionRecord* record, TransitionRecord* next) const
    {record->setNextInSearch(next);}
  virtual int compare(const TransitionRecord* record1,
		      const TransitionRecord* record2)
    const
    {return record1->compareToForTrace(record2);}
};


 //############################################################################
//# class NondeterministicTransitionIterator
//############################################################################

class NondeterministicTransitionIterator
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NondeterministicTransitionIterator();
  ~NondeterministicTransitionIterator() {}

  //##########################################################################
  //# Initial State Iteration
  void setupInit(const AutomatonRecord* aut);
  bool advanceInit(uint32_t* tuple);

  //##########################################################################
  //# Transition Iteration
  uint32_t setup(const TransitionRecord* trans, uint32_t source);
  bool advance(uint32_t* tuple);
  uint32_t reset();
  uint32_t next();
  uint32_t current();

private:
  //##########################################################################
  //# Data Members
  const AutomatonRecord* mAutomatonRecord;
  const TransitionRecord* mTransitionRecord;
  uint32_t mSource;
  int mIndex;  
};


}   /* namespace waters */

#endif  /* !_TransitionRecord_h_ */
