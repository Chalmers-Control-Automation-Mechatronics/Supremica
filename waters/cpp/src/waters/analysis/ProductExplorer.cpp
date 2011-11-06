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

#ifdef __MINGW32__
#  include <windows.h>
#  include <psapi.h>
#else
#  include <sys/resource.h>
#endif

#include "jni/cache/ClassCache.h"
#include "jni/cache/JavaString.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/ConflictKindGlue.h"
#include "jni/glue/ConflictTraceGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/ExceptionGlue.h"
#include "jni/glue/ExplorerModeGlue.h"
#include "jni/glue/Glue.h"
#include "jni/glue/HashMapGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeConflictCheckerGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/OverflowExceptionGlue.h"
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

const uint32_t ProductExplorer::TAG_COREACHABLE = AutomatonEncoding::TAG0;


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
    mStateLimit(UINT32_MAX),
    mTransitionLimit(UINT32_MAX),
    mIsInitialUncontrollable(false),
    mIsAbortRequested(false),
    mEncoding(0),
    mStateSpace(0),
    mDepthMap(0),
    mReverseTransitionStore(0),
    mIsTrivial(false),
    mNumAutomata(0),
    mNumStates(0),
    mNumCoreachableStates(0),
    mNumTransitions(0),
    mDFSStack(0),
    mDFSStackSize(0),
    mTraceList(0),
    mTraceEvent(0),
    mTraceAutomaton(0),
    mTraceState(UINT32_MAX),
    mTraceLimit(UINT32_MAX),
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
      result = (mTraceAutomaton == 0);
    } else {
      storeInitialStates(true);
      result = doSafetySearch();
    }
    if (!result) {
      mTraceStartTime = clock();
      mTraceList = new jni::LinkedListGlue(mCache);
      if (mTraceAutomaton) {
        mJavaTraceAutomaton = mTraceAutomaton->getJavaAutomaton();
      }
      if (mTraceEvent == 0) {
        jni::TraceStepGlue step = mFactory.createTraceStepProxyGlue(0, mCache);
        mTraceList->add(&step);
      } else {
        mJavaTraceEvent = mTraceEvent->getJavaEvent();
        if (mTraceAutomaton) {
          const uint32_t* packed = mStateSpace->get(mTraceState);
          const uint32_t autindex = mTraceAutomaton->getAutomatonIndex();
          const uint32_t statecode = mEncoding->get(packed, autindex);
          mJavaTraceState = mTraceAutomaton->getJavaState(statecode);
        }
        jni::TraceStepGlue step =
          mFactory.createTraceStepProxyGlue(&mJavaTraceEvent, mCache);
        mTraceList->add(&step);
        const uint32_t level = mDepthMap->size() - 2;
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
    if (!mIsTrivial || mTraceState != UINT32_MAX) {
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
        const uint32_t level = getDepth(mTraceState);
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
#ifdef __MINGW32__
  /*
  PROCESS_MEMORY_COUNTERS counters;
  if (GetProcessMemoryInfo(GetCurrentProcess(), &counters,
                           sizeof(PROCESS_MEMORY_COUNTERS))) {
    jlong usage = counters.PeakWorkingSetSize;
    vresult.updatePeakMemoryUsage(usage);
  }
  */
#else
  struct rusage rusage;
  if (getrusage(RUSAGE_SELF, &rusage) == 0) {
    jlong usage = rusage.ru_maxrss << 10;
    vresult.updatePeakMemoryUsage(usage);
  }
#endif

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
  mNumStates = mNumCoreachableStates = mNumTransitions = 0;
  mTraceEvent = 0;
  mTraceAutomaton = 0;
  mTraceState = UINT32_MAX;
  mConflictKind = jni::ConflictKind_CONFLICT;
  switch (mMode) {
  case EXPLORER_MODE_SAFETY:
    if (mEncoding->hasSpecs()) {
      mStateSpace = new StateSpace(mEncoding, mStateLimit);
      mDepthMap = new ArrayList<uint32_t>(128);
    } else {
      setTrivial();
    }
    break;
  case EXPLORER_MODE_NONBLOCKING:
    if (mEncoding->isTriviallyNonblocking()) {
      setTrivial();
    } else if (mEncoding->isTriviallyBlocking()) {
      mStateSpace = new StateSpace(mEncoding, 1);
      mDepthMap = new ArrayList<uint32_t>(2);
      storeInitialStates(false, false);
      setTraceState(0);
      setTrivial();
    } else {
      mStateSpace = new TaggedStateSpace(mEncoding, mStateLimit);
      mDepthMap = new ArrayList<uint32_t>(128);
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
    uint32_t nextlevel = mNumStates;                              \
    uint32_t current = 0;                                         \
    uint32_t* currenttuple = new uint32_t[mNumAutomata];            \
    try {                                                       \
      while (current < mNumStates) {                            \
        checkAbort();                                           \
        uint32_t* currentpacked = mStateSpace->get(current);      \
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
  mDFSStack = new uint32_t[mDFSStackSize];
  mDFSStackPos = 0;
  mNumCoreachableStates = mPreMarking.isNull() ? 0 : UINT32_MAX;
  uint32_t* currenttuple = 0;
  bool overflow = false;
  if (mTransitionLimit == 0) {
    currenttuple = new uint32_t[mNumAutomata];
    setupReverseTransitionRelations();
  }
  for (uint32_t current = 0; current < mNumStates; current++) {
    checkAbort();
    uint32_t* currentpacked = mStateSpace->get(current);
    if (mEncoding->hasTag(currentpacked, TAG_COREACHABLE)) {
      continue;
    }
    if (mEncoding->isMarkedStateTuplePacked(currentpacked)) {
      mEncoding->setTag(currentpacked, TAG_COREACHABLE);
      if (mNumCoreachableStates != UINT32_MAX &&
          ++mNumCoreachableStates == mNumStates) {
        return true;
      }
      try {
        if (currenttuple == 0) { // storing
          exploreNonblockingCoreachabilityStateDFS(current);
        } else { // non-storing
          exploreNonblockingCoreachabilityStateDFS(currenttuple,
                                                   currentpacked);
        }
      } catch (const SearchAbort& abort) {
        delete [] currenttuple;
        return true;
      } catch (const DFSStackOverflow& abort) {
        mDFSStackPos = 0;
        overflow = true;
      }
    }
  }
  while (overflow) {
    overflow = false;
    try {
      for (uint32_t current = mNumStates; current-- > 0;) {
        checkAbort();
        uint32_t* currentpacked = mStateSpace->get(current);
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
      mDFSStackPos = 0;
      overflow = true;
    }
  }
  delete [] currenttuple;
  for (uint32_t current = 0; current < mNumStates; current++) {
    checkAbort();
    uint32_t* currentpacked = mStateSpace->get(current);
    if (!mEncoding->hasTag(currentpacked, TAG_COREACHABLE) &&
        mEncoding->isPreMarkedStateTuplePacked(currentpacked)) {
      mTraceState = current;
      return false;
    }
  }
  return true;
}

void ProductExplorer::
computeCounterExample(const jni::ListGlue& list, uint32_t level)
{
  if (level > 0) {
    setupReverseTransitionRelations();
  }
  uint32_t* buffers = new uint32_t[2 * mNumAutomata];
  try {
    jni::HashMapGlue statemap(mNumAutomata, mCache);
    uint32_t* sourcetuple = buffers;
    uint32_t* targettuple = &buffers[mNumAutomata];
    uint32_t* targetpacked = mStateSpace->get(mTraceState);
    mEncoding->decode(targetpacked, targettuple);
    while (level > 0) {
      checkAbort();
      mTraceLimit = mDepthMap->get(level);
      expandTraceState(targettuple, targetpacked);
      uint32_t* sourcepacked = mStateSpace->get(mTraceState);
      mEncoding->decode(sourcepacked, sourcetuple);
      storeNondeterministicTargets(sourcetuple, targettuple, statemap);
      const jni::EventGlue& event = mTraceEvent->getJavaEvent();
      jni::TraceStepGlue step =
        mFactory.createTraceStepProxyGlue(&event, &statemap, mCache);
      statemap.clear();
      list.add(0, &step);
      uint32_t* tmp = sourcetuple;
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
  uint32_t* initpacked = mStateSpace->prepare();
  if (initzero) {
    const int numwords = getAutomatonEncoding().getEncodingSize();
    for (int w = 0; w < numwords; w++) {
      initpacked[w] = 0;
    }
  } else {
    uint32_t* inittuple = new uint32_t[mNumAutomata];
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
      uint32_t popped = mDFSStack[--mDFSStackPos];                        \
      UNPACK(popped, targetpacked);                                     \
      EXPAND(popped, targettuple, targetpacked);                        \
    }                                                                   \
  }

#define UNPACK(target, targetpacked)

#define EXPAND(target, targettuple, targetpacked)                       \
  {                                                                     \
    uint32_t iter = mReverseTransitionStore->iterator(target);            \
    while (mReverseTransitionStore->hasNext(iter)) {                    \
      uint32_t source = mReverseTransitionStore->next(iter);              \
      VISIT(source);                                                    \
    }                                                                   \
  }

#define VISIT(source)                                                   \
  {                                                                     \
    checkAbort();                                                       \
    uint32_t* sourcepacked = mStateSpace->get(source);                    \
    if (!mEncoding->hasTag(sourcepacked, TAG_COREACHABLE)) {            \
      mEncoding->setTag(sourcepacked, TAG_COREACHABLE);                 \
      if (mNumCoreachableStates != UINT32_MAX &&                      \
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
exploreNonblockingCoreachabilityStateDFS(uint32_t target)
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
exploreNonblockingCoreachabilityStateDFS(uint32_t* targettuple,
                                         uint32_t* targetpacked)
{
  EXPLORE(TARGET, targettuple, targetpacked);
}

#undef EXPLORE
#undef EXPAND
#undef UNPACK

void ProductExplorer::
checkCoreachabilityState()
{
  uint32_t source = mStateSpace->find();
  if (source != UINT32_MAX) {
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
  uint32_t found = mStateSpace->find();
  if (found < mTraceLimit) {
    mTraceState = found;
    throw SearchAbort();
  }
}

uint32_t ProductExplorer::
getDepth(uint32_t state)
  const
{
  // return level such that depth[level] <= state < depth[level+1]
  uint32_t l = 0;
  uint32_t u = mDepthMap->size() - 1;
  while (l < u) {
    uint32_t m = (l + u) >> 1;
    uint32_t m1 = m + 1;
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
    finalize();
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
    if (limit >= 0) {
      mProductExplorer->setStateLimit(limit);
    }
    const int tlimit = mNativeModelVerifier.getTransitionLimit();
    if (tlimit >= 0) {
      mProductExplorer->setTransitionLimit(tlimit);
    }
    JNIEnv* env = cache.getEnvironment();
    jobject bbuffer =
      env->NewDirectByteBuffer(mProductExplorer, sizeof(*mProductExplorer));
    mNativeModelVerifier.setNativeModelAnalyzer(bbuffer);
    return mProductExplorer;
  }

  //##########################################################################
  //# Finalisation
  void finalize()
  {
    if (mProductExplorer != 0) {
      mNativeModelVerifier.setNativeModelAnalyzer(0);
      delete mProductExplorer;
      mProductExplorer = 0;
    }
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
  jni::ClassCache cache(env);
  jni::NativeSafetyVerifierGlue gchecker(jchecker, &cache);
  waters::ProductExplorerFinalizer finalizer(gchecker);
  try {
    jni::KindTranslatorGlue translator =
      gchecker.getKindTranslatorGlue(&cache);
    jni::EventGlue nomarking(0, &cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(translator, nomarking, nomarking, cache);
    bool initUncont = gchecker.isInitialUncontrollable();
    checker->setInitialUncontrollable(initUncont);
    bool result = checker->runSafetyCheck();
    jni::VerificationResultGlue vresult =
      gchecker.createAnalysisResultGlue(&cache);
    if (result) {
      vresult.setSatisfied(true);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    } else {
      jni::SafetyTraceGlue trace = checker->getSafetyCounterExample(gchecker);
      vresult.setCounterExample(&trace);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    }
  } catch (const std::bad_alloc& error) {
    finalizer.finalize();
    jni::OverflowExceptionGlue glue(jni::OverflowKind_MEMORY, &cache);
    cache.throwJavaException(glue);
    return 0;
  } catch (const jni::PreJavaException& pre) {
    finalizer.finalize();
    pre.throwJavaException(cache);
    return 0;
  } catch (const jni::ExceptionGlue& glue) {
    finalizer.finalize();
    cache.throwJavaException(glue);
    return 0;
  } catch (const jthrowable& throwable) {
    env->ExceptionClear();
    finalizer.finalize();
    env->Throw(throwable);
    return 0;
  }
}


JNIEXPORT jobject JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeConflictChecker_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeConflictCheckerGlue gchecker(jchecker, &cache);
  waters::ProductExplorerFinalizer finalizer(gchecker);
  try {
    jni::KindTranslatorGlue translator =
      gchecker.getKindTranslatorGlue(&cache);
    jni::EventGlue marking = gchecker.getUsedMarkingPropositionGlue(&cache);
    jni::EventGlue premarking = gchecker.getPreconditionMarkingGlue(&cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(translator, premarking, marking, cache);
    bool result = checker->runNonblockingCheck();
    jni::VerificationResultGlue vresult =
      gchecker.createAnalysisResultGlue(&cache);
    if (result) {
      vresult.setSatisfied(true);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    } else {
      jni::ConflictTraceGlue trace =
        checker->getConflictCounterExample(gchecker);
      vresult.setCounterExample(&trace);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    }
  } catch (const std::bad_alloc& error) {
    finalizer.finalize();
    jni::OverflowExceptionGlue glue(jni::OverflowKind_MEMORY, &cache);
    cache.throwJavaException(glue);
    return 0;
  } catch (const jni::PreJavaException& pre) {
    finalizer.finalize();
    pre.throwJavaException(cache);
    return 0;
  } catch (const jni::ExceptionGlue& glue) {
    finalizer.finalize();
    cache.throwJavaException(glue);
    return 0;
  } catch (const jthrowable& throwable) {
    env->ExceptionClear();
    finalizer.finalize();
    env->Throw(throwable);
    return 0;
  }
}


JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeModelAnalyzer_requestAbort
  (JNIEnv *env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeModelAnalyzerGlue gchecker(jchecker, &cache);
  jobject bbuffer = gchecker.getNativeModelAnalyzer();
  if (bbuffer != 0) {
    waters::ProductExplorer* explorer =
      (waters::ProductExplorer*) env->GetDirectBufferAddress(bbuffer);
    explorer->requestAbort();
  }
}
