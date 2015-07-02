//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ProductExplorer
//###########################################################################
//# $Id$
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

#include <jni.h>

#include "jni/glue/ConflictCheckModeGlue.h"
#include "jni/glue/ConflictKindGlue.h"
#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "waters/base/ArrayList.h"
#include <stdint.h>
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
  class ProductDESGlue;
  class SafetyTraceGlue;
  class VerificationResultGlue;
}


namespace waters {

class EventRecord;


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
  virtual void addStatistics(const jni::VerificationResultGlue& vresult) const;

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
  virtual bool doSafetySearch();
  virtual bool doNonblockingReachabilitySearch();
  virtual bool doNonblockingCoreachabilitySearch();
  virtual bool doNonblockingTarjanSearch();
  virtual bool doNonblockingTarjanSearch(uint32_t root);
  virtual void computeBFSCounterExample
    (const jni::ListGlue& list, uint32_t level);
  virtual void computeTarjanCounterExample(const jni::ListGlue& list);
  virtual void storeInitialStates(bool initzero, bool donondet = true);
  virtual bool isLocalDumpState(const uint32_t* tuple) const;
  virtual bool expandSafetyState
    (const uint32_t* sourcetuple, const uint32_t* sourcepacked) = 0;
  virtual bool expandNonblockingReachabilityState
    (uint32_t source, const uint32_t* sourcetuple,
     const uint32_t* sourcepacked) = 0;
  virtual void expandNonblockingCoreachabilityState
    (const uint32_t* targettuple, const uint32_t* targetpacked) = 0;
  virtual void expandTarjanState
    (uint32_t source, const uint32_t* sourcetuple,
     const uint32_t* sourcepacked) = 0;
  virtual bool closeNonblockingTarjanState
    (uint32_t state, uint32_t* tupleBuffer) = 0;
  virtual bool expandTarjanTraceState
    (uint32_t source, const uint32_t* sourcetuple,
     const uint32_t* sourcepacked) = 0;
  virtual void setupReverseTransitionRelations() = 0;
  virtual void expandTraceState
    (const uint32_t* targettuple, const uint32_t* targetpacked,
     uint32_t level) = 0;
  virtual const EventRecord* findEvent
    (const uint32_t* sourcetuple, const uint32_t* sourcepacked,
     const uint32_t* targetpacked) = 0;
  virtual void storeNondeterministicTargets
    (const uint32_t* sourcetuple, const uint32_t* targettuple,
     const jni::MapGlue& map) = 0;
  
  void exploreNonblockingCoreachabilityStateDFS(uint32_t target);
  void exploreNonblockingCoreachabilityStateDFS
    (uint32_t* targettuple, uint32_t* targetpacked);
  void checkCoreachabilityState();
  uint32_t getFirstState(uint32_t level) const {return mDepthMap->get(level);}
  uint32_t getNumberOfInitialStates() const {return getFirstState(1);}
  uint32_t getDepth(uint32_t state) const;

  inline void checkAbort() const {if (mIsAbortRequested) doAbort();}
  void doAbort() const;

  //##########################################################################
  //# Simple Access
  inline jni::ClassCache* getCache() {return mCache;}
  inline jni::ProductDESGlue& getModel() {return mModel;}
  inline jni::KindTranslatorGlue& getKindTranslator() {return mKindTranslator;}
  inline jni::EventGlue& getMarking() {return mMarking;}
  inline AutomatonEncoding& getAutomatonEncoding() {return *mEncoding;}
  inline StateSpace& getStateSpace() {return *mStateSpace;}
  inline bool isTrivial() const {return mIsTrivial;}
  inline void setTrivial() {mIsTrivial = true;}
  inline int getNumberOfAutomata() const {return mNumAutomata;}
  inline uint32_t getNumberOfStates() const {return mNumStates;}
  inline uint32_t incNumberOfStates() {return mNumStates++;}
  inline uint32_t incNumberOfTransitions() {return mNumTransitions++;}
  inline void addCoreachabilityTransition(uint32_t source, uint32_t target)
    {mReverseTransitionStore->addTransition(source, target);}
  inline void setTraceState(uint32_t state) {mTraceState = state;}
  inline jni::ConflictKind getConflictKind() const {return mConflictKind;}
  inline void setConflictKind(jni::ConflictKind kind) {mConflictKind = kind;}

  //##########################################################################
  //# Class Constants
  static const uint32_t TAG_COREACHABLE;
  static const uint32_t DFS_STACK_SIZE = 0x00100000;

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
  uint32_t* mDFSStack;
  uint32_t mDFSStackSize;
  uint32_t mDFSStackPos;
  jni::ListGlue* mTraceList;
  const EventRecord* mTraceEvent;
  const AutomatonRecord* mTraceAutomaton;
  uint32_t mTraceState;
  jni::EventGlue mJavaTraceEvent;
  jni::AutomatonGlue mJavaTraceAutomaton;
  jni::StateGlue mJavaTraceState;
  jni::ConflictKind mConflictKind;
  clock_t mStartTime;
  clock_t mTraceStartTime;
  clock_t mStopTime;

  //##########################################################################
  //# Friends
  friend class NonblockingTarjanCallBack;
};

}   /* namespace waters */

#endif  /* !_ProductExplorer_h_ */
