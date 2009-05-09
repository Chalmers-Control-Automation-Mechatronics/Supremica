//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id: SafetyVerifier.h,v 1.5 2007-04-18 03:45:53 robi Exp $
//###########################################################################


#ifndef _SafetyVerifier_h_
#define _SafetyVerifier_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <ctime>

#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/SafetyTraceGlue.h"
#include "waters/base/ArrayList.h"
#include "waters/base/IntTypes.h"
#include "waters/analysis/AutomatonEncoding.h"


namespace jni {
  class ClassCache;
  class ListGlue;
  class ProductDESGlue;
  class VerificationResultGlue;
}


namespace waters {

class AutomatonRecord;
class EventRecord;
class NondeterministicTransitionIterator;
class SafetyVerifier;
class StateSpace;


//############################################################################
//# class SafetyVerifier
//############################################################################

class SafetyVerifier
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit SafetyVerifier(jni::ProductDESGlue des,
			  jni::KindTranslatorGlue translator,
			  jni::ClassCache* cache);
  virtual ~SafetyVerifier();

public:
  //##########################################################################
  //# Invocation
  bool run();
  jni::SafetyTraceGlue getCounterExample
    (const jni::ProductDESProxyFactoryGlue& factory) const;
  void addStatistics(const jni::VerificationResultGlue& vresult) const;

  //##########################################################################
  //# Parameters
  void setStateLimit(uint32 limit) {mStateLimit = limit;}

private:
  //##########################################################################
  //# Auxiliary Methods
  void setup();
  void teardown();
  bool checkProperty();
  void computeCounterExample();
  bool checkTraceState();

  //##########################################################################
  //# Data Members
  jni::ClassCache* mCache;
  jni::ProductDESGlue mModel;
  jni::KindTranslatorGlue mKindTranslator;
  uint32 mStateLimit;
  AutomatonEncoding* mEncoding;
  StateSpace* mStateSpace;
  ArrayList<uint32>* mDepthMap;
  int mNumEventRecords;
  bool mIsTrivial;
  EventRecord** mEventRecords;
  int mNumNondetInitialStates;
  NondeterministicTransitionIterator* mNondeterministicTransitionIterators;
  uint32 mTraceState;
  const EventRecord* mTraceEvent;
  uint32 mTraceLimit;
  jni::ListGlue* mTraceList;
  int mNumAutomata;
  uint32 mNumStates;
  clock_t mStartTime;
  clock_t mTraceStartTime;
  clock_t mStopTime;
};

}   /* namespace waters */

#endif  /* !_SafetyVerifier_h_ */
