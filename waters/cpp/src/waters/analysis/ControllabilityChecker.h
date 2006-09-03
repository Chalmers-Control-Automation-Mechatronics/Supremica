//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.h,v 1.5 2006-09-03 17:09:15 robi Exp $
//###########################################################################


#ifndef _ControllabilityChecker_h_
#define _ControllabilityChecker_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

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
//# class ControllabilityChecker
//############################################################################

class ControllabilityChecker
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ControllabilityChecker(jni::ProductDESGlue des,
				  jni::ClassCache* cache);
  virtual ~ControllabilityChecker();

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

#endif  /* !_ControllabilityChecker_h_ */
