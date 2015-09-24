//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

#ifndef _ProductExplorer_h_
#define _ProductExplorer_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <ctime>

#include <stdint.h>
#include <jni.h>

#include "jni/glue/ConflictCheckModeGlue.h"
#include "jni/glue/ConflictKindGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/StateGlue.h"

#include "waters/base/ArrayList.h"
#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/CheckType.h"
#include "waters/analysis/ReverseTransitionStore.h"
#include "waters/analysis/TarjanStateSpace.h"


namespace jni {
  class ClassCache;
  class ConflictTraceGlue;
  class ListGlue;
  class NativeConflictCheckerGlue;
  class NativeSafetyVerifierGlue;
  class SafetyTraceGlue;
  class NativeVerificationResultGlue;
}


namespace waters {

class EventRecord;
class ProductExplorer;


//############################################################################
//# exception SearchAbort
//############################################################################

class SearchAbort {
public:
  //##########################################################################
  //# Constructors & Destructors
  SearchAbort() {}
  ~SearchAbort() {}
};


//############################################################################
//# exception DFSStackOverflow
//############################################################################

class DFSStackOverflow {
public:
  //##########################################################################
  //# Constructors & Destructors
  DFSStackOverflow() {}
  ~DFSStackOverflow() {}
};


//############################################################################
//# callback TransitionCallBack
//############################################################################

typedef bool (ProductExplorer::*TransitionCallBack)
  (uint32_t, const EventRecord*, uint32_t);


//############################################################################
//# class ProductExplorer
//############################################################################

class ProductExplorer
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
			   const jni::ProductDESGlue& des,
			   const jni::KindTranslatorGlue& translator,
			   const jni::EventGlue& premarking,
			   const jni::EventGlue& marking,
			   jni::ClassCache* cache);
  virtual ~ProductExplorer();

  //##########################################################################
  //# Invocation
  virtual bool runSafetyCheck();
  virtual bool runNonblockingCheck();
  virtual jni::SafetyTraceGlue getSafetyCounterExample
    (const jni::NativeSafetyVerifierGlue& gchecker) const;
  virtual jni::ConflictTraceGlue getConflictCounterExample
    (const jni::NativeConflictCheckerGlue& gchecker) const;
  virtual void addStatistics
    (const jni::NativeVerificationResultGlue& vresult) const;

  //##########################################################################
  //# Parameters
  inline uint32_t getStateLimit() const {return mStateLimit;}
  inline void setStateLimit(uint32_t limit) {mStateLimit = limit;}
  inline uint32_t getTransitionLimit() const {return mTransitionLimit;}
  inline void setTransitionLimit(uint32_t limit) {mTransitionLimit = limit;}
  inline jni::ConflictCheckMode getConflictCheckMode() const
    {return mConflictCheckMode;}
  inline void setConflictCheckMode(jni::ConflictCheckMode mode)
    {mConflictCheckMode = mode;}
  inline bool isDumpStateAware() const {return mDumpStateAware;}
  inline void setDumpStateAware(bool aware) {mDumpStateAware = aware;}
  inline bool isInitialUncontrollable() const {return mIsInitialUncontrollable;}
  inline void setInitialUncontrollable(bool initUncont)
    {mIsInitialUncontrollable = initUncont;}

  //##########################################################################
  //# Simple Access
  inline CheckType getCheckType() const {return mCheckType;}
  inline StateSpace& getStateSpace() {return *mStateSpace;}
  inline int getNumberOfAutomata() const {return mNumAutomata;}
  inline uint32_t getNumberOfStates() const {return mNumStates;}
  inline uint32_t incNumberOfStates() {return mNumStates++;}
  inline uint32_t incNumberOfTransitions() {return mNumTransitions++;}
  inline uint32_t incNumberOfTransitionsExplored()
    {return mNumTransitionsExplored++;}
  inline const EventRecord* getTraceEvent() const {return mTraceEvent;}
  void setTraceEvent(const EventRecord* event) {mTraceEvent = event;}
  void setTraceEvent(const EventRecord* event, const AutomatonRecord* aut)
    {mTraceEvent = event; mTraceAutomaton = aut;}

  //##########################################################################
  //# Aborting
  inline void requestAbort() {mIsAbortRequested = true;}
  inline void resetAbort() {mIsAbortRequested = false;}

protected:
  //##########################################################################
  //# Auxiliary Methods
  virtual void setup();
  virtual void teardown();
  inline void checkAbort() const {if (mIsAbortRequested) doAbort();}
  void doAbort() const;

  //##########################################################################
  //# Search Algorithms
  virtual bool doSafetySearch();

  virtual bool doNonblockingReachabilitySearch();
  bool expandNonblockingReachabilityState
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  bool rememberNonDeadlockTransition
    (uint32_t source, const EventRecord* event, uint32_t target);
  bool addCoreachabilityTransition
    (uint32_t source, const EventRecord* event, uint32_t target);

  virtual bool doNonblockingCoreachabilitySearch();
  void exploreNonblockingCoreachabilityStateDFS(uint32_t target);
  void exploreNonblockingCoreachabilityStateDFS
    (uint32_t target, uint32_t* targetTuple, uint32_t* targetPacked);
  bool processCoreachabilityTransition
    (uint32_t source, const EventRecord* event, uint32_t target);

  virtual bool doNonblockingTarjanSearch();
  virtual bool doNonblockingTarjanSearch(uint32_t root);
  bool processTarjanTransition
    (uint32_t source, const EventRecord* event, uint32_t target);
  bool closeNonblockingTarjanState(uint32_t state, uint32_t* tupleBuffer);
  bool closeNonblockingTarjanTransition
    (uint32_t source, const EventRecord* event, uint32_t target);

  virtual void computeBFSCounterExample
    (const jni::ListGlue& list, uint32_t level);

  virtual void computeTarjanCounterExample(const jni::ListGlue& list);
  void expandTarjanTraceState
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, BlockedArrayList<uint32_t>* successors);
  bool processTarjanTraceTransition
    (uint32_t source, const EventRecord* event, uint32_t target);

  uint32_t getFirstState(uint32_t level) const {return mDepthMap->get(level);}
  uint32_t getNumberOfInitialStates() const {return getFirstState(1);}
  uint32_t getDepth(uint32_t state) const;

  //##########################################################################
  //# State Expansion Procedures
  virtual void storeInitialStates(bool initzero, bool donondet = true);
  virtual bool isLocalDumpState(const uint32_t* tuple) const;
  virtual void setupReverseTransitionRelations() = 0;
  virtual bool expandForwardSafety
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0) = 0;
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0) = 0;
  virtual bool expandForwardAgain
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0) = 0;
  virtual bool expandReverse
    (uint32_t target, const uint32_t* targetTuple,
     const uint32_t* targetPacked, TransitionCallBack callBack = 0) = 0;
  virtual void expandTraceState
    (uint32_t target, const uint32_t* targetTuple,
     const uint32_t* targetpacked, uint32_t level) = 0;
  virtual const EventRecord* findEvent
    (uint32_t source, const uint32_t* sourcetuple,
     const uint32_t* sourcepacked, const uint32_t* targetpacked) = 0;
  virtual void storeNondeterministicTargets
    (const uint32_t* sourcetuple, const uint32_t* targettuple,
     const jni::MapGlue& map) = 0;

  //##########################################################################
  //# Simple Access
  inline jni::ClassCache* getCache() {return mCache;}
  inline jni::ProductDESGlue& getModel() {return mModel;}
  inline jni::KindTranslatorGlue& getKindTranslator() {return mKindTranslator;}
  inline jni::EventGlue& getMarking() {return mMarking;}
  inline AutomatonEncoding& getAutomatonEncoding() {return *mEncoding;}
  inline bool isTrivial() const {return mIsTrivial;}
  inline void setTrivial() {mIsTrivial = true;}
  inline void setTraceState(uint32_t state) {mTraceState = state;}
  inline jni::ConflictKind getConflictKind() const {return mConflictKind;}
  inline void setConflictKind(jni::ConflictKind kind) {mConflictKind = kind;}

private:
  //##########################################################################
  //# Data Members
  jni::ClassCache* mCache;
  jni::ProductDESProxyFactoryGlue mFactory;
  jni::ProductDESGlue mModel;
  jni::KindTranslatorGlue mKindTranslator;
  jni::EventGlue mPreMarking;
  jni::EventGlue mMarking;
  jni::ConflictCheckMode mConflictCheckMode;
  uint32_t mStateLimit;
  uint32_t mTransitionLimit;
  bool mDumpStateAware;
  bool mIsInitialUncontrollable;
  CheckType mCheckType;
  bool mIsAbortRequested;
  AutomatonEncoding* mEncoding;
  StateSpace* mStateSpace;
  ArrayList<uint32_t>* mDepthMap;
  ReverseTransitionStore* mReverseTransitionStore;
  bool mIsTrivial;
  int mNumAutomata;
  uint32_t mNumStates;
  uint32_t mNumCoreachableStates;
  uint32_t mNumTransitions;
  uint64_t mNumTransitionsExplored;
  uint32_t* mDFSStack;
  uint32_t mDFSStackSize;
  uint32_t mDFSStackPos;
  jni::ListGlue* mTraceList;
  const EventRecord* mTraceEvent;
  const AutomatonRecord* mTraceAutomaton;
  uint32_t mTraceState;
  bool mGotTarjanTraceSuccessor;
  BlockedArrayList<uint32_t>* mTarjanTraceSuccessors;
  jni::EventGlue mJavaTraceEvent;
  jni::AutomatonGlue mJavaTraceAutomaton;
  jni::StateGlue mJavaTraceState;
  jni::ConflictKind mConflictKind;
  clock_t mStartTime;
  clock_t mTraceStartTime;
  clock_t mStopTime;

  //##########################################################################
  //# Class Constants
  static const uint32_t TAG_COREACHABLE;
  static const uint32_t DFS_STACK_SIZE = 0x00100000;

  //##########################################################################
  //# Friends
  friend class NonblockingTarjanCallBack;
};

}   /* namespace waters */

#endif  /* !_ProductExplorer_h_ */
