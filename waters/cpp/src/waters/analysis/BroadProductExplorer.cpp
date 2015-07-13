//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadProductExplorer
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
#include "jni/cache/PreAnalysisConfigurationException.h"
#include "jni/cache/PreEventNotFoundException.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/CollectionGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/EventKindGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/NativeVerificationResultGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/TreeSetGlue.h"

#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/TarjanStateSpace.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/analysis/TransitionUpdateRecord.h"
#include "waters/base/HashTable.h"


namespace waters {

//############################################################################
//# class BroadProductExplorer
//############################################################################

//############################################################################
//# BroadProductExplorer: Constructors & Destructors

BroadProductExplorer::
BroadProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
                     const jni::ProductDESGlue& des,
                     const jni::KindTranslatorGlue& translator,
                     const jni::EventGlue& premarking,
                     const jni::EventGlue& marking,
                     jni::ClassCache* cache)
  : ProductExplorer(factory, des, translator, premarking, marking, cache),
    mNumEventRecords(0),
    mEventRecords(0),
    mNumReversedEventRecords(0),
    mReversedEventRecords(0),
    mMaxNondeterministicUpdates(0),
    mNondeterministicTransitionIterators(0),
    mDumpStates(0),
    mNumTransitionsExplored(0)
{
}

BroadProductExplorer::
~BroadProductExplorer()
{
  if (mReversedEventRecords != 0) {
    for (int i = 0; i < mNumReversedEventRecords; i++) {
      delete mReversedEventRecords[i];
    }
    delete [] mReversedEventRecords;
  }
  if (mEventRecords != 0) {
    for (int i = 0; i < mNumEventRecords; i++) {
      delete mEventRecords[i];
    }
    delete [] mEventRecords;
  }
  delete [] mNondeterministicTransitionIterators;
  delete [] mDumpStates;
}


//############################################################################
//# BroadProductExplorer: Overrides for ProductExplorer

void BroadProductExplorer::
addStatistics(const jni::NativeVerificationResultGlue& vresult)
  const
{
  ProductExplorer::addStatistics(vresult);
  vresult.setTotalNumberOfEvents(mNumEventRecords);
  vresult.setNumberOfExploredTransitions(mNumTransitionsExplored);
}


//############################################################################
//# BroadProductExplorer: Shared Auxiliary Methods

void BroadProductExplorer::
setup()
{
  ProductExplorer::setup();
  if (!isTrivial()) {
    switch (getCheckType()) {
    case CHECK_TYPE_SAFETY:
      setupSafety();
      break;
    case CHECK_TYPE_NONBLOCKING:
      setupNonblocking();
      break;
    default:
      break;
    }
  }
}

void BroadProductExplorer::
teardown()
{
  if (mReversedEventRecords != 0) {
    for (int i = 0; i < mNumReversedEventRecords; i++) {
      delete mReversedEventRecords[i];
    }
    delete [] mReversedEventRecords;
    mReversedEventRecords = 0;
  }
  if (mEventRecords != 0) {
    for (int i = 0; i < mNumEventRecords; i++) {
      delete mEventRecords[i];
    }
    delete [] mEventRecords;
    mEventRecords = 0;
  }
  delete [] mNondeterministicTransitionIterators;
  mNondeterministicTransitionIterators = 0;
  delete [] mDumpStates;
  mDumpStates = 0;
  ProductExplorer::teardown();
}


bool BroadProductExplorer::
isLocalDumpState(const uint32_t* tuple)
  const
{
  if (mDumpStates != 0) {
    uint32_t a = mDumpStates[0];
    int d = 1;
    do {
      if (tuple[a] == mDumpStates[d++]) {
        return true;
      }
      a = mDumpStates[d++];
    } while (a != UINT32_MAX);
  }
  return false;
}


// I know this is really kludgy,
// but inlining this code is 15% faster than using method calls.
// ~~~Robi

#define EXPAND_FORWARD(numwords, source, sourcetuple, sourcepacked)     \
  {                                                                     \
    for (int e = 0; e < mNumEventRecords; e++) {                        \
      BroadEventRecord* event = mEventRecords[e];                       \
      const AutomatonRecord* dis = 0;                                   \
      FIND_DISABLING_AUTOMATON(sourcetuple, event, dis);                \
      if (dis == 0) {                                                   \
        EXPAND_ENABLED_TRANSITIONS                                      \
          (numwords, source, sourcetuple, sourcepacked, event);         \
      }                                                                 \
    }                                                                   \
  }

#define EXPAND_REVERSE(numwords, source, sourcetuple, sourcepacked)     \
  {                                                                     \
    for (int e = 0; e < mNumReversedEventRecords; e++) {                \
      BroadEventRecord* event = mReversedEventRecords[e];               \
      const AutomatonRecord* dis = 0;                                   \
      FIND_DISABLING_AUTOMATON(sourcetuple, event, dis);                \
      if (dis == 0) {                                                   \
        EXPAND_ENABLED_TRANSITIONS                                      \
          (numwords, source, sourcetuple, sourcepacked, event);         \
      }                                                                 \
    }                                                                   \
  }

#define FIND_DISABLING_AUTOMATON(sourcetuple, event, dis)               \
  {                                                                     \
    for (TransitionRecord* trans = event->getTransitionRecord();        \
         trans != 0;                                                    \
         trans = trans->getNextInSearch()) {                            \
      const AutomatonRecord* aut = trans->getAutomaton();               \
      const int a = aut->getAutomatonIndex();                           \
      const uint32_t source = sourcetuple[a];                           \
      const uint32_t target = trans->getDeterministicSuccessorShifted(source); \
      if (target == TransitionRecord::NO_TRANSITION) {                  \
        dis = aut;                                                      \
        break;                                                          \
      }                                                                 \
    }                                                                   \
  }

#define EXPAND_ENABLED_TRANSITIONS(numwords, source,                    \
                                   sourcetuple, sourcepacked, event)    \
  {                                                                     \
    uint32_t* bufferpacked = getStateSpace().prepare();                 \
    if (event->isDeterministic()) {                                     \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferpacked[w] = sourcepacked[w];                            \
        } else {                                                        \
          uint32_t word = (sourcepacked[w] & update->getKeptMask()) |   \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32_t source = sourcetuple[a];                     \
            word |= trans->getDeterministicSuccessorShifted(source);    \
          }                                                             \
          bufferpacked[w] = word;                                       \
        }                                                               \
      }                                                                 \
      ADD_NEW_STATE(source);                                            \
      mNumTransitionsExplored++;                                        \
    } else {                                                            \
      int ndend = 0;                                                    \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferpacked[w] = sourcepacked[w];                            \
        } else {                                                        \
          uint32_t word = (sourcepacked[w] & update->getKeptMask()) |   \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32_t source = sourcetuple[a];                     \
            uint32_t succ = trans->getDeterministicSuccessorShifted(source); \
            if (succ == TransitionRecord::MULTIPLE_TRANSITIONS) {       \
              succ = mNondeterministicTransitionIterators[ndend++].     \
                setup(trans, source);                                   \
            }                                                           \
            word |= succ;                                               \
          }                                                             \
          bufferpacked[w] = word;                                       \
        }                                                               \
      }                                                                 \
      if (ndend == 0) {                                                 \
        ADD_NEW_STATE(source);                                          \
        mNumTransitionsExplored++;                                      \
      } else {                                                          \
        int ndindex;                                                    \
        do {                                                            \
          ADD_NEW_STATE_ALLOC(source, bufferpacked);                    \
          mNumTransitionsExplored++;                                    \
          for (ndindex = 0; ndindex < ndend; ndindex++) {               \
            if (!mNondeterministicTransitionIterators[ndindex].         \
                advance(bufferpacked)) {                                \
              break;                                                    \
            }                                                           \
          }                                                             \
        } while (ndindex < ndend);                                      \
      }                                                                 \
    }                                                                   \
  }


#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    incNumberOfTransitions();                                           \
    if (getStateSpace().add() == getNumberOfStates()) {                 \
      incNumberOfStates();                                              \
    }                                                                   \
  }

#define ADD_NEW_STATE_ALLOC(source, bufferpacked)                       \
  {                                                                     \
    incNumberOfTransitions();                                           \
    if (getStateSpace().add() == getNumberOfStates()) {                 \
      bufferpacked = getStateSpace().prepare(incNumberOfStates());      \
    }                                                                   \
  }


bool BroadProductExplorer::
expandSafetyState(const uint32_t* sourcetuple, const uint32_t* sourcepacked)
{
  const int numwords = getAutomatonEncoding().getEncodingSize();
  for (int e = 0; e < mNumEventRecords; e++) {
    const BroadEventRecord* event = mEventRecords[e];
    const AutomatonRecord* dis = 0;
    FIND_DISABLING_AUTOMATON(sourcetuple, event, dis);
    if (dis == 0) {
      EXPAND_ENABLED_TRANSITIONS
        (numwords, SOURCE, sourcetuple, sourcepacked, event);
    } else if (!dis->isPlant() && !event->isControllable()) {
      setTraceEvent(event, dis);
      return false;
    }
  }
  return true;
}

#undef ADD_NEW_STATE
#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    uint32_t code = getStateSpace().add();                              \
    if (code != source) {                                               \
      setConflictKind(jni::ConflictKind_CONFLICT);                      \
      if (code == getNumberOfStates()) {                                \
        incNumberOfStates();                                            \
      }                                                                 \
      ADD_TRANSITION(source, code);                                     \
    }                                                                   \
  }

#undef ADD_NEW_STATE_ALLOC
#define ADD_NEW_STATE_ALLOC(source, bufferpacked)                       \
  {                                                                     \
    uint32_t code = getStateSpace().add();                              \
    if (code != source) {                                               \
      setConflictKind(jni::ConflictKind_CONFLICT);                      \
      if (code == getNumberOfStates()) {                                \
        bufferpacked = getStateSpace().prepare(incNumberOfStates());    \
      }                                                                 \
      ADD_TRANSITION_ALLOC(source, code);                               \
    }                                                                   \
  }

bool BroadProductExplorer::
expandNonblockingReachabilityState(uint32_t source,
                                   const uint32_t* sourcetuple,
                                   const uint32_t* sourcepacked)
{
  const int numwords = getAutomatonEncoding().getEncodingSize();
  setConflictKind(jni::ConflictKind_DEADLOCK);
  // Check for local dump states ...
  if (isLocalDumpState(sourcetuple)) {
    return false;
  }
  // Expand transitions, check for global deadlock ...
  switch (getConflictCheckMode()) {
  case jni::ConflictCheckMode_STORED_BACKWARDS_TRANSITIONS:
#   define ADD_TRANSITION addCoreachabilityTransition
#   define ADD_TRANSITION_ALLOC ADD_TRANSITION
    EXPAND_FORWARD(numwords, source, sourcetuple, sourcepacked);
#   undef ADD_TRANSITION
#   undef ADD_TRANSITION_ALLOC
    break;
  case jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS:
    {
      // Nondeterministic transitions only need to be marked off once per
      // state and event. The 'markedoff' flag suppresses the call to
      // markTransitionsTakenFast() for subsequent nondeterministic transitions.
      const BroadEventRecord* markedoff = 0;
#     define ADD_TRANSITION(source, target) {                           \
        incNumberOfTransitions();                                       \
        event->markTransitionsTakenFast(sourcetuple);                   \
      }
#     define ADD_TRANSITION_ALLOC(source, target) {                     \
        incNumberOfTransitions();                                       \
        if (markedoff != event) {                                       \
          event->markTransitionsTakenFast(sourcetuple);                 \
          markedoff = event;                                            \
        }                                                               \
      }
      EXPAND_FORWARD(numwords, source, sourcetuple, sourcepacked);
#     undef ADD_TRANSITION
#     undef ADD_TRANSITION_ALLOC
    }
    break;
  default:
    throw jni::PreAnalysisConfigurationException(getConflictCheckMode());
    break;
  }
  if (getConflictKind() != jni::ConflictKind_DEADLOCK) {
    return true;
  } else if (getAutomatonEncoding().isMarkedStateTuple(sourcetuple) ||
             !getAutomatonEncoding().isPreMarkedStateTuple(sourcetuple)) {
    setConflictKind(jni::ConflictKind_CONFLICT);
    return true;
  } else {
    return false;
  }
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC


#define ADD_NEW_STATE(source) checkCoreachabilityState()
#define ADD_NEW_STATE_ALLOC(source, bufferpacked) ADD_NEW_STATE(source)

void BroadProductExplorer::
expandNonblockingCoreachabilityState(const uint32_t* targettuple,
                                     const uint32_t* targetpacked)
{
  const int numwords = getAutomatonEncoding().getEncodingSize();
  EXPAND_REVERSE(numwords, TARGET, targettuple, targetpacked);
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC


#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    uint32_t target = getStateSpace().add();                            \
    if (target != source) {                                             \
      if (target == getNumberOfStates()) {                              \
        incNumberOfStates();                                            \
      }                                                                 \
      incNumberOfTransitions();                                         \
      tarjan->processTransition(source, target);                        \
    }                                                                   \
  }
#define ADD_NEW_STATE_ALLOC(source, bufferPacked)                       \
  {                                                                     \
    uint32_t target = getStateSpace().add();                            \
    if (target != source) {                                             \
      if (target == getNumberOfStates()) {                              \
        bufferPacked = getStateSpace().prepare(incNumberOfStates());    \
      }                                                                 \
      incNumberOfTransitions();                                         \
      tarjan->processTransition(source, target);                        \
    }                                                                   \
  }

void BroadProductExplorer::
expandTarjanState(uint32_t source,
                  const uint32_t* sourceTuple,
                  const uint32_t* sourcePacked)
{
  if (!isLocalDumpState(sourceTuple)) {
    const int numWords = getAutomatonEncoding().getEncodingSize();
    TarjanStateSpace* tarjan = (TarjanStateSpace*) &getStateSpace();
    EXPAND_FORWARD(numWords, source, sourceTuple, sourcePacked);
  }
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC


#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    uint32_t target = getStateSpace().find();                           \
    if (tarjan->isClosedState(target)) {                                \
      return false; /* stop checking states */                          \
    }                                                                   \
  }
#define ADD_NEW_STATE_ALLOC(source, bufferPacked) ADD_NEW_STATE(source)

bool BroadProductExplorer::
closeNonblockingTarjanState(uint32_t state, uint32_t* tupleBuffer)
{
  uint32_t* tuplePacked = getStateSpace().get(state);
  getAutomatonEncoding().decode(tuplePacked, tupleBuffer);
  if (isLocalDumpState(tupleBuffer)) {
    return true; // continue checking states - results in critical component
  } else if (getAutomatonEncoding().isMarkedStateTuple(tupleBuffer)) {
    return false; // stop checking states
  } else {
    const int numWords = getAutomatonEncoding().getEncodingSize();
    TarjanStateSpace* tarjan = (TarjanStateSpace*) &getStateSpace();
    EXPAND_FORWARD(numWords, state, tupleBuffer, tuplePacked);
    return true; // continue checking states
  }
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC


#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    uint32_t target = getStateSpace().find();                           \
    if (target != source) {                                             \
      gotSuccessor = true;                                              \
      if (target != UINT32_MAX) {                                       \
        uint32_t& ref = tarjan->getTraceStatusRef(target);              \
        switch (ref) {                                                  \
        case TarjanStateSpace::TR_OPEN:                                 \
          successors->add(target);                                      \
          ref = source;                                                 \
          break;                                                        \
        case TarjanStateSpace::TR_CRITICAL:                             \
          ref = source;                                                 \
          setTraceState(target);                                        \
          setTraceEvent(event);                                         \
          return false;                                                 \
        default:                                                        \
          break;                                                        \
        }                                                               \
      }                                                                 \
    }                                                                   \
  }
#define ADD_NEW_STATE_ALLOC(source, bufferPacked) ADD_NEW_STATE(source)

bool BroadProductExplorer::
expandTarjanTraceState(uint32_t source,
                       const uint32_t* sourceTuple,
                       const uint32_t* sourcePacked,
                       BlockedArrayList<uint32_t>* successors)
{
  bool gotSuccessor = false;
  if (!isLocalDumpState(sourceTuple)) {
    const int numWords = getAutomatonEncoding().getEncodingSize();
    TarjanStateSpace* tarjan = (TarjanStateSpace*) &getStateSpace();
    EXPAND_FORWARD(numWords, source, sourceTuple, sourcePacked);
  }
  if (!gotSuccessor &&
      !getAutomatonEncoding().isMarkedStateTuple(sourceTuple)) {
    setTraceState(source);
    setTraceEvent(0);
    setConflictKind(jni::ConflictKind_DEADLOCK);
  }
  return gotSuccessor; 
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC


void BroadProductExplorer::
setupReverseTransitionRelations()
{
  if (mReversedEventRecords == 0) {
    bool removing = getCheckType() == CHECK_TYPE_NONBLOCKING &&
                    getConflictCheckMode() ==
                      jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS;
    int maxupdates = 0;
    BroadEventRecord** reversed = new BroadEventRecord*[mNumEventRecords];
    for (int e = 0; e < mNumEventRecords; e++) {
      BroadEventRecord* event = mEventRecords[e];
      if (removing) {
        event->removeTransitionsNotTaken();
      }
      if (!event->isGloballyDisabled() && !event->isOnlySelfloops()) {
        BroadEventRecord* rev = event->createReversedRecord();
        reversed[mNumReversedEventRecords++] = rev;
        const int numupdates = rev->getNumberOfNondeterministicUpdates();
        if (numupdates > maxupdates) {
          maxupdates = numupdates;
        }
      }
    }
    if (mNumReversedEventRecords < mNumEventRecords) {
      mReversedEventRecords = new BroadEventRecord*[mNumReversedEventRecords];
      for (int e = 0; e < mNumReversedEventRecords; e++) {
        mReversedEventRecords[e] = reversed[e];
      }
      delete [] reversed;
    } else {
      mReversedEventRecords = reversed;
    }
    qsort(mReversedEventRecords, mNumReversedEventRecords,
          sizeof(BroadEventRecord*),
          BroadEventRecord::compareForBackwardSearch);
    if (maxupdates > mMaxNondeterministicUpdates) {
      mMaxNondeterministicUpdates = maxupdates;
      delete [] mNondeterministicTransitionIterators;
      mNondeterministicTransitionIterators =
        new NondeterministicTransitionIterator[mMaxNondeterministicUpdates];
    }
  }
}



void BroadProductExplorer::
expandTraceState(const uint32_t* targettuple,
                 const uint32_t* targetpacked,
                 uint32_t level)
{
  const int numwords = getAutomatonEncoding().getEncodingSize();
  const uint32_t prevLevelStart = getFirstState(level - 1);
  const uint32_t prevLevelEnd = getFirstState(level);
  const int prevLevelSize = prevLevelEnd - prevLevelStart;
  const BroadEventRecord* event = 0;
  const BroadEventRecord** deferred =
    new const BroadEventRecord*[mNumReversedEventRecords];
  uint32_t* sourcetuple = new uint32_t[getNumberOfAutomata()];
  try {
#   define ADD_NEW_STATE(source) {                    \
      uint32_t found = getStateSpace().find();        \
      if (found < prevLevelEnd) {                     \
        setTraceState(found);                         \
        throw SearchAbort();                          \
      }                                               \
    }
#   define ADD_NEW_STATE_ALLOC(source, bufferpacked) ADD_NEW_STATE(source)
    int numDeferred = 0;
    for (int e = 0; e < mNumReversedEventRecords; e++) {
      event = mReversedEventRecords[e];
      const AutomatonRecord* dis = 0;
      FIND_DISABLING_AUTOMATON(targettuple, event, dis);
      if (dis == 0) {
        if (event->getFanout(targettuple) <= prevLevelSize) {
          EXPAND_ENABLED_TRANSITIONS
            (numwords, TARGET, targettuple, targetpacked, event);
        } else {
          // If the event has too many transitions to this state,
          // do not try to expand. Search later in forward direction,
          // if really necessary ...
          deferred[numDeferred++] = event;
        }
      }
    }
#   undef ADD_NEW_STATE
#   define ADD_NEW_STATE(source) {                                      \
      if (getStateSpace().equalTuples(bufferpacked, targetpacked)) {    \
        setTraceState(source);                                          \
        throw SearchAbort();                                            \
      }                                                                 \
    }
    for (uint32_t source = prevLevelStart; source < prevLevelEnd; source++) {
      uint32_t* sourcepacked = getStateSpace().get(source);
      getAutomatonEncoding().decode(sourcepacked, sourcetuple);
      for (int e = 0; e < numDeferred; e++) {
        event = deferred[e]->getForwardRecord();
        const AutomatonRecord* dis = 0;
        FIND_DISABLING_AUTOMATON(sourcetuple, event, dis);
        if (dis == 0) {
          EXPAND_ENABLED_TRANSITIONS
            (numwords, source, sourcetuple, sourcepacked, event);
        }                                                        
      }
    }
    // We should never get here ...
    delete [] deferred;
    delete [] sourcetuple;
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    setTraceEvent(event);
    delete [] deferred;
    delete [] sourcetuple;
  } catch (...) {
    delete [] deferred;
    delete [] sourcetuple;
    throw;
  }
}

#undef ADD_NEW_STATE


#define ADD_NEW_STATE(source)                                    \
  if (getStateSpace().equalTuples(bufferpacked, targetpacked)) { \
    throw SearchAbort();                                         \
  }

const EventRecord* BroadProductExplorer::
findEvent(const uint32_t* sourcepacked,
          const uint32_t* sourcetuple,
          const uint32_t* targetpacked)
{
  const int numwords = getAutomatonEncoding().getEncodingSize();
  const BroadEventRecord* event;
  try {
    int e = -1;
    do {
      event = mEventRecords[++e];
      const AutomatonRecord* dis = 0;
      FIND_DISABLING_AUTOMATON(sourcetuple, event, dis);
      if (dis == 0) {
        EXPAND_ENABLED_TRANSITIONS
          (numwords, SOURCE, sourcetuple, sourcepacked, event);
      }
    } while (true);
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    return event;
  }
}

#undef ADD_NEW_STATE_ALLOC
#undef EXPAND_FORWARD
#undef EXPAND_REVERSE


void BroadProductExplorer::
storeNondeterministicTargets(const uint32_t* sourcetuple,
                             const uint32_t* targettuple,
                             const jni::MapGlue& map)
{
  const BroadEventRecord* event = (const BroadEventRecord*) getTraceEvent();
  event->storeNondeterministicTargets(sourcetuple, targettuple, map);
}


//############################################################################
//# BroadProductExplorer: Private Auxiliary Methods

void BroadProductExplorer::
setupSafety()
{
  // Establish initial event map ...
  jni::ClassCache* cache = getCache();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const EventRecordHashAccessor* eventaccessor =
    BroadEventRecord::getHashAccessor();
  PtrHashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  setupEventMap(eventmap);

  // Collect transitions ...
  const int numaut = getNumberOfAutomata();
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
    if (aut->getNumberOfInitialStates() == 0 && !isTrivial()) {
      setTrivial();
      if (!aut->isPlant() && isInitialUncontrollable()) {
        setTraceEvent(0, aut);
      }
    }
    setupTransitions(aut, autglue, eventmap);
    const jni::SetGlue& events = autglue.getEventsGlue(cache);
    const jni::IteratorGlue& eventiter = events.iteratorGlue(cache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      jni::EventKind kind =
        getKindTranslator().getEventKindGlue(&event, cache);
      if (kind == jni::EventKind_UNCONTROLLABLE ||
          kind == jni::EventKind_CONTROLLABLE) {
        BroadEventRecord* eventrecord = eventmap.get(&event);
        eventrecord->normalize(aut);
      }
    }
  }

  // Establish compact event list ...
  setupCompactEventList(eventmap);
}


void BroadProductExplorer::
setupNonblocking()
{
  // Establish initial event map ...
  jni::ClassCache* cache = getCache();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const EventRecordHashAccessor* eventaccessor =
    BroadEventRecord::getHashAccessor();
  PtrHashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  setupEventMap(eventmap);

  // Collect transitions ...
  const int numaut = getNumberOfAutomata();
  uint32_t numdump = 0;
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
    if (aut->getNumberOfInitialStates() == 0) {
      setTrivial();
      return;
    }
    setupTransitions(aut, autglue, eventmap);
    const jni::SetGlue& events = autglue.getEventsGlue(cache);
    const jni::IteratorGlue& eventiter = events.iteratorGlue(cache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      switch (getKindTranslator().getEventKindGlue(&event, cache)) {
      case jni::EventKind_UNCONTROLLABLE:
      case jni::EventKind_CONTROLLABLE:
        {
          BroadEventRecord* eventrecord = eventmap.get(&event);
          eventrecord->normalize(aut);
          break;
        }
      default:
        break;
      }
    }
    numdump += aut->getNumberOfDumpStates();
  }

  // Make dump states list ...
  if (numdump > 0) {
    mDumpStates = new uint32_t[2 * numdump + 1];
    int d = 0;
    for (int a = 0; a < numaut; a++) {
      const AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
      for (int i = 0; i < aut->getNumberOfDumpStates(); i++) {
        mDumpStates[d++] = a;
        mDumpStates[d++] = aut->getDumpState(i);
      }
    }
    mDumpStates[d] = UINT32_MAX;
  }

  // Establish compact event list ...
  setupCompactEventList(eventmap);
}


void BroadProductExplorer::
setupEventMap(PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  const int numwords = getAutomatonEncoding().getEncodingSize();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::EventGlue event(javaobject, cache);
    bool controllable;
    switch (getKindTranslator().getEventKindGlue(&event, cache)) {
    case jni::EventKind_UNCONTROLLABLE:
      controllable = false;
      break;
    case jni::EventKind_CONTROLLABLE:
      controllable = true;
      break;
    default:
      continue;
    }
    BroadEventRecord* record =
      new BroadEventRecord(event, controllable, numwords);
    eventmap.add(record);
  }
}


void BroadProductExplorer::
setupTransitions
  (AutomatonRecord* aut,
   const jni::AutomatonGlue& autglue,
   const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  Int32PtrHashTable<const jni::StateGlue*,uint32_t>* statemap =
    aut->createStateMap();
  const jni::CollectionGlue transitions = autglue.getTransitionsGlue(cache);
  const jni::TreeSetGlue uniqtrans(&transitions, cache);
  bool* dumpstatus = 0;
  if (isDumpStateAware() && !aut->isAllMarked()) {
    uint32_t numstates = aut->getNumberOfStates();
    dumpstatus = new bool[numstates];
    for (uint32_t s = 0; s < numstates; s++) {
      dumpstatus[s] = !aut->isMarkedState(s);
    }
  }
  int maxpass = 1;
  for (int pass = 1; pass <= maxpass; pass++) {
    const jni::IteratorGlue transiter = uniqtrans.iteratorGlue(cache);
    while (transiter.hasNext()) {
      jobject javaobject = transiter.next();
      jni::TransitionGlue trans(javaobject, cache);
      const jni::EventGlue& eventglue = trans.getEventGlue(cache);
      BroadEventRecord* eventrecord = eventmap.get(&eventglue);
      if (eventrecord == 0) {
        throw jni::PreEventNotFoundException(getModel(), eventglue.getName());
      }
      const jni::StateGlue& sourceglue = trans.getSourceGlue(cache);
      const uint32_t sourcecode = statemap->get(&sourceglue);
      const jni::StateGlue& targetglue = trans.getTargetGlue(cache);
      const uint32_t targetcode = statemap->get(&targetglue);
      if (pass == 1) {
        const bool det =
          eventrecord->addDeterministicTransition(aut, sourcecode, targetcode);
        if (!det) {
          maxpass = 2;
        }
        if (dumpstatus != 0) {
          dumpstatus[sourcecode] = false;
        }
      } else {
        eventrecord->addNondeterministicTransition
          (aut, sourcecode, targetcode);
      }
    }
  }
  if (dumpstatus != 0) {
    aut->setupDumpStates(dumpstatus);
    delete [] dumpstatus;
  }
  aut->deleteStateMap(statemap);
}


void BroadProductExplorer::
setupCompactEventList
  (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  mMaxNondeterministicUpdates = 0;
  mNumEventRecords = eventmap.size();
  CheckType checkType = getCheckType();
  HashTableIterator hiter1 = eventmap.iterator();
  while (eventmap.hasNext(hiter1)) {
    const BroadEventRecord* event = eventmap.next(hiter1);
    if (event->isSkippable(checkType)) {
      mNumEventRecords--;
    }
    const int numupdates = event->getNumberOfNondeterministicUpdates();
    if (numupdates > mMaxNondeterministicUpdates) {
      mMaxNondeterministicUpdates = numupdates;
    }
  }
  mEventRecords = new BroadEventRecord*[mNumEventRecords];
  HashTableIterator hiter2 = eventmap.iterator();
  int i = 0;
  bool trivial = checkType == CHECK_TYPE_SAFETY;
  bool removing = checkType == CHECK_TYPE_NONBLOCKING &&
                  getConflictCheckMode() ==
                    jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS;
  while (eventmap.hasNext(hiter2)) {
    BroadEventRecord* event = eventmap.next(hiter2);
    if (event->isSkippable(checkType)) {
      delete event;
    } else {
      event->optimizeTransitionRecordsForSearch(checkType);
      if (removing) {
        event->setupNotTakenSearchRecords();
      }
      mEventRecords[i++] = event;
      trivial &= (event->isControllable() | !event->isDisabledInSpec());
    }
  }
  if (trivial) {
    setTrivial();
    return;
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(BroadEventRecord*),
        BroadEventRecord::compareForForwardSearch);
  if (mMaxNondeterministicUpdates > 0) {
    mNondeterministicTransitionIterators =
      new NondeterministicTransitionIterator[mMaxNondeterministicUpdates];
  }
}


}  /* namespace waters */
