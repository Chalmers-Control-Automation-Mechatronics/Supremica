//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionRecord
//###########################################################################
//# $Id: TransitionRecord.h,v 1.3 2006-11-22 21:27:57 robi Exp $
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


namespace waters {

class AutomatonRecord;
class StateRecord;


//############################################################################
//# class TransitionRecord
//############################################################################

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
  const AutomatonRecord* getAutomaton() const {return mAutomaton;}
  uint32 getShiftedSuccessor(uint32 source) const
    {return mShiftedSuccessors[source];}
  bool isAlwaysDisabled() const {return mWeight == 0;}
  bool isAlwaysEnabled() const {return mWeight == PROBABILITY_1;}
  bool isOnlySelfloops() const {return mIsOnlySelfloops;}
  TransitionRecord* getNextInSearch() const {return mNextInSearch;}
  void setNextInSearch(TransitionRecord* next) {mNextInSearch = next;}
  TransitionRecord* getNextInUpdate() const {return mNextInUpdate;}
  void setNextInUpdate(TransitionRecord* next) {mNextInUpdate = next;}

  //##########################################################################
  //# Comparing and Hashing
  int compareToForSearch(const TransitionRecord* partner) const;
  int compareToForTrace(const TransitionRecord* partner) const;
  static int compareForSearch(const TransitionRecord* trans1,
			      const TransitionRecord* trans2);
  static int compareForTrace(const TransitionRecord* trans1,
			     const TransitionRecord* trans2);

  //##########################################################################
  //# Set up
  bool addTransition(const StateRecord* source, const StateRecord* target);
  void normalize();
  uint32 getCommonTarget() const;

private:
  //##########################################################################
  //# Data Members
  const AutomatonRecord* mAutomaton;
  int mWeight;
  bool mIsOnlySelfloops;
  uint32* mShiftedSuccessors;
  TransitionRecord* mNextInSearch;
  TransitionRecord* mNextInUpdate;

  //##########################################################################
  //# Class Constants
  static const int PROBABILITY_1 = 0x10000000;
};

typedef int (*TransitionRecordComparator)
  (const TransitionRecord*, const TransitionRecord*);


//############################################################################
//# class TransitionRecordList
//############################################################################

class TransitionRecordList
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TransitionRecordList();
  explicit TransitionRecordList(TransitionRecord* record);

  //##########################################################################
  //# List Access
  bool isEmpty() const {return mHead == 0;}
  TransitionRecord* getHead() const {return mHead;}
  TransitionRecord* getTail() const {return mTail;}
  void append(TransitionRecord* record);
  void append(const TransitionRecordList& list);

  //##########################################################################
  //# Sorting
  void qsort(TransitionRecordComparator comparator);

private:
  //##########################################################################
  //# Auxiliary Methods
  void seek();

  //##########################################################################
  //# Data Members
  TransitionRecord* mHead;
  TransitionRecord* mTail;
};


}   /* namespace waters */

#endif  /* !_TransitionRecord_h_ */
