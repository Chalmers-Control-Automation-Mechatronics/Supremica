//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadEventRecord
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _BroadEventRecord_h_
#define _BroadEventRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/EventGlue.h"
#include "waters/analysis/EventRecord.h"


namespace waters {

class AutomatonRecord;
class HashAccessor;
class TransitionRecord;
class TransitionUpdateRecord;


//############################################################################
//# class BroadEventRecord
//############################################################################

class BroadEventRecord : public EventRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BroadEventRecord(jni::EventGlue event,
			    bool controllable,
			    int numwords);
  virtual ~BroadEventRecord();

  //##########################################################################
  //# Simple Access
  inline bool isGloballyDisabled() const {return mIsGloballyDisabled;}
  inline bool isDisabledInSpec() const {return mIsDisabledInSpec;}
  inline bool isOnlySelfloops() const {return mIsOnlySelfloops;}
  bool isSkippable(bool safety) const;
  inline bool isDeterministic() const {return mIsDeterministic;}
  inline int getNumberOfUpdates() const {return mNumberOfUpdates;}
  inline TransitionRecord* getTransitionRecord() const
    {return mUsedSearchRecords;}
  inline TransitionUpdateRecord* getTransitionUpdateRecord(int w) const
    {return mUpdateRecords[w];}

  //##########################################################################
  //# Comparing and Hashing
  int compareToForForwardSearch(const BroadEventRecord* partner) const;
  int compareToForBackwardSearch(const BroadEventRecord* partner) const;
  static int compareForForwardSearch(const void* elem1, const void* elem2);
  static int compareForBackwardSearch(const void* elem1, const void* elem2);

  //##########################################################################
  //# Set up
  bool addDeterministicTransition(const AutomatonRecord* aut,
				  uint32 source, uint32 target);
  void setupNondeterministicBuffers(const AutomatonRecord* aut);
  void addNondeterministicTransition(const AutomatonRecord* aut,
				     uint32 source, uint32 target);
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
  bool mIsGloballyDisabled;
  bool mIsOnlySelfloops;
  bool mIsDisabledInSpec;
  bool mIsDeterministic;
  int mNumberOfWords;
  int mNumberOfUpdates;
  TransitionRecord* mUsedSearchRecords;
  TransitionRecord* mUnusedSearchRecords;
  TransitionUpdateRecord** mUpdateRecords;
  float mProbability;
};

}   /* namespace waters */

#endif  /* !_BroadEventRecord_h_ */
