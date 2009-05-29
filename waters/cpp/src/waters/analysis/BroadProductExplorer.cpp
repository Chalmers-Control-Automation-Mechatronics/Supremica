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
#include "jni/cache/PreJavaException.h"
#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/CollectionGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/EventKindGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/NondeterministicDESExceptionGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/StateSpace.h"
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
                     const jni::EventGlue& marking,
                     jni::ClassCache* cache)
  : ProductExplorer(factory, des, translator, marking, cache),
    mNumEventRecords(0),
    mEventRecords(0),
    mReversedEventRecords(0),
    mMaxUpdates(0),
    mNondeterministicTransitionIterators(0)
{
}

BroadProductExplorer::
~BroadProductExplorer()
{
  for (int i = 0; i < mNumEventRecords; i++) {
    delete mEventRecords[i];
  }
  delete [] mEventRecords;
  delete [] mNondeterministicTransitionIterators;
}


//############################################################################
//# BroadProductExplorer: Shared Auxiliary Methods

void BroadProductExplorer::
setup()
{
  ProductExplorer::setup();
  if (!isTrivial()) {
    switch (getMode()) {
    case EXPLORER_MODE_SAFETY:
      setupSafety();
      break;
    case EXPLORER_MODE_NONBLOCKING:
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
  for (int i = 0; i < mNumEventRecords; i++) {
    delete mEventRecords[i];
  }
  mNumEventRecords = 0;
  delete [] mEventRecords;
  mEventRecords = mReversedEventRecords = 0;
  delete [] mNondeterministicTransitionIterators;
  mNondeterministicTransitionIterators = 0;
  ProductExplorer::teardown();
}


// I know this is really kludgy,
// but inlining this code is 15% faster than using method calls.
// ~~~Robi

#define EXPAND(numwords, source, sourcetuple, sourcepacked)             \
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

#define FIND_DISABLING_AUTOMATON(sourcetuple, event, dis)               \
  {                                                                     \
    for (TransitionRecord* trans = event->getTransitionRecord();        \
         trans != 0;                                                    \
         trans = trans->getNextInSearch()) {                            \
      const AutomatonRecord* aut = trans->getAutomaton();               \
      const int a = aut->getAutomatonIndex();                           \
      const uint32 source = sourcetuple[a];                             \
      const uint32 target = trans->getDeterministicSuccessorShifted(source); \
      if (target == TransitionRecord::NO_TRANSITION) {                  \
        dis = aut;                                                      \
        break;                                                          \
      }                                                                 \
    }                                                                   \
  }

#define EXPAND_ENABLED_TRANSITIONS(numwords, source,                    \
                                   sourcetuple, sourcepacked, event)    \
  {                                                                     \
    uint32* bufferpacked = getStateSpace().prepare();                   \
    if (event->isDeterministic()) {                                     \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferpacked[w] = sourcepacked[w];                            \
        } else {                                                        \
          uint32 word = (sourcepacked[w] & update->getKeptMask()) |     \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32 source = sourcetuple[a];                       \
            word |= trans->getDeterministicSuccessorShifted(source);    \
          }                                                             \
          bufferpacked[w] = word;                                       \
        }                                                               \
      }                                                                 \
      ADD_NEW_STATE(source);                                            \
    } else {                                                            \
      int ndend = 0;                                                    \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferpacked[w] = sourcepacked[w];                            \
        } else {                                                        \
          uint32 word = (sourcepacked[w] & update->getKeptMask()) |     \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32 source = sourcetuple[a];                       \
            uint32 succ = trans->getDeterministicSuccessorShifted(source); \
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
      } else {                                                          \
        int ndindex;                                                    \
        do {                                                            \
          ADD_NEW_STATE_ALLOC(source, bufferpacked);                    \
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
expandSafetyState(const uint32* sourcetuple, const uint32* sourcepacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  for (int e = 0; e < mNumEventRecords; e++) {
    const BroadEventRecord* event = mEventRecords[e];
    const AutomatonRecord* dis = 0;
    FIND_DISABLING_AUTOMATON(sourcetuple, event, dis);
    if (dis == 0) {
      EXPAND_ENABLED_TRANSITIONS
        (numwords, SOURCE, sourcetuple, sourcepacked, event);
    } else if (!dis->isPlant() && !event->isControllable()) {
      setTraceEvent(event);
      return false;
    }
  }
  return true;
}

#undef ADD_NEW_STATE
#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    uint32 code = getStateSpace().add();                                \
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
    uint32 code = getStateSpace().add();                                \
    if (code != source) {                                               \
      setConflictKind(jni::ConflictKind_CONFLICT);                      \
      if (code == getNumberOfStates()) {                                \
        bufferpacked = getStateSpace().prepare(incNumberOfStates());    \
      }                                                                 \
      ADD_TRANSITION(source, code);                                     \
    }                                                                   \
  }

bool BroadProductExplorer::
expandNonblockingReachabilityState(uint32 source,
                                   const uint32* sourcetuple,
                                   const uint32* sourcepacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  setConflictKind(jni::ConflictKind_DEADLOCK);
  if (getTransitionLimit() > 0) {
#   define ADD_TRANSITION(source, target) \
      addCoreachabilityTransition(source, target)
    EXPAND(numwords, source, sourcetuple, sourcepacked);
#   undef ADD_TRANSITION
  } else {
#   define ADD_TRANSITION(source, target) \
      incNumberOfTransitions(); event->markTransitionsTakenFast(sourcetuple)
    EXPAND(numwords, source, sourcetuple, sourcepacked);
#   undef ADD_TRANSITION
  }
  if (getConflictKind() != jni::ConflictKind_DEADLOCK) {
    return true;
  } else if (getAutomatonEncoding().isMarkedStateTuple(sourcetuple)) {
    setConflictKind(jni::ConflictKind_CONFLICT);
    return true;
  } else {
    return false;
  }
}

#undef ADD_NEW_STATE
#define ADD_NEW_STATE(source) checkCoreachabilityState()
#undef ADD_NEW_STATE_ALLOC
#define ADD_NEW_STATE_ALLOC(source, bufferpacked) ADD_NEW_STATE(source)

void BroadProductExplorer::
expandNonblockingCoreachabilityState(const uint32* targettuple,
                                     const uint32* targetpacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  EXPAND(numwords, TARGET, targettuple, targetpacked);
}

#undef ADD_NEW_STATE


void BroadProductExplorer::
setupReverseTransitionRelations()
{
  if (mReversedEventRecords == 0) {
    bool removing =
      getMode() == EXPLORER_MODE_NONBLOCKING && getTransitionLimit() == 0;
    int numremoved = 0;
    int numreversed = 0;
    mReversedEventRecords = new BroadEventRecord*[mNumEventRecords];
    for (int e = 0; e < mNumEventRecords; e++) {
      BroadEventRecord* event = mEventRecords[e];
      if (removing) {
        numremoved += event->removeTransitionsNotTaken();
      }
      if (!event->isGloballyDisabled() && !event->isOnlySelfloops()) {
        if (event->reverse()) {
          mReversedEventRecords[numreversed++] = event;
        }
      }
    }
    qsort(mReversedEventRecords, numreversed, sizeof(BroadEventRecord*),
          BroadEventRecord::compareForBackwardSearch);
  }
}


#define ADD_NEW_STATE(source) checkTraceState()

void BroadProductExplorer::
expandTraceState(const uint32* targettuple, const uint32* targetpacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  const BroadEventRecord* event;
  try {
    int e = -1;
    do {
      event = mReversedEventRecords[++e];
      const AutomatonRecord* dis = 0;
      FIND_DISABLING_AUTOMATON(targettuple, event, dis);
      if (dis == 0) {
        EXPAND_ENABLED_TRANSITIONS
          (numwords, TARGET, targettuple, targetpacked, event);
      }
    } while (true);
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    setTraceEvent(event);
  }
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC
#undef EXPAND


//############################################################################
//# BroadProductExplorer: Private Auxiliary Methods

void BroadProductExplorer::
setupSafety()
{
  // Establish initial event map ...
  jni::ClassCache* cache = getCache();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const HashAccessor* eventaccessor = BroadEventRecord::getHashAccessor();
  HashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  setupEventMap(eventmap);

  // Collect transitions ...
  const int numaut = getNumberOfAutomata();
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
    const uint32 numinit = aut->getEndOfInitialStates();
    switch (numinit) {
    case 0:
      setTrivial();
      return;
    case 1:
      break;
    default:
      if (aut->isPlant()) {
        break;
      } else {
        const jni::StateGlue& state = aut->getJavaState(1);
        jni::NondeterministicDESExceptionGlue
          exception(&autglue, &state, cache);
        throw cache->throwJavaException(exception);
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
  const HashAccessor* eventaccessor = BroadEventRecord::getHashAccessor();
  HashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  setupEventMap(eventmap);

  // Collect transitions ...
  const jni::EventGlue& marking = getMarking();
  const int numaut = getNumberOfAutomata();
  bool allmarked = true;
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
    bool usemarking = false;
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      switch (getKindTranslator().getEventKindGlue(&event, cache)) {
      case jni::EventKind_UNCONTROLLABLE:
      case jni::EventKind_CONTROLLABLE:
        {
          BroadEventRecord* eventrecord = eventmap.get(&event);
          eventrecord->normalize(aut);
        }
        break;
      case jni::EventKind_PROPOSITION:
        if (!usemarking) {
          usemarking = event.isSameObject(marking, cache);
        }
        break;
      default:
        break;
      }
    }
    if (!usemarking) {
      aut->setAllMarked();
    } else if (aut->getNumberOfMarkedStates() == 0) {
      // we are blocking --- if there is an initial state ...
      setTraceState(0);
      setTrivial();
      allmarked = false;
    } else {
      allmarked &= aut->isAllMarked();
    }
  }
  if (allmarked) {
    setTrivial();
    return;
  }
  getAutomatonEncoding().setupMarkingTest();

  // Establish compact event list ...
  setupCompactEventList(eventmap);
}

void BroadProductExplorer::
setupEventMap(HashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  const int numwords = getAutomatonEncoding().getNumberOfWords();
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
   const HashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  HashTable<const jni::StateGlue*,uint32>* statemap = aut->createStateMap();
  const jni::CollectionGlue transitions = autglue.getTransitionsGlue(cache);
  int maxpass = 1;
  for (int pass = 1; pass <= maxpass; pass++) {
    const jni::IteratorGlue transiter = transitions.iteratorGlue(cache);
    while (transiter.hasNext()) {
      jobject javaobject = transiter.next();
      jni::TransitionGlue trans(javaobject, cache);
      const jni::EventGlue& eventglue = trans.getEventGlue(cache);
      BroadEventRecord* eventrecord = eventmap.get(&eventglue);
      const jni::StateGlue& sourceglue = trans.getSourceGlue(cache);
      const uint32 sourcecode = statemap->get(&sourceglue);
      const jni::StateGlue& targetglue = trans.getTargetGlue(cache);
      const uint32 targetcode = statemap->get(&targetglue);
      if (pass == 1) {
        const bool det = eventrecord->addDeterministicTransition
          (aut, sourcecode, targetcode);
        if (!det) {
          if (aut->isPlant()) {
            maxpass = 2;
          } else {
            jni::NondeterministicDESExceptionGlue
              exception(&autglue, &sourceglue, &eventglue, cache);
            throw cache->throwJavaException(exception);
          }
        }
      } else {
        eventrecord->addNondeterministicTransition
          (aut, sourcecode, targetcode);
      }
    }
  }
  aut->deleteStateMap(statemap);
}

void BroadProductExplorer::
setupCompactEventList
  (const HashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  mMaxUpdates = 0;
  mNumEventRecords = eventmap.size();
  ExplorerMode mode = getMode();
  HashTableIterator hiter1 = eventmap.iterator();
  while (eventmap.hasNext(hiter1)) {
    const BroadEventRecord* event = eventmap.next(hiter1);
    if (event->isSkippable(mode)) {
      mNumEventRecords--;
    }
    const int numupdates = event->getNumberOfUpdates();
    if (numupdates > mMaxUpdates) {
      mMaxUpdates = numupdates;
    }
  }
  mEventRecords = new BroadEventRecord*[mNumEventRecords];
  HashTableIterator hiter2 = eventmap.iterator();
  int i = 0;
  bool trivial = mode == EXPLORER_MODE_SAFETY;
  while (eventmap.hasNext(hiter2)) {
    BroadEventRecord* event = eventmap.next(hiter2);
    if (event->isSkippable(mode)) {
      delete event;
    } else {
      event->optimizeTransitionRecordsForSearch(mode);
      if (mode == EXPLORER_MODE_NONBLOCKING && getTransitionLimit() == 0) {
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
  mNondeterministicTransitionIterators =
    new NondeterministicTransitionIterator[mMaxUpdates];
}


}  /* namespace waters */
