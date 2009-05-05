//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.h,v 1.9 2006-11-24 23:25:59 robi Exp $
//###########################################################################


#ifndef _EventRecord_h_
#define _EventRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/EventGlue.h"
#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class JavaString;
}


namespace waters {

class AutomatonRecord;
class HashAccessor;
class StateRecord;
class TransitionRecord;
class TransitionUpdateRecord;


//###########################################################################
//# Class EventRecordHashAccessor
//###########################################################################

class EventRecordHashAccessor : public PtrHashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventRecordHashAccessor() {};
  friend class EventRecord;

public:
  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const;
};



//############################################################################
//# class EventRecord
//############################################################################

class EventRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventRecord(jni::EventGlue event, bool controllable, int numwords);
  ~EventRecord();

  //##########################################################################
  //# Simple Access
  inline bool isControllable() const {return mIsControllable;}
  inline bool isGloballyDisabled() const {return mIsGloballyDisabled;}
  inline bool isDisabledInSpec() const {return mIsDisabledInSpec;}
  inline bool isOnlySelfloops() const {return mIsOnlySelfloops;}
  bool isSkippable() const;
  inline bool isDeterministic() const {return mIsDeterministic;}
  const jni::EventGlue& getJavaEvent() const {return mJavaEvent;}
  jni::JavaString getName() const;
  inline TransitionRecord* getTransitionRecord() const
    {return mUsedSearchRecords;}
  inline TransitionUpdateRecord* getTransitionUpdateRecord(int w) const
    {return mUpdateRecords[w];}

  //##########################################################################
  //# Comparing and Hashing
  int compareToForForwardSearch(const EventRecord* partner) const;
  int compareToForBackwardSearch(const EventRecord* partner) const;
  static int compareForForwardSearch(const void* elem1, const void* elem2);
  static int compareForBackwardSearch(const void* elem1, const void* elem2);
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}

  //##########################################################################
  //# Set up
  bool addDeterministicTransition(const AutomatonRecord* aut,
				  const StateRecord* source,
				  const StateRecord* target);
  void setupNondeterministicBuffers(const AutomatonRecord* aut);
  void addNondeterministicTransition(const AutomatonRecord* aut,
				     const StateRecord* source,
				     const StateRecord* target);
  void normalize(const AutomatonRecord* aut);
  TransitionUpdateRecord* createUpdateRecord(int wordindex);
  void sortTransitionRecordsForSearch();
  bool reverse();

private:
  //##########################################################################
  //# Auxiliary Methods
  void addReversedList(TransitionRecord* trans);
  void enqueueSearchRecord(TransitionRecord* trans);
  void clearSearchAndUpdateRecords();

  //##########################################################################
  //# Data Members
  jni::EventGlue mJavaEvent;
  bool mIsControllable;
  bool mIsGloballyDisabled;
  bool mIsOnlySelfloops;
  bool mIsDisabledInSpec;
  bool mIsDeterministic;
  int mNumberOfWords;
  TransitionRecord* mUsedSearchRecords;
  TransitionRecord* mUnusedSearchRecords;
  TransitionUpdateRecord** mUpdateRecords;
  float mProbability;

  //##########################################################################
  //# Class Variables
  static const EventRecordHashAccessor theHashAccessor;
};

}   /* namespace waters */

#endif  /* !_EventRecord_h_ */
