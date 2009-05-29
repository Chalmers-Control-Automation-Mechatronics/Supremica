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
#include "waters/analysis/ExplorerMode.h"


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
  inline bool isOnlySelfloops() const {return mNumNonSelfloopingRecords == 0;}
  bool isSkippable(ExplorerMode mode) const;
  inline bool isDeterministic() const
    {return mNumNondeterministicRecords == 0;}
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
  void optimizeTransitionRecordsForSearch(ExplorerMode mode);
  void setupNotTakenSearchRecords();
  void markTransitionsTaken(const uint32* tuple);
  int removeTransitionsNotTaken();
  bool reverse();

  inline void markTransitionsTakenFast(const uint32* tuple)
    {if (mNotTakenSearchRecords) markTransitionsTaken(tuple);}

  //##########################################################################
  //# Trace Computation
  void storeNondeterministicTargets(const uint32* sourcetuple,
				    const uint32* targettuple,
				    const jni::MapGlue& map) const;

private:
  //##########################################################################
  //# Auxiliary Methods
  void relink(TransitionRecord* trans);
  void addReversedList(TransitionRecord* trans);
  void enqueueSearchRecord(TransitionRecord* trans);
  void clearSearchAndUpdateRecords();
  void storeNondeterministicTargets(TransitionRecord* trans,
				    const uint32* sourcetuple,
				    const uint32* targettuple,
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
};

}   /* namespace waters */

#endif  /* !_BroadEventRecord_h_ */
