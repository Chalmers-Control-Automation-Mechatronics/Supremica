//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.h,v 1.6 2006-11-22 21:27:57 robi Exp $
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
  bool isControllable() const {return mIsControllable;}
  bool isGloballyDisabled() const {return mIsGloballyDisabled;}
  bool isSkippable() const;
  const jni::EventGlue& getJavaEvent() const {return mJavaEvent;}
  jni::JavaString getName() const;
  TransitionRecord* getTransitionRecord() const {return mSearchRecords;}
  TransitionUpdateRecord* getTransitionUpdateRecord(int w) const
    {return mUpdateRecords[w];}

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const EventRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}

  //##########################################################################
  //# Set up
  bool addTransition(const AutomatonRecord* aut,
		     const StateRecord* source,
		     const StateRecord* target);
  void normalize(const AutomatonRecord* aut);
  TransitionUpdateRecord* createUpdateRecord(int wordindex);
  void sortTransitionRecordsForSearch();
  void sortTransitionRecordsForTrace();

private:
  //##########################################################################
  //# Data Members
  jni::EventGlue mJavaEvent;
  bool mIsControllable;
  bool mIsGloballyDisabled;
  int mNumberOfWords;
  TransitionRecord* mSearchRecords;
  TransitionRecord* mTraceSearchRecords;
  TransitionUpdateRecord** mUpdateRecords;

  //##########################################################################
  //# Class Variables
  static const EventRecordHashAccessor theHashAccessor;
};

}   /* namespace waters */

#endif  /* !_EventRecord_h_ */
