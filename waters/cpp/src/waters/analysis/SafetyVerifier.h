//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id: SafetyVerifier.h,v 1.1 2006-11-03 01:00:07 robi Exp $
//###########################################################################


#ifndef _SafetyVerifier_h_
#define _SafetyVerifier_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

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
}


namespace waters {

class EventRecord;
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

private:
  //##########################################################################
  //# Auxiliary Methods
  void setup();
  bool checkProperty();
  void computeCounterExample();
  void teardown();

  //##########################################################################
  //# Data Members
  jni::ClassCache* mCache;
  jni::ProductDESGlue mModel;
  jni::KindTranslatorGlue mKindTranslator;
  AutomatonEncoding* mEncoding;
  StateSpace* mStateSpace;
  ArrayList<uint32>* mDepthMap;
  int mNumEventRecords;
  EventRecord** mEventRecords;
  uint32* mCurrentTuple;
  uint32 mBadState;
  const EventRecord* mBadEvent;
  jni::ListGlue* mTraceList;
};

}   /* namespace waters */

#endif  /* !_SafetyVerifier_h_ */
