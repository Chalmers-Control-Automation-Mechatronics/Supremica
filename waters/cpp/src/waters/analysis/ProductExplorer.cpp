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
//# ProductExplorer: Class Constants

const uint32 ProductExplorer::TAG_COREACHABLE = AutomatonEncoding::TAG0;


//############################################################################
//# ProductExplorer: Constructors & Destructors

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
    mTupleStackSize(0),
    mTupleStack(0),
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
  delete [] mTupleStack;
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
    setup(true);
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
        const uint32 level = mDepthMap->size() - 2;
        computeCounterExample(*mTraceList, level);
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
    setup(false);
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
          result = doNonblockingCoreachabilitySearch();
        }
      }
      if (!result) {
        mTraceStartTime = clock();
        mTraceList = new jni::LinkedListGlue(mCache);
        const uint32 level = getDepth(mTraceState);
        computeCounterExample(*mTraceList, level);
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
getSafetyCounterExample(const jni::ProductDESProxyFactoryGlue& factory,
                        jstring name)
  const
{
  return factory.createSafetyTraceProxyGlue(name, &mModel, mTraceList, mCache);
}


jni::ConflictTraceGlue ProductExplorer::
getConflictCounterExample(const jni::ProductDESProxyFactoryGlue& factory,
                          jstring name)
  const
{
  jni::ConflictKindGlue kind(mConflictKind, mCache);
  return factory.createConflictTraceProxyGlue(name, &mModel,
                                              mTraceList, &kind, mCache);
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
setup(bool safety)
{
  // Establish automaton encoding ...
  const int numtags = safety ? 0 : 1;
  mEncoding =
    new AutomatonEncoding(mModel, mKindTranslator, mMarking, mCache, numtags);
  // mEncoding->dump();
  mNumAutomata = mEncoding->getNumberOfRecords();
  mIsTrivial = safety ? !mEncoding->hasSpecs() : mNumAutomata == 0;
  if (!mIsTrivial) {
    mStateSpace = safety ?
      new StateSpace(mEncoding, mStateLimit) :
      new TaggedStateSpace(mEncoding, mStateLimit);
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
  delete [] mTupleStack;
  mTupleStack = 0;
  mTupleStackSize = 0;
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
  expandNonblockingReachabilityState(source, sourcetuple, sourcepacked)

bool ProductExplorer::
doNonblockingReachabilitySearch()
{
  DO_REACHABILITY;
}

#undef EXPAND

bool ProductExplorer::
doNonblockingCoreachabilitySearch()
{
  try {
    const uint32 factor =
      mNumStates < ITER_STACK_SIZE ? mNumStates : ITER_STACK_SIZE;
    const int ndcount = allocateNondeterministicTransitionIterators(factor);
    delete [] mTupleStack;
    mTupleStackSize =
      mNumStates < TUPLE_STACK_SIZE ? mNumStates : TUPLE_STACK_SIZE;
    uint32* currenttuple = mTupleStack =
      new uint32[mTupleStackSize * mNumAutomata];
    mStateSpace->prepareStack(mTupleStackSize);
    mStackOverflow = false;
    mNumCoreachableStates = 0;
    uint32 current;
    for (current = 0; current < mNumStates; current++) {
      uint32* currentpacked = mStateSpace->get(current);
      if (mEncoding->hasTag(currentpacked, TAG_COREACHABLE)) {
        continue;
      }
      if (mEncoding->isMarkedStateTuplePacked(currentpacked)) {
        mEncoding->setTag(currentpacked, TAG_COREACHABLE);
        if (++mNumCoreachableStates == mNumStates) {
          return true;
        }
        mEncoding->decode(currentpacked, currenttuple);
        expandNonblockingCoreachabilityState
          (currenttuple, currentpacked, 1, ndcount);
      }
    }
    while (mStackOverflow) {
      mStackOverflow = false;
      for (current = mNumStates; current-- > 0;) {
        uint32* currentpacked = mStateSpace->get(current);
        if (mEncoding->hasTag(currentpacked, TAG_COREACHABLE)) {
          mEncoding->decode(currentpacked, currenttuple);
          expandNonblockingCoreachabilityState
            (currenttuple, currentpacked, 1, ndcount);
        }
      }
    }
    for (current = 0; current < mNumStates; current++) {
      uint32* currentpacked = mStateSpace->get(current);
      if (!mEncoding->hasTag(currentpacked, TAG_COREACHABLE)) {
        mTraceState = current;
        break;
      }
    }
    return false;
  } catch (const SearchAbort& abort) {
    return true;
  }
}


void ProductExplorer::
computeCounterExample(const jni::ListGlue& list, uint32 level)
{
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


void ProductExplorer::
checkCoreachabilityState(uint32 stackpos, int ndcount)
{
  uint32 found = mStateSpace->find(mNumStates + stackpos);
  if (found == UNDEF_UINT32) {
    return;
  }
  uint32* foundpacked = mStateSpace->get(found);
  if (mEncoding->hasTag(foundpacked, TAG_COREACHABLE)) {
    return;
  }
  mEncoding->setTag(foundpacked, TAG_COREACHABLE);
  if (++mNumCoreachableStates == mNumStates) {
    throw SearchAbort();
  } else if (stackpos < mTupleStackSize &&
             ndcount >= getMinimumNondeterministicTransitionIterators()) {
    uint32* foundtuple = &mTupleStack[stackpos++ * mNumAutomata];
    mEncoding->decode(foundpacked, foundtuple);
    expandNonblockingCoreachabilityState
      (foundtuple, foundpacked, stackpos, ndcount);
  } else {
    mStackOverflow = true;
  }    
}

void ProductExplorer::
checkTraceState()
{
  uint32 found = mStateSpace->find();
  if (found < mTraceLimit) {
    mTraceState = found;
    throw SearchAbort();
  }
}

uint32 ProductExplorer::
getDepth(uint32 state)
  const
{
  // return level such that depth[level] <= state < depth[level+1]
  uint32 l = 0;
  uint32 u = mDepthMap->size() - 1;
  while (l < u) {
    uint32 m = (l + u) >> 1;
    uint32 m1 = m + 1;
    if (state < mDepthMap->get(m1)) {
      u = m;
    } else {
      l = m1;
    }
  }
  return l;
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
      jni::EventGlue marking(0, &cache);
      waters::BroadProductExplorer checker(des, translator, marking, &cache);
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
        jstring name = gchecker.getTraceName();
        jni::SafetyTraceGlue trace =
          checker.getSafetyCounterExample(factory, name);
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
        jstring name = gchecker.getTraceName();
        jni::ConflictTraceGlue trace =
          checker.getConflictCounterExample(factory, name);
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
