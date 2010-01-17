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
#include "jni/glue/ExplorerModeGlue.h"
#include "jni/glue/Glue.h"
#include "jni/glue/HashMapGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeConflictCheckerGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/SafetyTraceGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TraceStepGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/EventRecord.h"
#include "waters/analysis/NarrowProductExplorer.h"
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
ProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
                const jni::ProductDESGlue& des,
		const jni::KindTranslatorGlue& translator,
                const jni::EventGlue& premarking,
                const jni::EventGlue& marking,
		jni::ClassCache* cache)
  : mCache(cache),
    mFactory(factory),
    mModel(des),
    mKindTranslator(translator),
    mPreMarking(premarking),
    mMarking(marking),
    mStateLimit(UNDEF_UINT32),
    mTransitionLimit(UNDEF_UINT32),
    mIsAbortRequested(false),
    mEncoding(0),
    mStateSpace(0),
    mDepthMap(0),
    mReverseTransitionStore(0),
    mIsTrivial(false),
    mNumAutomata(0),
    mNumStates(0),
    mDFSStack(0),
    mDFSStackSize(0),
    mTraceList(0),
    mTraceEvent(0),
    mTraceAutomaton(0),
    mTraceState(UNDEF_UINT32),
    mTraceLimit(UNDEF_UINT32),
    mJavaTraceEvent(0, cache),
    mJavaTraceAutomaton(0, cache),
    mJavaTraceState(0, cache),
    mConflictKind(jni::ConflictKind_CONFLICT)
{
}


ProductExplorer::
~ProductExplorer()
{
  delete mEncoding;
  delete mStateSpace;
  delete mDepthMap;
  delete mReverseTransitionStore;
  delete [] mDFSStack;
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
    mMode = EXPLORER_MODE_SAFETY;
    setup();
    bool result;
    if (mIsTrivial) {
      result = true;
    } else {
      storeInitialStates(true);
      result = doSafetySearch();
      if (!result) {
        mTraceStartTime = clock();
        mJavaTraceEvent = mTraceEvent->getJavaEvent();
        if (mTraceAutomaton) {
          mJavaTraceAutomaton = mTraceAutomaton->getJavaAutomaton();
          const uint32* packed = mStateSpace->get(mTraceState);
          const uint32 autindex = mTraceAutomaton->getAutomatonIndex();
          const uint32 statecode = mEncoding->get(packed, autindex);
          mJavaTraceState = mTraceAutomaton->getJavaState(statecode);
        }
        mTraceList = new jni::LinkedListGlue(mCache);
        jni::TraceStepGlue step =
          mFactory.createTraceStepProxyGlue(&mJavaTraceEvent, mCache);
        mTraceList->add(0, &step);
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
    mMode = EXPLORER_MODE_NONBLOCKING;
    setup();
    if (!mIsTrivial || mTraceState != UNDEF_UINT32) {
      if (mIsTrivial) {
        result = false;
        // mDeadlock = "transitions enabled in mTraceState";
      } else {
        storeInitialStates(false);
        result = doNonblockingReachabilitySearch();
        if (result) {
          mConflictKind = jni::ConflictKind_LIVELOCK;
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
getSafetyCounterExample(const jni::NativeSafetyVerifierGlue& gchecker)
  const
{
  jstring name = gchecker.getTraceName();
  jstring comment;
  if (mJavaTraceAutomaton.isNull()) {
    comment = 0;
  } else {
    comment = gchecker.getTraceComment
      (&mJavaTraceEvent, &mJavaTraceAutomaton, &mJavaTraceState);
  }
  const jni::SetGlue automata = mModel.getAutomataGlue(mCache);
  return mFactory.createSafetyTraceProxyGlue
    (name, comment, 0, &mModel, &automata, mTraceList, mCache);
}


jni::ConflictTraceGlue ProductExplorer::
getConflictCounterExample(const jni::NativeConflictCheckerGlue& gchecker)
  const
{
  jstring name = gchecker.getTraceName();
  const jni::SetGlue automata = mModel.getAutomataGlue(mCache);
  const jni::ConflictKindGlue kind(mConflictKind, mCache);
  return mFactory.createConflictTraceProxyGlue
    (name, 0, 0, &mModel, &automata, mTraceList, &kind, mCache);
}


void ProductExplorer::
addStatistics(const jni::VerificationResultGlue& vresult)
  const
{
  vresult.setNumberOfAutomata(mNumAutomata);
  vresult.setNumberOfStates(mNumStates);
  vresult.setNumberOfTransitions(mNumTransitions);
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
  mIsAbortRequested = false;
  // Establish automaton encoding ...
  const int numtags = mMode == EXPLORER_MODE_SAFETY ? 0 : 1;
  mEncoding =
    new AutomatonEncoding(mModel, mKindTranslator,
                          mPreMarking, mMarking, mCache, numtags);
  // mEncoding->dump();
  mNumAutomata = mEncoding->getNumberOfRecords();
  mNumStates = 0;
  mTraceEvent = 0;
  mTraceAutomaton = 0;
  mTraceState = UNDEF_UINT32;
  mConflictKind = jni::ConflictKind_CONFLICT;
  switch (mMode) {
  case EXPLORER_MODE_SAFETY:
    if (mEncoding->hasSpecs()) {
      mStateSpace = new StateSpace(mEncoding, mStateLimit);
      mDepthMap = new ArrayList<uint32>(128);
    } else {
      setTrivial();
    }
    break;
  case EXPLORER_MODE_NONBLOCKING:
    if (mEncoding->isTriviallyNonblocking()) {
      setTrivial();
    } else if (mEncoding->isTriviallyBlocking()) {
      mStateSpace = new StateSpace(mEncoding, 1);
      mDepthMap = new ArrayList<uint32>(2);
      storeInitialStates(false, false);
      setTraceState(0);
      setTrivial();
    } else {
      mStateSpace = new TaggedStateSpace(mEncoding, mStateLimit);
      mDepthMap = new ArrayList<uint32>(128);
      if (mTransitionLimit > 0) {
        mReverseTransitionStore = new ReverseTransitionStore(mTransitionLimit);
      }
    }
    break;
  default:
    // throw exception ?
    break;
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
  if (mReverseTransitionStore) {
    mNumTransitions = mReverseTransitionStore->getNumberOfTransitions();
    delete mReverseTransitionStore;
    mReverseTransitionStore = 0;
  }
  delete [] mDFSStack;
  mDFSStack = 0;
  mDFSStackSize = 0;
  mTraceEvent = 0;
  mTraceAutomaton = 0;
}


#define DO_REACHABILITY                                         \
  {                                                             \
    uint32 nextlevel = mNumStates;                              \
    uint32 current = 0;                                         \
    uint32* currenttuple = new uint32[mNumAutomata];            \
    try {                                                       \
      while (current < mNumStates) {                            \
        checkAbort();                                           \
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
  delete [] mDFSStack;
  mDFSStackSize = mNumStates < DFS_STACK_SIZE ? mNumStates : DFS_STACK_SIZE;
  mDFSStack = new uint32[mDFSStackSize];
  mDFSStackPos = 0;
  mNumCoreachableStates = mPreMarking.isNull() ? 0 : UNDEF_UINT32;
  uint32* currenttuple = 0;
  bool overflow = false;
  if (mTransitionLimit == 0) {
    currenttuple = new uint32[mNumAutomata];
    setupReverseTransitionRelations();
  }
  try {
    for (uint32 current = 0; current < mNumStates; current++) {
      checkAbort();
      uint32* currentpacked = mStateSpace->get(current);
      if (mEncoding->hasTag(currentpacked, TAG_COREACHABLE)) {
        continue;
      }
      if (mEncoding->isMarkedStateTuplePacked(currentpacked)) {
        mEncoding->setTag(currentpacked, TAG_COREACHABLE);
        if (mNumCoreachableStates != UNDEF_UINT32 &&
            ++mNumCoreachableStates == mNumStates) {
          return true;
        }
        if (currenttuple == 0) { // storing
          exploreNonblockingCoreachabilityStateDFS(current);
        } else { // non-storing
          exploreNonblockingCoreachabilityStateDFS(currenttuple,
                                                   currentpacked);
        }
      }
    }
  } catch (const SearchAbort& abort) {
    delete [] currenttuple;
    return true;
  } catch (const DFSStackOverflow& abort) {
    overflow = true;
  }
  while (overflow) {
    overflow = false;
    try {
      for (uint32 current = mNumStates; current-- > 0;) {
        checkAbort();
        uint32* currentpacked = mStateSpace->get(current);
        if (mEncoding->hasTag(currentpacked, TAG_COREACHABLE)) {
          if (currenttuple == 0) {
            exploreNonblockingCoreachabilityStateDFS(current);
          } else {
            exploreNonblockingCoreachabilityStateDFS
              (currenttuple, currentpacked);
          }
        }
      }
    } catch (const SearchAbort& abort) {
      delete [] currenttuple;
      return true;
    } catch (const DFSStackOverflow& abort) {
      overflow = true;
    }
  }
  delete [] currenttuple;
  for (uint32 current = 0; current < mNumStates; current++) {
    checkAbort();
    uint32* currentpacked = mStateSpace->get(current);
    if (!mEncoding->hasTag(currentpacked, TAG_COREACHABLE) &&
        mEncoding->isPreMarkedStateTuplePacked(currentpacked)) {
      mTraceState = current;
      return false;
    }
  }
  return true;
}

void ProductExplorer::
computeCounterExample(const jni::ListGlue& list, uint32 level)
{
  if (level > 0) {
    setupReverseTransitionRelations();
  }
  uint32* buffers = new uint32[2 * mNumAutomata];
  try {
    jni::HashMapGlue statemap(mNumAutomata, mCache);
    uint32* sourcetuple = buffers;
    uint32* targettuple = &buffers[mNumAutomata];
    uint32* targetpacked = mStateSpace->get(mTraceState);
    mEncoding->decode(targetpacked, targettuple);
    while (level > 0) {
      checkAbort();
      mTraceLimit = mDepthMap->get(level);
      expandTraceState(targettuple, targetpacked);
      uint32* sourcepacked = mStateSpace->get(mTraceState);
      mEncoding->decode(sourcepacked, sourcetuple);
      storeNondeterministicTargets(sourcetuple, targettuple, statemap);
      const jni::EventGlue& event = mTraceEvent->getJavaEvent();
      jni::TraceStepGlue step =
        mFactory.createTraceStepProxyGlue(&event, &statemap, mCache);
      statemap.clear();
      list.add(0, &step);
      uint32* tmp = sourcetuple;
      sourcetuple = targettuple;
      targettuple = tmp;
      targetpacked = sourcepacked;
      level--;
    }
    mEncoding->storeNondeterministicInitialStates(targettuple, statemap);
    jni::TraceStepGlue step =
      mFactory.createTraceStepProxyGlue(0, &statemap, mCache);
    list.add(0, &step);
    delete[] buffers;
  } catch (...) {
    delete[] buffers;
    throw;
  }
}


void ProductExplorer::
storeInitialStates(bool initzero, bool donondet)
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
      inittuple[a] = aut->getFirstInitialState1();
    }
    getAutomatonEncoding().encode(inittuple, initpacked);
    delete [] inittuple;
  }
  mStateSpace->add();
  const int ndcount =
    donondet ? mEncoding->getNumberOfNondeterministicInitialAutomata() : 0;
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
        checkAbort();
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
  if (mDepthMap != 0) {
    mDepthMap->add(0);
    mDepthMap->add(mNumStates);
  }
}


#define EXPLORE(target, targettuple, targetpacked)                      \
  {                                                                     \
    EXPAND(target, targettuple, targetpacked);                          \
    while (mDFSStackPos > 0) {                                          \
      uint32 popped = mDFSStack[--mDFSStackPos];                        \
      UNPACK(popped, targetpacked);                                     \
      EXPAND(popped, targettuple, targetpacked);                        \
    }                                                                   \
  }

#define UNPACK(target, targetpacked)

#define EXPAND(target, targettuple, targetpacked)                       \
  {                                                                     \
    uint32 iter = mReverseTransitionStore->iterator(target);            \
    while (mReverseTransitionStore->hasNext(iter)) {                    \
      uint32 source = mReverseTransitionStore->next(iter);              \
      VISIT(source);                                                    \
    }                                                                   \
  }

#define VISIT(source)                                                   \
  {                                                                     \
    uint32* sourcepacked = mStateSpace->get(source);                    \
    if (!mEncoding->hasTag(sourcepacked, TAG_COREACHABLE)) {            \
      mEncoding->setTag(sourcepacked, TAG_COREACHABLE);                 \
      if (mNumCoreachableStates != UNDEF_UINT32 &&                      \
          ++mNumCoreachableStates == mNumStates) {                      \
        throw SearchAbort();                                            \
      } else if (mDFSStackPos < mDFSStackSize) {                        \
        mDFSStack[mDFSStackPos++] = source;                             \
      } else {                                                          \
        throw DFSStackOverflow();                                       \
      }                                                                 \
    }                                                                   \
  }

void ProductExplorer::
exploreNonblockingCoreachabilityStateDFS(uint32 target)
{
  EXPLORE(target, TUPLE, PACKED);
}

#undef EXPAND
#undef UNPACK

#define EXPAND(target, targettuple, targetpacked)                       \
  {                                                                     \
    mEncoding->decode(targetpacked, targettuple);                       \
    expandNonblockingCoreachabilityState(targettuple, targetpacked);    \
  }
#define UNPACK(target, targetpacked) \
  targetpacked = mStateSpace->get(target)

void ProductExplorer::
exploreNonblockingCoreachabilityStateDFS(uint32* targettuple,
                                         uint32* targetpacked)
{
  EXPLORE(TARGET, targettuple, targetpacked);
}

#undef EXPLORE
#undef EXPAND
#undef UNPACK

void ProductExplorer::
checkCoreachabilityState()
{
  uint32 source = mStateSpace->find();
  if (source != UNDEF_UINT32) {
    VISIT(source);
  }
}

#undef EXPLORE
#undef EXPAND
#undef UNPACK
#undef VISIT

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

void ProductExplorer::
doAbort()
  const
{
  throw jni::PreJavaException(jni::CLASS_AbortException);
}


//############################################################################
//# Class ProductExplorerFinalizer

class ProductExplorerFinalizer {

public:
  //##########################################################################
  //# Constructor & Destructor
  ProductExplorerFinalizer(const jni::NativeModelVerifierGlue& gchecker) :
    mNativeModelVerifier(gchecker), mProductExplorer(0)
  {
  }

  ~ProductExplorerFinalizer()
  {
    mNativeModelVerifier.setNativeModelAnalyser(0);
    delete mProductExplorer;
  }

  //##########################################################################
  //# Initialisation
  ProductExplorer* createProductExplorer
    (const jni::KindTranslatorGlue& translator,
     const jni::EventGlue& premarking,
     const jni::EventGlue& marking,
     jni::ClassCache& cache)
  {
    jni::ProductDESProxyFactoryGlue factory =
      mNativeModelVerifier.getFactoryGlue(&cache);
    jni::ProductDESGlue des = mNativeModelVerifier.getModelGlue(&cache);
    jni::ExplorerMode mode = mNativeModelVerifier.getExplorerModeGlue(&cache);
    if (mode == jni::ExplorerMode_NARROW) {
      mProductExplorer = new NarrowProductExplorer
        (factory, des, translator, premarking, marking, &cache);
    } else {
      mProductExplorer = new BroadProductExplorer
        (factory, des, translator, premarking, marking, &cache);
    }
    const int limit = mNativeModelVerifier.getNodeLimit();
    if (limit != UNDEF_INT32) {
      mProductExplorer->setStateLimit(limit);
    }
    const int tlimit = mNativeModelVerifier.getTransitionLimit();
    if (tlimit != UNDEF_INT32) {
      mProductExplorer->setTransitionLimit(tlimit);
    }
    JNIEnv* env = cache.getEnvironment();
    jobject bbuffer =
      env->NewDirectByteBuffer(mProductExplorer, sizeof(*mProductExplorer));
    mNativeModelVerifier.setNativeModelAnalyser(bbuffer);
    return mProductExplorer;
  }

private:
  //##########################################################################
  //# Data Members
  jni::NativeModelVerifierGlue mNativeModelVerifier;
  ProductExplorer* mProductExplorer;

};


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
      jni::KindTranslatorGlue translator =
        gchecker.getKindTranslatorGlue(&cache);
      jni::EventGlue nomarking(0, &cache);
      waters::ProductExplorerFinalizer finalizer(gchecker);
      waters::ProductExplorer* checker =
        finalizer.createProductExplorer(translator, nomarking,
                                        nomarking, cache);
      bool result = checker->runSafetyCheck();
      if (result) {
        jni::VerificationResultGlue vresult(result, 0, &cache);
        checker->addStatistics(vresult);
        return vresult.returnJavaObject();
      } else {
        jni::SafetyTraceGlue trace =
          checker->getSafetyCounterExample(gchecker);
        jni::VerificationResultGlue vresult(result, &trace, &cache);
        checker->addStatistics(vresult);
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
      jni::KindTranslatorGlue translator =
        gchecker.getKindTranslatorGlue(&cache);
      jni::EventGlue marking = gchecker.getUsedMarkingPropositionGlue(&cache);
      jni::EventGlue premarking =
        gchecker.getGeneralisedPreconditionGlue(&cache);
      waters::ProductExplorerFinalizer finalizer(gchecker);
      waters::ProductExplorer* checker =
        finalizer.createProductExplorer(translator, premarking,
                                        marking, cache);
      bool result = checker->runNonblockingCheck();
      if (result) {
        jni::VerificationResultGlue vresult(result, 0, &cache);
        checker->addStatistics(vresult);
        return vresult.returnJavaObject();
      } else {
        jni::ConflictTraceGlue trace =
          checker->getConflictCounterExample(gchecker);
        jni::VerificationResultGlue vresult(result, &trace, &cache);
        checker->addStatistics(vresult);
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


JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeModelAnalyser_requestAbort
  (JNIEnv *env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeModelAnalyserGlue gchecker(jchecker, &cache);
  jobject bbuffer = gchecker.getNativeModelAnalyser();
  waters::ProductExplorer* explorer =
    (waters::ProductExplorer*) env->GetDirectBufferAddress(bbuffer);
  explorer->requestAbort();
}
