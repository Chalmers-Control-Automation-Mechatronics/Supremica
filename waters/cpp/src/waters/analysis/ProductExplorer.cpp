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
#include "jni/glue/ConflictKindGlue.h"
#include "jni/glue/ConflictTraceGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeConflictCheckerGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/SafetyTraceGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/ProductExplorer.h"
#include "waters/analysis/StateSpace.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# class ProductExplorer
//############################################################################

//############################################################################
//# ProductExplorer: Constructors & Destructors

ProductExplorer::
ProductExplorer(const jni::ProductDESGlue& des,
		const jni::KindTranslatorGlue& translator,
		jni::ClassCache* cache)
  : mCache(cache),
    mModel(des),
    mKindTranslator(translator),
    mMarking(0, cache),
    mStateLimit(UNDEF_UINT32),
    mEncoding(0),
    mStateSpace(0),
    mDepthMap(0),
    mIsTrivial(false),
    mNumAutomata(0),
    mNumStates(0),
    mTraceList(0),
    mTraceState(UNDEF_UINT32),
    mTraceLimit(UNDEF_UINT32),
    mConflictKind(jni::ConflictKind_CONFLICT)
{
}


ProductExplorer::
ProductExplorer(const jni::ProductDESGlue& des,
		const jni::KindTranslatorGlue& translator,
                const jni::EventGlue& marking,
		jni::ClassCache* cache)
  : mCache(cache),
    mModel(des),
    mKindTranslator(translator),
    mMarking(marking),
    mStateLimit(UNDEF_UINT32),
    mEncoding(0),
    mStateSpace(0),
    mDepthMap(0),
    mIsTrivial(false),
    mNumAutomata(0),
    mNumStates(0),
    mTraceList(0),
    mTraceState(UNDEF_UINT32),
    mTraceLimit(UNDEF_UINT32),
    mConflictKind(jni::ConflictKind_CONFLICT)
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
    setupSafety();
    bool result;
    if (mIsTrivial) {
      result = true;
    } else {
      storeInitialStates(true);
      result = doSafetySearch();
      if (!result) {
        mTraceStartTime = clock();
        mTraceList = new jni::LinkedListGlue(mCache);
        const jni::EventGlue& eventglue = getTraceEvent();
        mTraceList->add(0, &eventglue);
        computeCounterExample(*mTraceList);
      }
    }
    teardown();
    mStopTime = clock();
    return result;
  } catch (...) {
    teardown();
    throw;
  }
}

bool ProductExplorer::
runNonblockingCheck()
{
  try {
    // const jni::JavaString name(mCache->getEnvironment(), mModel.getName());
    // std::cerr << (const char*) name << std::endl;
    mStartTime = clock();
    bool result = true;
    setupNonblocking();
    if (!mIsTrivial || mTraceState != UNDEF_UINT32) {
      storeInitialStates(false);
      if (mIsTrivial) {
        result = false;
        // mDeadlock = "transitions enabled in mBadState";
      } else {
        result = doNonblockingReachabilitySearch();
        if (result) {
          mConflictKind = jni::ConflictKind_LIVELOCK;
          setupReverseTransitionRelations();
          //   result = doNonblockingCoreachabilitySearch();
        }
      }
      if (!result) {
        mTraceStartTime = clock();
        mTraceList = new jni::LinkedListGlue(mCache);
        computeCounterExample(*mTraceList);
      }
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
  jni::JavaString name(mCache->getEnvironment(), mModel.getName());
  name += ":unsafe";
  return factory.createSafetyTraceProxyGlue
    (name.getJavaString(), &mModel, mTraceList, mCache);
}


jni::ConflictTraceGlue ProductExplorer::
getConflictCounterExample(const jni::ProductDESProxyFactoryGlue& factory)
  const
{
  jni::JavaString name(mCache->getEnvironment(), mModel.getName());
  name += ":blocking";
  jni::ConflictKindGlue kind(mConflictKind, mCache);
  return factory.createConflictTraceProxyGlue
    (name.getJavaString(), &mModel, mTraceList, &kind, mCache);
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
setupSafety()
{
  // Establish automaton encoding ...
  mEncoding = new AutomatonEncoding(mModel, mKindTranslator, mCache);
  // mEncoding->dump();
  if (mEncoding->hasSpecs()) {
    mIsTrivial = false;
    mNumAutomata = mEncoding->getNumberOfRecords();
    mStateSpace = new StateSpace(mEncoding, mStateLimit);
    mDepthMap = new ArrayList<uint32>(128);
  } else {
    mIsTrivial = true;
  }
  mNumStates = 0;
  mTraceState = UNDEF_UINT32;
}

void ProductExplorer::
setupNonblocking()
{
  // Establish automaton encoding ...
  mEncoding = new AutomatonEncoding(mModel, mKindTranslator, mCache, 1);
  mNumAutomata = mEncoding->getNumberOfRecords();
  mIsTrivial = (mNumAutomata == 0);
  if (!mIsTrivial) {
    mStateSpace = new TaggedStateSpace(mEncoding, mStateLimit);
    mDepthMap = new ArrayList<uint32>(128);
  }
  mNumStates = 0;
  mTraceState = UNDEF_UINT32;
  mConflictKind = jni::ConflictKind_CONFLICT;
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


#define DO_REACHABILITY                                         \
  {                                                             \
    uint32 nextlevel = mNumStates;                              \
    uint32 current = 0;                                         \
    uint32* currenttuple = new uint32[mNumAutomata];            \
    try {                                                       \
      while (current < mNumStates) {                            \
        uint32* currentpacked = mStateSpace->get(current);      \
        mEncoding->decode(currentpacked, currenttuple);         \
        if (!EXPAND(current, currenttuple, currentpacked)) {    \
          mTraceState = current;                                \
          delete [] currenttuple;                               \
          return false;                                         \
        } else if (++current == nextlevel) {                    \
          nextlevel = mNumStates;                               \
          mDepthMap->add(nextlevel);                            \
        }                                                       \
      }                                                         \
      delete [] currenttuple;                                   \
      return true;                                              \
    } catch (...) {                                             \
      delete [] currenttuple;                                   \
      throw;                                                    \
    }                                                           \
  }

#define EXPAND(source, sourcetuple, sourcepacked) \
  expandSafetyState(sourcetuple, sourcepacked)

bool ProductExplorer::
doSafetySearch()
{
  DO_REACHABILITY;
}

#undef EXPAND
#define EXPAND(source, sourcetuple, sourcepacked) \
  expandNonblockingState(source, sourcetuple, sourcepacked)

bool ProductExplorer::
doNonblockingReachabilitySearch()
{
  DO_REACHABILITY;
}

#undef EXPAND


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


void ProductExplorer::
storeInitialStates(bool initzero)
{
  uint32* initpacked = mStateSpace->prepare();
  if (initzero) {
    const int numwords = getAutomatonEncoding().getNumberOfWords();
    for (int w = 0; w < numwords; w++) {
      initpacked[w] = 0;
    }
  } else {
    uint32* inittuple = new uint32[mNumAutomata];
    for (int a = 0; a < mNumAutomata; a++) {
      const AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
      inittuple[a] = aut->getFirstInitialState();
    }
    getAutomatonEncoding().encode(inittuple, initpacked);
    delete [] inittuple;
  }
  mStateSpace->add();
  const int ndcount = mEncoding->getNumberOfNondeterministicInitialAutomata();
  if (ndcount == 0) {
    mNumStates++;
  } else {
    NondeterministicTransitionIterator* iters =
      new NondeterministicTransitionIterator[ndcount];
    try {
      int ndindex = 0;
      for (int a = 0; a < mNumAutomata; a++) {
        const AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
        if (aut->getNumberOfInitialStates() > 1) {
          iters[ndindex++].setupInit(aut);
        }
      }
      do {
        initpacked = mStateSpace->prepare(mNumStates++);
        for (ndindex = 0; ndindex < ndcount; ndindex++) {
          if (!iters[ndindex].advanceInit(initpacked)) {
            break;
          }
        }
        if (ndindex < ndcount) {
          mStateSpace->add();
        }
      } while (ndindex < ndcount);
      delete [] iters;
    } catch (...) {
      delete [] iters;
      throw;
    }
  }
  mDepthMap->add(0);
  mDepthMap->add(mNumStates);
}


bool ProductExplorer::
checkDeadlockState(const uint32 source)
{
  uint32 code = mStateSpace->add();
  if (code != source) {
    mConflictKind = jni::ConflictKind_CONFLICT;
  }
  return code == mNumStates;
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


JNIEXPORT jobject JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeConflictChecker_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  try {
    jni::ClassCache cache(env);
    try {
      jni::NativeConflictCheckerGlue gchecker(jchecker, &cache);
      jni::ProductDESGlue des = gchecker.getModelGlue(&cache);
      jni::KindTranslatorGlue translator =
        gchecker.getKindTranslatorGlue(&cache);
      jni::EventGlue marking = gchecker.getUsedMarkingPropositionGlue(&cache);
      waters::BroadProductExplorer checker(des, translator, marking, &cache);
      const int limit = gchecker.getNodeLimit();
      if (limit != UNDEF_INT32) {
        checker.setStateLimit(limit);
      }
      bool result = checker.runNonblockingCheck();
      if (result) {
        jni::VerificationResultGlue vresult(result, 0, &cache);
        checker.addStatistics(vresult);
        return vresult.returnJavaObject();
      } else {
        jni::ProductDESProxyFactoryGlue factory =
          gchecker.getFactoryGlue(&cache);
        jni::ConflictTraceGlue trace =
          checker.getConflictCounterExample(factory);
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
