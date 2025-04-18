//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
#include "jni/cache/PreAnalysisConfigurationException.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/CollectionGlue.h"
#include "jni/glue/ConflictCheckModeGlue.h"
#include "jni/glue/ConflictCounterExampleGlue.h"
#include "jni/glue/ConflictKindGlue.h"
#include "jni/glue/ExceptionGlue.h"
#include "jni/glue/Glue.h"
#include "jni/glue/HashMapGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/ListGlue.h"
#include "jni/glue/LoopCounterExampleGlue.h"
#include "jni/glue/NativeConflictCheckerGlue.h"
#include "jni/glue/NativeControlLoopCheckerGlue.h"
#include "jni/glue/NativeDeadlockCheckerGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/NativeStateCounterGlue.h"
#include "jni/glue/NativeVerificationResultGlue.h"
#include "jni/glue/OverflowExceptionGlue.h"
#include "jni/glue/SafetyCounterExampleGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/TraceGlue.h"
#include "jni/glue/TraceStepGlue.h"

#include "waters/analysis/EventRecord.h"
#include "waters/analysis/EventTreeProductExplorer.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# Class NonblockingTarjanCallBack
//############################################################################

class NonblockingTarjanCallBack : public TarjanCallBack
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NonblockingTarjanCallBack
    (TarjanStateSpace* tarjan, ProductExplorer* explorer) :
    TarjanCallBack(tarjan),
    mExplorer(*explorer)
  {
    int numAutomata = explorer->getNumberOfAutomata();
    mTupleBuffer = new uint32_t[numAutomata];
  }

  virtual ~NonblockingTarjanCallBack()
  {
    delete [] mTupleBuffer;
  }

  //##########################################################################
  //# Interface TarjanCallBack
  virtual bool isCriticalComponent(uint32_t start, uint32_t end)
  {
    TarjanStateSpace& tarjan = getTarjan();
    bool first = (tarjan.getNumberOfComponents() == 0);
    for (uint32_t pos = start; pos < end; pos++) {
      uint32_t state = tarjan.getStateOnComponentStack(pos);
      if (first) {
        // Only check markings for the first component ...
        const AutomatonEncoding& enc = mExplorer.getAutomatonEncoding();
        const uint32_t* tuplePacked = tarjan.get(state);
        if (enc.isMarkedStateTuplePacked(tuplePacked)) {
          return false;
        }
      } else {
        if (!mExplorer.closeNonblockingTarjanState(state, mTupleBuffer)) {
          return false;
        }
      }
    }
    return true;
  }

private:
  //##########################################################################
  //# Data Members
  ProductExplorer& mExplorer;
  uint32_t* mTupleBuffer;
};


//############################################################################
//# Class ControlLoopTarjanCallBack
//############################################################################

class ControlLoopTarjanCallBack : public TarjanCallBack
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ControlLoopTarjanCallBack
    (TarjanStateSpace* tarjan, ProductExplorer* explorer) :
    TarjanCallBack(tarjan),
    mExplorer(*explorer)
  {
    int numAutomata = explorer->getNumberOfAutomata();
    mTupleBuffer = new uint32_t[numAutomata];
  }

  virtual ~ControlLoopTarjanCallBack()
  {
    delete [] mTupleBuffer;
  }

  //##########################################################################
  //# Interface TarjanCallBack
  virtual bool isCriticalComponent(uint32_t start, uint32_t end)
  {
    TarjanStateSpace& tarjan = getTarjan();
    if (end - start == 1) {
      uint32_t state = tarjan.getStateOnComponentStack(start);
      bool exhausted = mExplorer.expandForwardAgainIncludingSelfloops
        (state, mTupleBuffer, 
         &ProductExplorer::checkControlLoopTarjanTransitionForSelfloop);
      return !exhausted;
    } else {
      return true;
    }
  }

private:
  //##########################################################################
  //# Data Members
  ProductExplorer& mExplorer;
  uint32_t* mTupleBuffer;
};


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
    mConflictCheckMode(jni::ConflictCheckMode_STORED_BACKWARDS_TRANSITIONS),
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
    mNumTransitionsExplored(0),
    mDFSStack(0),
    mDFSStackSize(0),
    mTraceList(0),
    mTraceEvent(0),
    mTraceAutomaton(0),
    mTraceState(UINT32_MAX),
    mTarjanTraceSuccessors(0),
    mJavaTraceEvent(0, cache),
    mJavaTraceAutomaton(0, cache),
    mJavaTraceState(0, cache),
    mConflictKind(jni::ConflictKind_CONFLICT),
    mLoopIndex(-1)
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
  delete mTarjanTraceSuccessors;
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
    mCheckType = CHECK_TYPE_SAFETY;
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
        computeBFSCounterExample(*mTraceList, level);
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
    mCheckType = CHECK_TYPE_NONBLOCKING;
    setup();
    if (!mIsTrivial || mTraceState != UINT32_MAX) {
      bool tarjan = false;
      if (mIsTrivial) {
        result = false;
        // mDeadlock = "transitions enabled in mTraceState";
      } else {
        storeInitialStates(false);
        switch (mConflictCheckMode) {
        case jni::ConflictCheckMode_STORED_BACKWARDS_TRANSITIONS:
        case jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS:
          result = doNonblockingReachabilitySearch();
          if (result) {
            mConflictKind = jni::ConflictKind_LIVELOCK;
            result = doNonblockingCoreachabilitySearch();
          }
          break;
        case jni::ConflictCheckMode_NO_BACKWARDS_TRANSITIONS:
          result = doNonblockingTarjanSearch();
          tarjan = true;
          break;
        default:
          throw jni::PreAnalysisConfigurationException(mConflictCheckMode);
          break;
        }
      }
      if (!result) {
        mTraceStartTime = clock();
        mTraceList = new jni::LinkedListGlue(mCache);
        if (tarjan) {
          computeNonblockingTarjanCounterExample(*mTraceList);
        } else {
          const uint32_t level = getDepth(mTraceState);
          computeBFSCounterExample(*mTraceList, level);
        }
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
runDeadlockCheck()
{
  try {
    mStartTime = clock();
    mCheckType = CHECK_TYPE_DEADLOCK;
    setup();
    bool result = true;
    if (!mIsTrivial) {
      storeInitialStates(true);
      result = doDeadlockSearch();
      if (!result) {
        mTraceStartTime = clock();
        mTraceList = new jni::LinkedListGlue(mCache);
        const uint32_t level = getDepth(mTraceState);
        computeBFSCounterExample(*mTraceList, level);
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
runLoopCheck()
{
  try {
    // const jni::JavaString name(mCache->getEnvironment(), mModel.getName());
    // std::cerr << (const char*) name << std::endl;
    mStartTime = clock();
    bool result = true;
    mCheckType = CHECK_TYPE_LOOP;
    setup();
    if (mIsTrivial) {
      if (mTraceEvent == 0) {
        result = true;
      } else {
        storeInitialStates(false, false);
        uint32_t* initTuple = mStateSpace->get(0);
        jni::HashMapGlue stateMap(mNumAutomata, mCache);
        mEncoding->storeNondeterministicInitialStates(initTuple, stateMap);
        mTraceList = new jni::LinkedListGlue(mCache);
        jni::TraceStepGlue step0 =
          mFactory.createTraceStepProxyGlue(0, &stateMap, mCache);
        mTraceList->add(&step0);
        mJavaTraceEvent = mTraceEvent->getJavaEvent();
        jni::TraceStepGlue step1 =
          mFactory.createTraceStepProxyGlue(&mJavaTraceEvent, mCache);
        mTraceList->add(&step1);
        mLoopIndex = 0;
        result = false;
      }
    } else {
      storeInitialStates(false);
      result = doControlLoopTarjanSearch();
      if (!result) {
        mTraceStartTime = clock();
        mTraceList = new jni::LinkedListGlue(mCache);
        computeLoopCounterExample(*mTraceList);
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

void ProductExplorer::
runStateCount()
{
  try {
    mStartTime = clock();
    mCheckType = CHECK_TYPE_COUNT;
    setup();
    storeInitialStates(true);
    doSafetySearch();
    teardown();
    mStopTime = clock();
  } catch (...) {
    teardown();
    throw;
  }
}


jni::SafetyCounterExampleGlue ProductExplorer::
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
  const jni::TraceGlue trace =
    mFactory.createTraceProxyGlue(mTraceList, mCache);
  return mFactory.createSafetyCounterExampleProxyGlue
    (name, comment, 0, &mModel, &automata, &trace, mCache);
}


jni::ConflictCounterExampleGlue ProductExplorer::
getConflictCounterExample(const jni::NativeModelVerifierGlue& gchecker)
  const
{
  jstring name = gchecker.getTraceName();
  const jni::SetGlue automata = mModel.getAutomataGlue(mCache);
  const jni::TraceGlue trace =
    mFactory.createTraceProxyGlue(mTraceList, mCache);
  const jni::ConflictKindGlue kind(mConflictKind, mCache);
  return mFactory.createConflictCounterExampleProxyGlue
    (name, 0, 0, &mModel, &automata, &trace, &kind, mCache);
}


jni::LoopCounterExampleGlue ProductExplorer::
getLoopCounterExample(const jni::NativeControlLoopCheckerGlue& gchecker)
  const
{
  jstring name = gchecker.getTraceName();
  const jni::SetGlue automata = mModel.getAutomataGlue(mCache);
  const jni::TraceGlue trace =
    mFactory.createTraceProxyGlue(mTraceList, mLoopIndex, mCache);
  return mFactory.createLoopCounterExampleProxyGlue
    (name, 0, 0, &mModel, &automata, &trace, mCache);
}


void ProductExplorer::
addStatistics(const jni::NativeVerificationResultGlue& vresult)
  const
{
  vresult.setNumberOfAutomata(mNumAutomata);
  vresult.setNumberOfStates(mNumStates);
  vresult.setNumberOfTransitions(mNumTransitions);
  vresult.setNumberOfExploredTransitions(mNumTransitionsExplored);
  vresult.setPeakNumberOfNodes(mNumStates);
  if (mEncoding == 0) {
    vresult.setEncodingSize(0);
  } else {
    vresult.setEncodingSize(mEncoding->getNumberOfEncodedBits());
  }
  if (mStateSpace != 0) {
    mStateSpace->addStatistics(vresult);
  }
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
  int numTags;
  if (mCheckType == CHECK_TYPE_NONBLOCKING &&
      mConflictCheckMode != jni::ConflictCheckMode_NO_BACKWARDS_TRANSITIONS) {
    numTags = 1;
  } else {
    numTags = 0;
  }
  mEncoding =
    new AutomatonEncoding(mModel, mKindTranslator,
                          mPreMarking, mMarking, mCache, numTags);
  // mEncoding->dump();
  mNumAutomata = mEncoding->getNumberOfAutomata();
  mNumStates = mNumCoreachableStates = mNumTransitions = 0;
  mTraceEvent = 0;
  mTraceAutomaton = 0;
  mTraceState = UINT32_MAX;
  mConflictKind = jni::ConflictKind_CONFLICT;
  switch (mCheckType) {
  case CHECK_TYPE_SAFETY:
    if (mEncoding->hasSpecs()) {
      mStateSpace = new StateSpace(mEncoding, mStateLimit);
      mDepthMap = new ArrayList<uint32_t>(128);
    } else {
      setTrivial();
    }
    break;
  case CHECK_TYPE_NONBLOCKING:
    if (mEncoding->isTriviallyNonblocking()) {
      setTrivial();
    } else if (mEncoding->isTriviallyBlocking()) {
      mStateSpace = new StateSpace(mEncoding, 1);
      mDepthMap = new ArrayList<uint32_t>(2);
      storeInitialStates(false, false);
      setTraceState(0);
      setTrivial();
    } else {
      switch (mConflictCheckMode) {
      case jni::ConflictCheckMode_STORED_BACKWARDS_TRANSITIONS:
        mReverseTransitionStore = new ReverseTransitionStore(mTransitionLimit);
        // fall through ...
      case jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS:
        mStateSpace = new TaggedStateSpace(mEncoding, mStateLimit);
        mDepthMap = new ArrayList<uint32_t>(128);
        break;
      case jni::ConflictCheckMode_NO_BACKWARDS_TRANSITIONS:
        mStateSpace = new TarjanStateSpace(mEncoding, mStateLimit);
        mDepthMap = new ArrayList<uint32_t>(2); // for number of init states
        break;
      default:
        throw jni::PreAnalysisConfigurationException(mConflictCheckMode);
        break;
      }
    }
    break;
  case CHECK_TYPE_LOOP:
    mStateSpace = new TarjanStateSpace(mEncoding, mStateLimit);
    mDepthMap = new ArrayList<uint32_t>(2); // for number of init states
    break;
  case CHECK_TYPE_COUNT:
  case CHECK_TYPE_DEADLOCK:
    mStateSpace = new StateSpace(mEncoding, mStateLimit);
    mDepthMap = new ArrayList<uint32_t>(128);
    break;
  default:
    throw jni::PreAnalysisConfigurationException(mCheckType);
    break;
  }
}


void ProductExplorer::
teardown()
{
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
  delete mTarjanTraceSuccessors;
  mTarjanTraceSuccessors = 0;
}


void ProductExplorer::
doAbort()
  const
{
  throw jni::PreJavaException(jni::CLASS_NativeAbortException);
}



//############################################################################
//# ProductExplorer: Search Algorithms

//----------------------------------------------------------------------------
// doSafetySearch()

#define DO_REACHABILITY(callBack)                                 \
  {                                                               \
    uint32_t nextlevel = mNumStates;                              \
    uint32_t current = 0;                                         \
    uint32_t* currenttuple = new uint32_t[mNumAutomata];          \
    try {                                                         \
      while (current < mNumStates) {                              \
        checkAbort();                                             \
        uint32_t* currentpacked = mStateSpace->get(current);      \
        mEncoding->decode(currentpacked, currenttuple);           \
        if (!EXPAND(current, currenttuple, currentpacked, callBack)) {  \
          mTraceState = current;                                  \
          delete [] currenttuple;                                 \
          return false;                                           \
        } else if (++current == nextlevel) {                      \
          nextlevel = mNumStates;                                 \
          mDepthMap->add(nextlevel);                              \
        }                                                         \
      }                                                           \
      delete [] currenttuple;                                     \
      return true;                                                \
    } catch (...) {                                               \
      delete [] currenttuple;                                     \
      throw;                                                      \
    }                                                             \
  }


bool ProductExplorer::
doSafetySearch()
{    
# define EXPAND expandForwardSafety
  DO_REACHABILITY(0);
# undef EXPAND
}


//----------------------------------------------------------------------------
// doDeadlockSearch()

bool ProductExplorer::
doDeadlockSearch()
{
# define EXPAND expandForwardDeadlock
  DO_REACHABILITY(0);
# undef EXPAND
}


//----------------------------------------------------------------------------
// doNonblockingReachabilitySearch()

bool ProductExplorer::
doNonblockingReachabilitySearch()
{
  TransitionCallBack callBack;
  switch (getConflictCheckMode()) {
  case jni::ConflictCheckMode_STORED_BACKWARDS_TRANSITIONS:
    callBack = &ProductExplorer::addCoreachabilityTransition;
    break;
  case jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS:
    callBack = &ProductExplorer::rememberNonDeadlockTransition;
    break;
  default:
    throw jni::PreAnalysisConfigurationException(getConflictCheckMode());
    break;
  }
# define EXPAND expandNonblockingReachabilityState
  DO_REACHABILITY(callBack);
# undef EXPAND
}

bool ProductExplorer::
expandNonblockingReachabilityState
  (uint32_t source, const uint32_t* sourceTuple,
   const uint32_t* sourcePacked, TransitionCallBack callBack)
{
  setConflictKind(jni::ConflictKind_DEADLOCK);
  if (isLocalDumpState(sourceTuple)) {
    return false;
  }
  expandForward(source, sourceTuple, sourcePacked, callBack);
  if (getConflictKind() != jni::ConflictKind_DEADLOCK) {
    return true;
  } else if (getAutomatonEncoding().isMarkedStateTuple(sourceTuple) ||
             !getAutomatonEncoding().isPreMarkedStateTuple(sourceTuple)) {
    setConflictKind(jni::ConflictKind_CONFLICT);
    return true;
  } else {
    return false;
  }
}

bool ProductExplorer::rememberNonDeadlockTransition
  (uint32_t source, const waters::EventRecord* event, uint32_t target)
{
  setConflictKind(jni::ConflictKind_CONFLICT);
  return true;
}

bool ProductExplorer::addCoreachabilityTransition
  (uint32_t source, const waters::EventRecord* event, uint32_t target)
{
  setConflictKind(jni::ConflictKind_CONFLICT);
  mReverseTransitionStore->addTransition(source, target);
  return true;
}


//----------------------------------------------------------------------------
// doNonblockingCoreachabilitySearch()

bool ProductExplorer::
doNonblockingCoreachabilitySearch()
{
  delete [] mDFSStack;
  mDFSStackSize = mNumStates < DFS_STACK_SIZE ? mNumStates : DFS_STACK_SIZE;
  mDFSStack = new uint32_t[mDFSStackSize];
  mDFSStackPos = 0;
  mNumCoreachableStates = mPreMarking.isNull() ? 0 : UINT32_MAX;
  uint32_t* currentTuple = 0;
  bool overflow = false;
  if (mConflictCheckMode ==
      jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS) {
    currentTuple = new uint32_t[mNumAutomata];
    setupReverseTransitionRelations();
  }
  for (uint32_t current = 0; current < mNumStates; current++) {
    checkAbort();
    uint32_t* currentPacked = mStateSpace->get(current);
    if (mEncoding->hasTag(currentPacked, TAG_COREACHABLE)) {
      continue;
    }
    if (mEncoding->isMarkedStateTuplePacked(currentPacked)) {
      mEncoding->setTag(currentPacked, TAG_COREACHABLE);
      if (mNumCoreachableStates != UINT32_MAX &&
          ++mNumCoreachableStates == mNumStates) {
        return true;
      }
      try {
        if (currentTuple == 0) { // storing
          exploreNonblockingCoreachabilityStateDFS(current);
        } else { // non-storing
          exploreNonblockingCoreachabilityStateDFS
            (current, currentTuple, currentPacked);
        }
      } catch (const SearchAbort& abort) {
        delete [] currentTuple;
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
        uint32_t* currentPacked = mStateSpace->get(current);
        if (mEncoding->hasTag(currentPacked, TAG_COREACHABLE)) {
          if (currentTuple == 0) {
            exploreNonblockingCoreachabilityStateDFS(current);
          } else {
            exploreNonblockingCoreachabilityStateDFS
              (current, currentTuple, currentPacked);
          }
        }
      }
    } catch (const SearchAbort& abort) {
      delete [] currentTuple;
      return true;
    } catch (const DFSStackOverflow& abort) {
      mDFSStackPos = 0;
      overflow = true;
    }
  }
  delete [] currentTuple;
  for (uint32_t current = 0; current < mNumStates; current++) {
    checkAbort();
    uint32_t* currentPacked = mStateSpace->get(current);
    if (!mEncoding->hasTag(currentPacked, TAG_COREACHABLE) &&
        mEncoding->isPreMarkedStateTuplePacked(currentPacked)) {
      mTraceState = current;
      return false;
    }
  }
  return true;
}


#define EXPLORE(target, targetTuple, targetPacked)                      \
  {                                                                     \
    EXPAND(target, targetTuple, targetPacked);                          \
    while (mDFSStackPos > 0) {                                          \
      uint32_t popped = mDFSStack[--mDFSStackPos];                      \
      UNPACK(popped, targetPacked);                                     \
      EXPAND(popped, targetTuple, targetPacked);                        \
    }                                                                   \
  }

#define UNPACK(target, targetPacked)

#define EXPAND(target, targetTuple, targetPacked)                       \
  {                                                                     \
    uint32_t iter = mReverseTransitionStore->iterator(target);          \
    while (mReverseTransitionStore->hasNext(iter)) {                    \
      uint32_t source = mReverseTransitionStore->next(iter);            \
      processCoreachabilityTransition(target, 0, source);               \
    }                                                                   \
  }

void ProductExplorer::
exploreNonblockingCoreachabilityStateDFS(uint32_t target)
{
  EXPLORE(target, TUPLE, PACKED);
}

#undef EXPAND
#undef UNPACK


#define EXPAND(target, targetTuple, targetPacked)                       \
  {                                                                     \
    mEncoding->decode(targetPacked, targetTuple);                       \
    expandReverse(target, targetTuple, targetPacked,                    \
                  &ProductExplorer::processCoreachabilityTransition);   \
  }
#define UNPACK(target, targetPacked) \
  targetPacked = mStateSpace->get(target)

void ProductExplorer::
exploreNonblockingCoreachabilityStateDFS(uint32_t target,
                                         uint32_t* targetTuple,
                                         uint32_t* targetPacked)
{
  EXPLORE(target, targetTuple, targetPacked);
}

#undef EXPLORE
#undef EXPAND
#undef UNPACK


bool ProductExplorer::
processCoreachabilityTransition(uint32_t target,
                                const EventRecord* event,
                                uint32_t source)
{
  checkAbort();
  if (source != UINT32_MAX) {
    uint32_t* sourcePacked = mStateSpace->get(source);
    if (!mEncoding->hasTag(sourcePacked, TAG_COREACHABLE)) {
      mEncoding->setTag(sourcePacked, TAG_COREACHABLE);
      if (mNumCoreachableStates != UINT32_MAX &&
          ++mNumCoreachableStates == mNumStates) {
        throw SearchAbort();
      } else if (mDFSStackPos < mDFSStackSize) {
        mDFSStack[mDFSStackPos++] = source;
      } else {
        throw DFSStackOverflow();
      }
    }
  }
  return true;
}


//----------------------------------------------------------------------------
// doNonblockingTarjanSearch()

bool ProductExplorer::
doNonblockingTarjanSearch()
{
  uint32_t numInit = getNumberOfInitialStates();
  for (uint32_t root = 0; root < numInit; root++) {
    if (!doNonblockingTarjanSearch(root)) {
      return false;
    }
  }
  return true;
}


bool ProductExplorer::
doNonblockingTarjanSearch(uint32_t root)
{
  TarjanStateSpace* tarjan = (TarjanStateSpace*) mStateSpace;
  if (!tarjan->isOpenState(root)) {
    return true;
  }
  // std::cerr << "n=" << mStateSpace->size() << std::endl;
  NonblockingTarjanCallBack callBack(tarjan, this);
  uint32_t* sourceTuple = new uint32_t[mNumAutomata];
  try {
    tarjan->pushRootControlState(root);
    do {
      checkAbort();
      // tarjan->dumpControlStack();
      if (tarjan->isTopControlStateClosing()) {
        // std::cerr << "c" << tarjan->getTopControlState() << std::endl;
        tarjan->mayBeCloseComponent(&callBack);
        tarjan->popControlState();
        switch (tarjan->getCriticalComponentSize()) {
        case 0: // no critical component - continue ...
          break;
        case 1: // deadlock
          setConflictKind(jni::ConflictKind_DEADLOCK);
          return false;
        default: // livelock
          setConflictKind(jni::ConflictKind_LIVELOCK);
          return false;
        }
      } else {
        // std::cerr << "e" << tarjan->getTopControlState() << std::endl;
        uint32_t source = tarjan->beginStateExpansion();
        uint32_t* sourcePacked = mStateSpace->get(source);
        mEncoding->decode(sourcePacked, sourceTuple);
        if (!isLocalDumpState(sourceTuple)) {
          expandForward(source, sourceTuple, sourcePacked,
                        &ProductExplorer::processNonblockingTarjanTransition);
        }
        // The above calls tarjan->processTransition(source, target) ...
        tarjan->endStateExpansion();
        // std::cerr << "n=" << mStateSpace->size() << std::endl;
      }
    } while (!tarjan->isControlStackEmpty());
    return true;
  } catch (...) {
    delete[] sourceTuple;
    throw;
  }
}

bool ProductExplorer::
processNonblockingTarjanTransition(uint32_t source,
                                   const EventRecord* event,
                                   uint32_t target)
{
  // std::cerr << "  " << source << "->" << target << std::endl;
  TarjanStateSpace* tarjan = (TarjanStateSpace*) mStateSpace;
  tarjan->processTransition(source, target);
  return true;
}

bool ProductExplorer::
closeNonblockingTarjanState(uint32_t source, uint32_t* sourceTuple)
{
  uint32_t* sourcePacked = getStateSpace().get(source);
  getAutomatonEncoding().decode(sourcePacked, sourceTuple);
  if (isLocalDumpState(sourceTuple)) {
    return true; // continue checking states - results in critical component
  } else if (getAutomatonEncoding().isMarkedStateTuple(sourceTuple)) {
    return false; // stop checking states
  } else {
    return expandForwardAgain
      (source, sourceTuple, sourcePacked,
       &ProductExplorer::closeNonblockingTarjanTransition);
    // expandForwardAgain() returns false when stopping early
  }
}

bool ProductExplorer::
closeNonblockingTarjanTransition(uint32_t source,
                                 const EventRecord* event,
                                 uint32_t target)
{
  TarjanStateSpace* tarjan = (TarjanStateSpace*) mStateSpace;
  return !tarjan->isClosedState(target);
  // false return value means to stop checking
}


//----------------------------------------------------------------------------
// doNonblockingTarjanSearch()

bool ProductExplorer::
doControlLoopTarjanSearch()
{
  for (uint32_t root = 0; root < mNumStates; root++) {
    if (!doControlLoopTarjanSearch(root)) {
      return false;
    }
  }
  return true;
}


bool ProductExplorer::
doControlLoopTarjanSearch(uint32_t root)
{
  TarjanStateSpace* tarjan = (TarjanStateSpace*) mStateSpace;
  if (!tarjan->isOpenState(root)) {
    return true;
  }

  ControlLoopTarjanCallBack callBack(tarjan, this);
  uint32_t* sourceTuple = new uint32_t[mNumAutomata];
  try {
    tarjan->pushRootControlState(root);
    do {
      checkAbort();
      // tarjan->dumpControlStack();
      if (tarjan->isTopControlStateClosing()) {
        // std::cerr << "c" << tarjan->getTopControlState() << std::endl;
        tarjan->mayBeCloseComponent(&callBack);
        tarjan->popControlState();
        if (tarjan->getCriticalComponentSize() > 0) {
          return false;
        }
      } else {
        // std::cerr << "e" << tarjan->getTopControlState() << std::endl;
        uint32_t source = tarjan->beginStateExpansion();
        uint32_t* sourcePacked = mStateSpace->get(source);
        mEncoding->decode(sourcePacked, sourceTuple);
        expandForward(source, sourceTuple, sourcePacked,
                      &ProductExplorer::processControlLoopTarjanTransition);
        // The above calls tarjan->processTransition(source, target) ...
        tarjan->endStateExpansion();
        // std::cerr << "n=" << mStateSpace->size() << std::endl;
      }
    } while (!tarjan->isControlStackEmpty());
    return true;
  } catch (...) {
    delete[] sourceTuple;
    throw;
  }
}

bool ProductExplorer::
processControlLoopTarjanTransition(uint32_t source,
                                   const EventRecord* event,
                                   uint32_t target)
{
  // std::cerr << "  " << source << " -" << event->getName() << "-> "
  //           << target << std::endl;
  if (event->isControllable()) {
    TarjanStateSpace* tarjan = (TarjanStateSpace*) mStateSpace;
    tarjan->processTransition(source, target);
  }
  return true;
}

bool ProductExplorer::
checkControlLoopTarjanTransitionForSelfloop(uint32_t source,
                                            const EventRecord* event,
                                            uint32_t target)
{
  // return false on controllable selfloop - stop searching
  return !event->isControllable() || source != target;
}


//----------------------------------------------------------------------------
// computeBFSCounterExample()

void ProductExplorer::
computeBFSCounterExample(const jni::ListGlue& list, uint32_t level)
{
  if (mReverseTransitionStore == 0 && level > 0) {
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
      if (mReverseTransitionStore == 0) {
        expandTraceState(mTraceState, targettuple, targetpacked, level);
      } else {
        mTraceState = mReverseTransitionStore->getFirstPredecessor(mTraceState);
      }
      uint32_t* sourcepacked = mStateSpace->get(mTraceState);
      mEncoding->decode(sourcepacked, sourcetuple);
      if (mReverseTransitionStore != 0) {
        mTraceEvent =
          findEvent(mTraceState, sourcepacked, sourcetuple, targetpacked);
      }
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


//----------------------------------------------------------------------------
// compute***TarjanCounterExample()

class TarjanTraceFinder
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanTraceFinder(ProductExplorer& explorer) :
    mExplorer(explorer),
    mTarjan((TarjanStateSpace&) explorer.getStateSpace()),
    mList1(new BlockedArrayList<uint32_t>()),
    mList2(new BlockedArrayList<uint32_t>()),
    mBuffers(new uint32_t[2 * explorer.getNumberOfAutomata()]),
    mSourceTuple(mBuffers),
    mTargetTuple(&mBuffers[explorer.getNumberOfAutomata()]),
    mCriticalState(UINT32_MAX)
  {
  }

  virtual ~TarjanTraceFinder()
  {
    delete mList1;
    delete mList2;
    delete [] mBuffers;
  }

  //##########################################################################
  //# Simple Access
  inline ProductExplorer& getExplorer() const {return mExplorer;}
  inline TarjanStateSpace& getStateSpace() const {return mTarjan;}

  //##########################################################################
  //# Algorithm
  uint32_t appendCounterExample(const jni::ListGlue& traceList)
  {
    mExplorer.setTraceState(UINT32_MAX);
    collectStartStates(mList1);

    AutomatonEncoding& encoding = mExplorer.getAutomatonEncoding();
    while (mCriticalState == UINT32_MAX) {
      for (uint32_t i = 0; i < mList1->size(); i++) {
        uint32_t source = mList1->get(i);
        uint32_t* sourcePacked = mTarjan.get(source);
        encoding.decode(sourcePacked, mSourceTuple);
        mCriticalState =
          expandState(source, mSourceTuple, sourcePacked, mList2);
        if (mCriticalState != UINT32_MAX) {
          break;
        }
      }
      mList1->clear();
      BlockedArrayList<uint32_t>* tmp = mList1;
      mList1 = mList2;
      mList2 = tmp;
    }

    jni::ClassCache* cache = mExplorer.getCache();
    jni::ProductDESProxyFactoryGlue& factory = mExplorer.getFactory();
    int numAutomata = mExplorer.getNumberOfAutomata();
    uint32_t currentState = mCriticalState;
    const EventRecord* currentEvent = mExplorer.getTraceEvent();
    jni::HashMapGlue stateMap(numAutomata, cache);
    uint32_t* targetPacked = mTarjan.get(mCriticalState);
    encoding.decode(targetPacked, mTargetTuple);
    int count = 0;
    while (!isStartState(currentState, count++)) {
      uint32_t source = mTarjan.getTraceStatus(currentState);
      uint32_t* sourcePacked = mTarjan.get(source);
      encoding.decode(sourcePacked, mSourceTuple);
      if (currentEvent == 0) {
        currentEvent =
          mExplorer.findEvent(source, sourcePacked, mSourceTuple, targetPacked);
      }
      mExplorer.setTraceEvent(currentEvent);
      mExplorer.storeNondeterministicTargets
        (mSourceTuple, mTargetTuple, stateMap);
      const jni::EventGlue& event = currentEvent->getJavaEvent();
      jni::TraceStepGlue step =
        factory.createTraceStepProxyGlue(&event, &stateMap, cache);
      traceList.add(0, &step);
      stateMap.clear();
      uint32_t* tmp = mSourceTuple;
      mSourceTuple = mTargetTuple;
      mTargetTuple = tmp;
      targetPacked = sourcePacked;
      currentState = source;
      currentEvent = 0;
    }
    prependInitialState(mTargetTuple, stateMap, traceList);

    return mCriticalState;
  }

  //##########################################################################
  //# Callbacks
  virtual void collectStartStates(BlockedArrayList<uint32_t>* list)
  {
    uint32_t numInit = mExplorer.getNumberOfInitialStates();
    mTarjan.setUpTraceSearch(numInit);
    for (uint32_t s = 0; s < numInit; s++) {
      if (mTarjan.getTraceStatus(s) == TarjanStateSpace::TR_CRITICAL) {
        mCriticalState = s;
        break;
      } else {
        list->add(s);
      }
    }
  }

  virtual uint32_t expandState(uint32_t source,
                               uint32_t* sourceTuple,
                               const uint32_t* sourcePacked,
                               BlockedArrayList<uint32_t>* successors) = 0;

  virtual bool isStartState(uint32_t state, int count)
  {
    return state < mExplorer.getNumberOfInitialStates();
  }

  virtual void prependInitialState(const uint32_t* tuple,
                                   const jni::HashMapGlue& stateMap,
                                   const jni::ListGlue& traceList)
  {
    jni::ClassCache* cache = mExplorer.getCache();
    jni::ProductDESProxyFactoryGlue& factory = mExplorer.getFactory();
    AutomatonEncoding& encoding = mExplorer.getAutomatonEncoding();
    encoding.storeNondeterministicInitialStates(tuple, stateMap);
    jni::TraceStepGlue step =
      factory.createTraceStepProxyGlue(0, &stateMap, cache);
    traceList.add(0, &step);
  }

private:
  //##########################################################################
  //# Data Members
  ProductExplorer& mExplorer;
  TarjanStateSpace& mTarjan;
  BlockedArrayList<uint32_t>* mList1;
  BlockedArrayList<uint32_t>* mList2;
  uint32_t* mBuffers;
  uint32_t* mSourceTuple;
  uint32_t* mTargetTuple;
  uint32_t mCriticalState;
};


//----------------------------------------------------------------------------
// computeNonblockingTarjanCounterExample()

class NonblockingTarjanTraceFinder : public TarjanTraceFinder
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NonblockingTarjanTraceFinder(ProductExplorer& explorer) :
    TarjanTraceFinder(explorer)
  {}

  //##########################################################################
  //# Callbacks
  virtual uint32_t expandState(uint32_t source,
                               uint32_t* sourceTuple,
                               const uint32_t* sourcePacked,
                               BlockedArrayList<uint32_t>* successors)
  {
    ProductExplorer& explorer = getExplorer();
    explorer.expandNonblockingTarjanTraceState(source, sourceTuple,
                                               sourcePacked, successors);
    return explorer.getTraceState();
  }
};


void ProductExplorer::
computeNonblockingTarjanCounterExample(const jni::ListGlue& list)
{
  NonblockingTarjanTraceFinder finder(*this);
  finder.appendCounterExample(list);
  mTarjanTraceSuccessors = 0;
}

void ProductExplorer::
expandNonblockingTarjanTraceState(uint32_t source,
                                  const uint32_t* sourceTuple,
                                  const uint32_t* sourcePacked,
                                  BlockedArrayList<uint32_t>* successors)
{
  mGotTarjanTraceSuccessor = false;
  mTarjanTraceSuccessors = successors;
  if (!isLocalDumpState(sourceTuple)) {
    expandForwardAgain(source, sourceTuple, sourcePacked,
                       &ProductExplorer::processTarjanTraceTransition);
  }
  if (!mGotTarjanTraceSuccessor &&
      !getAutomatonEncoding().isMarkedStateTuple(sourceTuple)) {
    setTraceState(source);
    setTraceEvent(0);
    setConflictKind(jni::ConflictKind_DEADLOCK);
  }
}

bool ProductExplorer::
processTarjanTraceTransition(uint32_t source,
                             const EventRecord* event,
                             uint32_t target)
{
  mGotTarjanTraceSuccessor = true;
  if (target != UINT32_MAX) {
    TarjanStateSpace* tarjan = (TarjanStateSpace*) mStateSpace;
    uint32_t& ref = tarjan->getTraceStatusRef(target);
    switch (ref) {
    case TarjanStateSpace::TR_OPEN:
      mTarjanTraceSuccessors->add(target);
      ref = source;
      break;
    case TarjanStateSpace::TR_CRITICAL:
      ref = source;
      setTraceState(target);
      setTraceEvent(event);
      return false;
    default:
      break;
    }
  }
  return true;
}


//----------------------------------------------------------------------------
// computeLoopCounterExample()

class LoopEntryTraceFinder : public TarjanTraceFinder
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit LoopEntryTraceFinder(ProductExplorer& explorer) :
    TarjanTraceFinder(explorer)
  {}

  //##########################################################################
  //# Callbacks
  virtual uint32_t expandState(uint32_t source,
                               uint32_t* sourceTuple,
                               const uint32_t* sourcePacked,
                               BlockedArrayList<uint32_t>* successors)
  {
    ProductExplorer& explorer = getExplorer();
    explorer.setTarjanTraceSuccessors(successors);
    explorer.expandForwardAgain(source, sourceTuple, sourcePacked,
                                &ProductExplorer::processTarjanTraceTransition);
    return explorer.getTraceState();
  }
};

class LoopClosingTraceFinder : public TarjanTraceFinder
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit LoopClosingTraceFinder(ProductExplorer& explorer,
                                  uint32_t entryState) :
    TarjanTraceFinder(explorer),
    mEntryState(entryState)
  {}

  //##########################################################################
  //# Callbacks
  virtual void collectStartStates(BlockedArrayList<uint32_t>* list)
  {
    TarjanStateSpace& tarjan = getStateSpace();
    tarjan.setUpLoopClosingSearch(mEntryState);
    list->add(mEntryState);
  }

  virtual uint32_t expandState(uint32_t source,
                               uint32_t* sourceTuple,
                               const uint32_t* sourcePacked,
                               BlockedArrayList<uint32_t>* successors)
  {
    ProductExplorer& explorer = getExplorer();
    explorer.setTarjanTraceSuccessors(successors);
    explorer.expandForwardAgainIncludingSelfloops
      (source, sourceTuple, &ProductExplorer::processTarjanTraceTransition);
    return explorer.getTraceState();
  }

  virtual bool isStartState(uint32_t state, int count)
  {
    return state == mEntryState && count > 0;
  }

  virtual void prependInitialState(const uint32_t* tuple,
                                   const jni::HashMapGlue& stateMap,
                                   const jni::ListGlue& traceList)
  {
  }

private:
  //##########################################################################
  //# Data Members
  uint32_t mEntryState;
};


void ProductExplorer::
computeLoopCounterExample(const jni::ListGlue& list)
{
  LoopEntryTraceFinder entryFinder(*this);
  uint32_t entry = entryFinder.appendCounterExample(list);
  mLoopIndex = list.size() - 1;
  removeUncontrollableEvents();
  LoopClosingTraceFinder closingFinder(*this, entry);
  jni::LinkedListGlue loopList(mCache);
  closingFinder.appendCounterExample(loopList);
  list.addAll(&loopList);
  mTarjanTraceSuccessors = 0;
}


//----------------------------------------------------------------------------
// Depth Map

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



//############################################################################
//# ProductExplorer: State Expansion Procedures

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

bool ProductExplorer::
isLocalDumpState(const uint32_t* tuple)
  const
{
  return false;
}



//############################################################################
//# Class ProductExplorerFinalizer

class ProductExplorerFinalizer {

public:
  //##########################################################################
  //# Constructor & Destructor
  ProductExplorerFinalizer(const jni::NativeModelAnalyzerGlue& gchecker) :
    mNativeModelAnalyzer(gchecker), mProductExplorer(0)
  {
  }

  ~ProductExplorerFinalizer()
  {
    finalize();
  }

  //##########################################################################
  //# Initialisation
  ProductExplorer* createProductExplorer
    (const jni::EventGlue& premarking,
     const jni::EventGlue& marking,
     jni::ClassCache& cache)
  {
    jni::ProductDESProxyFactoryGlue factory =
      mNativeModelAnalyzer.getFactoryGlue(&cache);
    jni::ProductDESGlue des = mNativeModelAnalyzer.getModelGlue(&cache);
    jni::KindTranslatorGlue translator =
      mNativeModelAnalyzer.getKindTranslatorGlue(&cache);
    if (mNativeModelAnalyzer.isEventTreeEnabled()) {
      mProductExplorer = new EventTreeProductExplorer
        (factory, des, translator, premarking, marking, &cache);
    } else {
      mProductExplorer = new BroadProductExplorer
        (factory, des, translator, premarking, marking, &cache);
    }
    const int limit = mNativeModelAnalyzer.getNodeLimit();
    if (limit >= 0) {
      mProductExplorer->setStateLimit(limit);
    }
    const int tlimit = mNativeModelAnalyzer.getTransitionLimit();
    if (tlimit >= 0) {
      mProductExplorer->setTransitionLimit(tlimit);
    }
    JNIEnv* env = cache.getEnvironment();
    jobject bbuffer =
      env->NewDirectByteBuffer(mProductExplorer, sizeof(*mProductExplorer));
    mNativeModelAnalyzer.setNativeModelAnalyzer(bbuffer);
    return mProductExplorer;
  }

  //##########################################################################
  //# Finalisation
  void finalize()
  {
    if (mProductExplorer != 0) {
      mNativeModelAnalyzer.setNativeModelAnalyzer(0);
      delete mProductExplorer;
      mProductExplorer = 0;
    }
  }

private:
  //##########################################################################
  //# Data Members
  jni::NativeModelAnalyzerGlue mNativeModelAnalyzer;
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
    jni::EventGlue nomarking(0, &cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(nomarking, nomarking, cache);
    bool initUncont = gchecker.isInitialUncontrollable();
    checker->setInitialUncontrollable(initUncont);
    bool result = checker->runSafetyCheck();
    jni::NativeVerificationResultGlue vresult =
      gchecker.createAnalysisResultGlue(&cache);
    if (result) {
      vresult.setSatisfied(true);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    } else {
      jni::SafetyCounterExampleGlue trace =
        checker->getSafetyCounterExample(gchecker);
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
    jni::EventGlue marking = gchecker.getUsedDefaultMarkingGlue(&cache);
    jni::EventGlue premarking =
      gchecker.getConfiguredPreconditionMarkingGlue(&cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(premarking, marking, cache);
    jni::ConflictCheckMode mode = gchecker.getConflictCheckModeGlue(&cache);
    checker->setConflictCheckMode(mode);
    bool aware = gchecker.isDumpStateAware();
    checker->setDumpStateAware(aware);
    bool result = checker->runNonblockingCheck();
    jni::NativeVerificationResultGlue vresult =
      gchecker.createAnalysisResultGlue(&cache);
    if (result) {
      vresult.setSatisfied(true);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    } else {
      jni::ConflictCounterExampleGlue trace =
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


JNIEXPORT jobject JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeDeadlockChecker_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeDeadlockCheckerGlue gchecker(jchecker, &cache);
  waters::ProductExplorerFinalizer finalizer(gchecker);
  try {
    jni::EventGlue noMarking(0, &cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(noMarking, noMarking, cache);
    bool result = checker->runDeadlockCheck();
    jni::NativeVerificationResultGlue vresult =
      gchecker.createAnalysisResultGlue(&cache);
    if (result) {
      vresult.setSatisfied(true);
    } else {
      jni::ConflictCounterExampleGlue trace =
        checker->getConflictCounterExample(gchecker);
      vresult.setCounterExample(&trace);
    }
    checker->addStatistics(vresult);
    return vresult.returnJavaObject();
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
Java_net_sourceforge_waters_cpp_analysis_NativeControlLoopChecker_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeControlLoopCheckerGlue gchecker(jchecker, &cache);
  waters::ProductExplorerFinalizer finalizer(gchecker);
  try {
    jni::EventGlue nomarking(0, &cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(nomarking, nomarking, cache);
    bool result = checker->runLoopCheck();
    jni::NativeVerificationResultGlue vresult =
      gchecker.createAnalysisResultGlue(&cache);
    if (result) {
      vresult.setSatisfied(true);
      checker->addStatistics(vresult);
      return vresult.returnJavaObject();
    } else {
      jni::LoopCounterExampleGlue trace =
        checker->getLoopCounterExample(gchecker);
      vresult.setCounterExample(&trace);
      vresult.setSatisfied(false);
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
Java_net_sourceforge_waters_cpp_analysis_NativeStateCounter_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeStateCounterGlue gchecker(jchecker, &cache);
  waters::ProductExplorerFinalizer finalizer(gchecker);
  try {
    jni::EventGlue nomarking(0, &cache);
    waters::ProductExplorer* checker =
      finalizer.createProductExplorer(nomarking, nomarking, cache);
    checker->runStateCount();
    jni::NativeVerificationResultGlue result =
      gchecker.createAnalysisResultGlue(&cache);
    result.setSatisfied(true);
    checker->addStatistics(result);
    return result.returnJavaObject();
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
Java_net_sourceforge_waters_cpp_analysis_NativeModelAnalyzer_requestAbortNative
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


JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeModelAnalyzer_resetAbortNative
  (JNIEnv *env, jobject jchecker)
{
  jni::ClassCache cache(env);
  jni::NativeModelAnalyzerGlue gchecker(jchecker, &cache);
  jobject bbuffer = gchecker.getNativeModelAnalyzer();
  if (bbuffer != 0) {
    waters::ProductExplorer* explorer =
      (waters::ProductExplorer*) env->GetDirectBufferAddress(bbuffer);
    explorer->resetAbort();
  }
}


JNIEXPORT jlong JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeModelAnalyzer_getPeakMemoryUsage
  (JNIEnv *env, jclass clazz)
{
#ifndef __MINGW32__
  struct rusage rusage;
  if (getrusage(RUSAGE_SELF, &rusage) == 0) {
    jlong usage = rusage.ru_maxrss << 10;
    return usage;
  }
#endif
  return -1;
}
