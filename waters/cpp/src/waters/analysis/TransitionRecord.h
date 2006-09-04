//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionRecord
//###########################################################################
//# $Id: TransitionRecord.h,v 1.2 2006-09-04 11:04:41 robi Exp $
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
  uint32 getSuccessor(uint32 source) const {return mSuccessorStates[source];}
  bool isAlwaysDisabled() const {return mWeight == 0;}
  bool isAlwaysEnabled() const {return mWeight == PROBABILITY_1;}
  bool isAllSelfloops() const {return mIsOnlySelfloops && isAlwaysEnabled();}
  TransitionRecord* getNext() const {return mNext;}
  void setNext(TransitionRecord* next) {mNext = next;}

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const TransitionRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);

  //##########################################################################
  //# Set up
  bool addTransition(const StateRecord* source, const StateRecord* target);
  void normalize();

private:
  //##########################################################################
  //# Data Members
  const AutomatonRecord* mAutomaton;
  int mWeight;
  bool mIsOnlySelfloops;
  uint32* mSuccessorStates;
  TransitionRecord* mNext;

  //##########################################################################
  //# Class Constants
  static const int PROBABILITY_1 = 0x10000000;
};


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
  void qsort();

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
