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
#include "waters/analysis/CheckType.h"

namespace jni {
  class MapGlue;
}


namespace waters {

class AutomatonRecord;
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
  explicit BroadEventRecord(BroadEventRecord& fwd);
  virtual ~BroadEventRecord();

  //##########################################################################
  //# Simple Access
  inline bool isGloballyDisabled() const {return mIsGloballyDisabled;}
  inline bool isDisabledInSpec() const {return mIsDisabledInSpec;}
  inline bool isOnlySelfloops() const {return mNumNonSelfloopingRecords == 0;}
  bool isSkippable(CheckType mode) const;
  inline bool isDeterministic() const
    {return mNumNondeterministicRecords == 0;}
  inline int getNumberOfUpdates() const {return mNumberOfUpdates;}
  inline int getNumberOfNondeterministicUpdates() const
    {return mNumNondeterministicRecords;}
  inline TransitionRecord* getTransitionRecord() const
    {return mUsedSearchRecords;}
  inline TransitionUpdateRecord* getTransitionUpdateRecord(int w) const
    {return mUpdateRecords[w];}
  inline BroadEventRecord* getForwardRecord() const {return mForwardRecord;}

  //##########################################################################
  //# Comparing and Hashing
  int compareToForForwardSearch(const BroadEventRecord* partner) const;
  int compareToForBackwardSearch(const BroadEventRecord* partner) const;
  static int compareForForwardSearch(const void* elem1, const void* elem2);
  static int compareForBackwardSearch(const void* elem1, const void* elem2);

  //##########################################################################
  //# Set up
  bool addDeterministicTransition(const AutomatonRecord* aut,
				  uint32_t source, uint32_t target);
  void setupNondeterministicBuffers(const AutomatonRecord* aut);
  void addNondeterministicTransition(const AutomatonRecord* aut,
				     uint32_t source, uint32_t target);
  void normalize(const AutomatonRecord* aut);
  TransitionUpdateRecord* createUpdateRecord(int wordindex);
  void optimizeTransitionRecordsForSearch(CheckType mode);
  void setupNotTakenSearchRecords();
  void markTransitionsTaken(const uint32_t* tuple);
  int removeTransitionsNotTaken();
  BroadEventRecord* createReversedRecord();

  inline void markTransitionsTakenFast(const uint32_t* tuple)
    {if (mNotTakenSearchRecords) markTransitionsTaken(tuple);}

  //##########################################################################
  //# Trace Computation
  float getFanout(const uint32_t* sourcetuple) const;
  void storeNondeterministicTargets(const uint32_t* sourcetuple,
				    const uint32_t* targettuple,
				    const jni::MapGlue& map) const;

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dumpTransitionRecords() const;
#endif /* DEBUG */

private:
  //##########################################################################
  //# Auxiliary Methods
  void relink(TransitionRecord* trans);
  void addReversedList(const TransitionRecord* trans);
  void enqueueSearchRecord(TransitionRecord* trans);
  void clearSearchAndUpdateRecords();
  void storeNondeterministicTargets(TransitionRecord* trans,
				    const uint32_t* sourcetuple,
				    const uint32_t* targettuple,
				    const jni::MapGlue& map) const;

  //##########################################################################
  //# Data Members
  bool mIsGloballyDisabled;
  bool mIsDisabledInSpec;
  int mNumNonSelfloopingRecords;
  int mNumNondeterministicRecords;
  int mNumberOfWords;
  int mNumberOfUpdates;
  TransitionRecord* mUsedSearchRecords;
  TransitionRecord* mUnusedSearchRecords;
  TransitionRecord* mNotTakenSearchRecords;
  TransitionRecord* mNonSelfloopingRecord;
  TransitionUpdateRecord** mUpdateRecords;
  float mProbability;
  BroadEventRecord* mForwardRecord;
};

}   /* namespace waters */

#endif  /* !_BroadEventRecord_h_ */
