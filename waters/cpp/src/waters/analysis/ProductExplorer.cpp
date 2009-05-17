//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ProductExplorer
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>
#include <time.h>
#include <stdlib.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/JavaString.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/ProductExplorer.h"
#include "waters/analysis/StateSpace.h"
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# class ProductExplorer
//############################################################################

//############################################################################
//# ProductExplorer: Constructors & Destructors

ProductExplorer::
ProductExplorer(const jni::ProductDESGlue des,
		const jni::KindTranslatorGlue translator,
		jni::ClassCache* cache)
  : mCache(cache),
    mModel(des),
    mKindTranslator(translator),
    mStateLimit(UNDEF_UINT32),
    mEncoding(0),
    mStateSpace(0),
    mDepthMap(0),
    mIsTrivial(false),
    mNumAutomata(0),
    mNumStates(0),
    mTraceList(0),
    mTraceState(UNDEF_UINT32),
    mTraceLimit(UNDEF_UINT32)
{
}


ProductExplorer::
~ProductExplorer()
{
  delete mEncoding;
  delete mStateSpace;
  delete mDepthMap;
  delete mTraceList;
}


//############################################################################
//# ProductExplorer: Invocation

bool ProductExplorer::
runSafetyCheck()
{
  try {
    // const jni::JavaString name(mCache->getEnvironment(), mModel.getName());
    // std::cerr << (const char*) name << std::endl;
    mStartTime = clock();
    setup();
    bool result = mIsTrivial || doSafetySearch();
    if (!result) {
      mTraceStartTime = clock();
      mTraceList = new jni::LinkedListGlue(mCache);
      const jni::EventGlue& eventglue = getTraceEvent();
      mTraceList->add(0, &eventglue);
      computeCounterExample(*mTraceList);
    }
    teardown();
    mStopTime = clock();
    return result;
  } catch (...) {
    teardown();
    throw;
  }
}

jni::SafetyTraceGlue ProductExplorer::
getSafetyCounterExample(const jni::ProductDESProxyFactoryGlue& factory)
  const
{
  return factory.createSafetyTraceProxyGlue(&mModel, mTraceList, mCache);
}


void ProductExplorer::
addStatistics(const jni::VerificationResultGlue& vresult)
  const
{
  vresult.setNumberOfAutomata(mNumAutomata);
  vresult.setNumberOfStates(mNumStates);
  vresult.setPeakNumberOfNodes(mNumStates);
  /*
  char buffer[40];
  double totaltime =
    (double) (mStopTime - mStartTime) / (double) CLOCKS_PER_SEC;
  sprintf(buffer, "%.3f", totaltime);
  std::cerr << "Total time: " << buffer << "s" << std::endl;
  if (mTraceList != 0) {
    double tracetime =
      (double) (mStopTime - mTraceStartTime) / (double) CLOCKS_PER_SEC;
    sprintf(buffer, "%.3f", tracetime);
    std::cerr << "Trace time: " << buffer << "s" << std::endl;
  }
  */
}


//############################################################################
//# ProductExplorer: Auxiliary Methods

void ProductExplorer::
setup()
{
  // Establish automaton encoding ...
  mEncoding = new AutomatonEncoding(mModel, mKindTranslator, mCache);
  // mEncoding->dump();
  if (mEncoding->hasSpecs()) {
    mIsTrivial = false;
    mNumAutomata = mEncoding->getNumberOfRecords();
    mNumStates = 0;
    mStateSpace = new StateSpace(mEncoding, mStateLimit);
    mDepthMap = new ArrayList<uint32>(128);
  } else {
    mIsTrivial = true;
    mNumStates = 0;
  }
}


void ProductExplorer::
teardown()
{
  delete mEncoding;
  mEncoding = 0;
  delete mStateSpace;
  mStateSpace = 0;
  delete mDepthMap;
  mDepthMap = 0;
}


bool ProductExplorer::
doSafetySearch()
{
  // Store initial states ...
  storeInitialStates();

  // Prepare depth map ...
  uint32 nextlevel = mNumStates;
  mDepthMap->add(0);
  mDepthMap->add(nextlevel);

  // Main loop ...
  uint32 current = 0;
  uint32* currenttuple = new uint32[mNumAutomata];
  try {
    while (current < mNumStates) {
      uint32* currentpacked = mStateSpace->get(current);
      mEncoding->decode(currentpacked, currenttuple);
      if (!expandState(currenttuple, currentpacked)) {
        mTraceState = current;
        delete [] currenttuple;
        return false;
      } else if (++current == nextlevel) {
        nextlevel = mNumStates;
        mDepthMap->add(nextlevel);
      }
    }
    delete [] currenttuple;
    return true;
  } catch (...) {
    delete [] currenttuple;
    throw;
  }
}


void ProductExplorer::
computeCounterExample(const jni::ListGlue& list)
{
  uint32 level = mDepthMap->size() - 2;
  if (level > 0) {
    setupReverseTransitionRelations();
    uint32* targettuple = new uint32[mNumAutomata];
    try {
      do {
        mTraceLimit = mDepthMap->get(level);
        const uint32* targetpacked = mStateSpace->get(mTraceState);
        mEncoding->decode(targetpacked, targettuple);
        expandTraceState(targettuple, targetpacked);
        const jni::EventGlue& eventglue = getTraceEvent();
        list.add(0, &eventglue);
      } while (level-- > 1);
      delete [] targettuple;
    } catch (...) {
      delete [] targettuple;
      throw;
    }
  }
}


bool ProductExplorer::
checkTraceState()
{
  uint32 found = mStateSpace->find();
  if (found >= mTraceLimit) {
    return false;
  } else {
    mTraceState = found;
    throw SearchAbort();
  }
}


}  /* namespace waters */



//############################################################################
//# ProductExplorer: Invocation through JNI

JNIEXPORT jobject JNICALL 
Java_net_sourceforge_waters_cpp_analysis_NativeSafetyVerifier_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  try {
    jni::ClassCache cache(env);
    try {
      jni::NativeSafetyVerifierGlue gchecker(jchecker, &cache);
      jni::ProductDESGlue des = gchecker.getModelGlue(&cache);
      jni::KindTranslatorGlue translator =
        gchecker.getKindTranslatorGlue(&cache);
      waters::BroadProductExplorer checker(des, translator, &cache);
      const int limit = gchecker.getNodeLimit();
      if (limit != UNDEF_INT32) {
        checker.setStateLimit(limit);
      }
      bool result = checker.runSafetyCheck();
      if (result) {
        jni::VerificationResultGlue vresult(result, 0, &cache);
        checker.addStatistics(vresult);
        return vresult.returnJavaObject();
      } else {
        jni::ProductDESProxyFactoryGlue factory =
          gchecker.getFactoryGlue(&cache);
        jni::SafetyTraceGlue trace = checker.getSafetyCounterExample(factory);
        jni::VerificationResultGlue vresult(result, &trace, &cache);
        checker.addStatistics(vresult);
        return vresult.returnJavaObject();
      }
    } catch (const jni::PreJavaException& pre) {
      cache.throwJavaException(pre);
      return 0;
    }
  } catch (jthrowable exception) {
    return 0;
  }
}
