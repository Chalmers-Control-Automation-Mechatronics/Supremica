//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionRecord
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _TransitionRecord_h_
#define _TransitionRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"
#include "waters/base/LinkedRecordList.h"


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
			    TransitionRecord* next = 0);
  ~TransitionRecord();

  //##########################################################################
  //# Simple Access
  inline const AutomatonRecord* getAutomaton() const {return mAutomaton;}
  inline uint32 getDeterministicSuccessorShifted(uint32 source) const
    {return mDeterministicSuccessorsShifted[source];}
  inline uint32 getNumberOfNondeterministicSuccessors(uint32 source) const
    {return mNumNondeterministicSuccessors[source];} // unsafe
  inline uint32 getNondeterministicSuccessorShifted
    (uint32 source, uint32 index) const
    {return mNondeterministicSuccessorsShifted[source][index];} // unsafe
  uint32 getNumberOfSuccessors(uint32 source) const; // slow
  uint32 getSuccessorShifted(uint32 source, uint32 index) const; // slow
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
  bool addDeterministicTransition(uint32 source, uint32 target);
  void addNondeterministicTransition(uint32 source, uint32 target);
  void normalize();
  uint32 getCommonTarget() const;

  //##########################################################################
  //# Class Constants
  static const uint32 NO_TRANSITION = 0xffffffff;
  static const uint32 MULTIPLE_TRANSITIONS = 0xfffffffe;

private:
  //##########################################################################
  //# Auxiliary Methods
  void setupNondeterministicBuffers();

  //##########################################################################
  //# Data Members
  const AutomatonRecord* mAutomaton;
  int mWeight;
  bool mIsOnlySelfloops;
  uint32* mDeterministicSuccessorsShifted;
  uint32* mNumNondeterministicSuccessors;
  uint32* mNondeterministicBuffer;
  uint32** mNondeterministicSuccessorsShifted;
  TransitionRecord* mNextInSearch;
  TransitionRecord* mNextInUpdate;

  //##########################################################################
  //# Class Constants
  static const int PROBABILITY_1 = 0x40000000;
  static const float PROBABILITY_ADJUST = 1.0f / PROBABILITY_1;

  static const TransitionRecordAccessorForSearch theControllableSearchAccessor;
  static const TransitionRecordAccessorForSearch
    theUncontrollableSearchAccessor;
  static const TransitionRecordAccessorForTrace theTraceAccessor;
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
  bool advanceInit(uint32* tuple);

  //##########################################################################
  //# Transition Iteration
  uint32 setup(const TransitionRecord* trans, uint32 source);
  bool advance(uint32* tuple);
  uint32 reset();
  uint32 next();
  uint32 current();

private:
  //##########################################################################
  //# Data Members
  const AutomatonRecord* mAutomatonRecord;
  const TransitionRecord* mTransitionRecord;
  uint32 mSource;
  int mIndex;  
};


}   /* namespace waters */

#endif  /* !_TransitionRecord_h_ */
