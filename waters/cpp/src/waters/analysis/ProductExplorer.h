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

#include "jni/glue/ConflictKindGlue.h"
#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "waters/base/ArrayList.h"
#include "waters/base/IntTypes.h"
#include "waters/analysis/AutomatonEncoding.h"


namespace jni {
  class ClassCache;
  class ConflictTraceGlue;
  class ListGlue;
  class ProductDESGlue;
  class SafetyTraceGlue;
  class VerificationResultGlue;
}


namespace waters {

class StateSpace;


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
  explicit ProductExplorer(const jni::ProductDESGlue& des,
			   const jni::KindTranslatorGlue& translator,
			   const jni::EventGlue& marking,
			   jni::ClassCache* cache);
  virtual ~ProductExplorer();

public:
  //##########################################################################
  //# Invocation
  virtual bool runSafetyCheck();
  virtual bool runNonblockingCheck();
  virtual jni::SafetyTraceGlue getSafetyCounterExample
    (const jni::ProductDESProxyFactoryGlue& factory, jstring name) const;
  virtual jni::ConflictTraceGlue getConflictCounterExample
    (const jni::ProductDESProxyFactoryGlue& factory, jstring name) const;
  virtual void addStatistics(const jni::VerificationResultGlue& vresult) const;

  //##########################################################################
  //# Parameters
  void setStateLimit(uint32 limit) {mStateLimit = limit;}

protected:
  //##########################################################################
  //# Auxiliary Methods
  virtual void setup(bool safety);
  virtual void teardown();
  virtual bool doSafetySearch();
  virtual bool doNonblockingReachabilitySearch();
  virtual bool doNonblockingCoreachabilitySearch();
  virtual void computeCounterExample(const jni::ListGlue& list, uint32 level);
  virtual void storeInitialStates(bool initzero);
  virtual bool expandSafetyState
    (const uint32* sourcetuple, const uint32* sourcepacked) = 0;
  virtual bool expandNonblockingReachabilityState
    (uint32 source, const uint32* sourcetuple, const uint32* sourcepacked) = 0;
  virtual void expandNonblockingCoreachabilityState
    (const uint32* targettuple, const uint32* targetpacked) = 0;
  virtual const jni::EventGlue& getTraceEvent() = 0;
  virtual void setupReverseTransitionRelations() = 0;
  virtual void expandTraceState
    (const uint32* targettuple, const uint32* targetpacked) = 0;
  
  void exploreNonblockingCoreachabilityStateDFS
    (uint32* targettuple, uint32* targetpacked);
  void checkCoreachabilityState();
  void checkTraceState();
  uint32 getDepth(uint32 state) const;

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
  inline uint32 getNumberOfStates() const {return mNumStates;}
  inline uint32 incNumberOfStates() {return mNumStates++;}
  inline void setTraceState(uint32 state) {mTraceState = state;}
  inline jni::ConflictKind getConflictKind() const {return mConflictKind;}
  inline void setConflictKind(jni::ConflictKind kind) {mConflictKind = kind;}

  //##########################################################################
  //# Class Constants
  static const uint32 TAG_COREACHABLE;
  static const uint32 DFS_STACK_SIZE = 0x00100000;

private:
  //##########################################################################
  //# Data Members
  jni::ClassCache* mCache;
  jni::ProductDESGlue mModel;
  jni::KindTranslatorGlue mKindTranslator;
  jni::EventGlue mMarking;
  uint32 mStateLimit;
  AutomatonEncoding* mEncoding;
  StateSpace* mStateSpace;
  ArrayList<uint32>* mDepthMap;
  bool mIsTrivial;
  int mNumAutomata;
  uint32 mNumStates;
  uint32 mNumCoreachableStates;
  uint32* mDFSStack;
  uint32 mDFSStackSize;
  uint32 mDFSStackPos;
  jni::ListGlue* mTraceList;
  uint32 mTraceState;
  uint32 mTraceLimit;
  jni::ConflictKind mConflictKind;
  clock_t mStartTime;
  clock_t mTraceStartTime;
  clock_t mStopTime;

};

}   /* namespace waters */

#endif  /* !_ProductExplorer_h_ */
