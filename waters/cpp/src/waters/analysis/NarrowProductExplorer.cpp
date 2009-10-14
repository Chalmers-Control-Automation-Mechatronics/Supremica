//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowProductExplorer
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

#include "waters/analysis/NarrowEventRecord.h"
#include "waters/analysis/NarrowProductExplorer.h"
#include "waters/analysis/NarrowTransitionTable.h"
#include "waters/analysis/StateSpace.h"
#include "waters/base/HashTable.h"


namespace waters {

//############################################################################
//# class NarrowProductExplorer
//############################################################################

//############################################################################
//# NarrowProductExplorer: Constructors & Destructors

NarrowProductExplorer::
NarrowProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
                      const jni::ProductDESGlue& des,
                      const jni::KindTranslatorGlue& translator,
                      const jni::EventGlue& marking,
                      jni::ClassCache* cache)
  : ProductExplorer(factory, des, translator, marking, cache),
    mNumEventRecords(0),
    mEventRecords(0),
    mTransitionTables(0),
    mIterator(0)
{
}

NarrowProductExplorer::
~NarrowProductExplorer()
{
  if (mEventRecords != 0) {
    for (int i = 0; i < mNumEventRecords; i++) {
      delete mEventRecords[i];
    }
    delete[] mEventRecords;
  }
  if (mTransitionTables != 0) {
    const int numaut = getNumberOfAutomata();
    for (int a = 0; a < numaut; a++) {
      mTransitionTables[a].~NarrowTransitionTable();
    }
    delete[] (char*) mTransitionTables;
  }
  delete[] mIterator;
}


//############################################################################
//# NarrowProductExplorer: Shared Auxiliary Methods

void NarrowProductExplorer::
setup()
{
  // Setup automaton encoding, state space, and depth map ...
  ProductExplorer::setup();

  // Check initial states ...
  jni::ClassCache* cache = getCache();
  const int numaut = getNumberOfAutomata();
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    const uint32 numinit = aut->getEndOfInitialStates();
    switch (numinit) {
    case 0:
      setTrivial();
      return;
    case 1:
      break;
    default:
      if (getMode() != EXPLORER_MODE_SAFETY || aut->isPlant()) {
        break;
      } else {
        const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
        const jni::StateGlue& state = aut->getJavaState(1);
        jni::NondeterministicDESExceptionGlue
          exception(&autglue, &state, cache);
        throw cache->throwJavaException(exception);
      }
    }
  }

  // Establish initial event map ...
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const HashAccessor* eventaccessor = NarrowEventRecord::getHashAccessor();
  HashTable<const jni::EventGlue*,NarrowEventRecord*>
    eventmap(eventaccessor, numevents);
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
    NarrowEventRecord* record = new NarrowEventRecord(event, controllable);
    eventmap.add(record);
  }

  // Establish compact event list ...
  mNumEventRecords = eventmap.size();
  mEventRecords = new NarrowEventRecord*[mNumEventRecords];
  HashTableIterator hiter = eventmap.iterator();
  int e = 0;
  while (eventmap.hasNext(hiter)) {
    mEventRecords[e++] = eventmap.next(hiter);
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(NarrowEventRecord*),
        EventRecord::compare);
  for (e = 0; e < mNumEventRecords; e++) {
    mEventRecords[e]->setEventCode(e);
  }

  // Collect transitions ...
  mTransitionTables = 
    (NarrowTransitionTable*) new char[numaut * sizeof(NarrowTransitionTable)];
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    new (&mTransitionTables[a]) NarrowTransitionTable(aut, cache, eventmap);
  }
  for (int a = 0; a < numaut; a++) {
    mTransitionTables[a].removeSkipped(mEventRecords);
  }

  // More allocation ...
  mIterator = new uint32[numaut];
}

void NarrowProductExplorer::
teardown()
{
  if (mEventRecords != 0) {
    for (int i = 0; i < mNumEventRecords; i++) {
      delete mEventRecords[i];
    }
    mNumEventRecords = 0;
    delete [] mEventRecords;
    mEventRecords = 0;
  }
  if (mTransitionTables != 0) {
    const int numaut = getNumberOfAutomata();
    for (int a = 0; a < numaut; a++) {
      mTransitionTables[a].~NarrowTransitionTable();
    }
    delete [] (char*) mTransitionTables;
    mTransitionTables = 0;
  }
  delete [] mIterator;
  mIterator = 0;
  ProductExplorer::teardown();
}


// I know this is really kludgy,
// but inlining this code is 15% faster than using method calls.
// ~~~Robi

#define EXPAND(numwords, source, sourcetuple, sourcepacked)             \
  {                                                                     \
    for (int e = 0; e < mNumEventRecords; e++) {                        \
      NarrowEventRecord* event = mEventRecords[e];                       \
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


bool NarrowProductExplorer::
expandSafetyState(const uint32* sourcetuple, const uint32* sourcepacked)
{
  /*
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  for (int e = 0; e < mNumEventRecords; e++) {
    const NarrowEventRecord* event = mEventRecords[e];
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
  */
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
      ADD_TRANSITION_ALLOC(source, code);                               \
    }                                                                   \
  }

bool NarrowProductExplorer::
expandNonblockingReachabilityState(uint32 source,
                                   const uint32* sourcetuple,
                                   const uint32* sourcepacked)
{
  const int numaut = getNumberOfAutomata();
  uint32 minevent = UNDEF_UINT32;
  uint32 mincount;
  for (int a = 0; a < numaut; a++) {
    const NarrowTransitionTable& table = mTransitionTables[a];
    mIterator[a] = table.iterator(sourcetuple[a]);
    uint32 event = table.getEvent(mIterator[a]);
    if (event < minevent) {
      minevent = event;
      mincount = 1;
    } else if (event == minevent) {
      mincount++;
    }
  }
  while (minevent != UNDEF_UINT32) {
    if (mincount == mEventRecords[minevent]->getNumberOfAutomata()) {
      addSuccessorStates();
    }
    uint32 newminevent = UNDEF_UINT32;
    for (int a = 0; a < numaut; a++) {
      const NarrowTransitionTable& table = mTransitionTables[a];
      uint32 event = table.getEvent(mIterator[a]);
      if (event == minevent) {
        mIterator[a] = table.next(mIterator[a]);
        event = table.getEvent(mIterator[a]);
      }
      if (event < newminevent) {
        newminevent = event;
        mincount = 1;
      } else if (event == newminevent) {
        mincount++;
      }
    }
    minevent = newminevent;
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

void NarrowProductExplorer::
expandNonblockingCoreachabilityState(const uint32* targettuple,
                                     const uint32* targetpacked)
{
  //const int numwords = getAutomatonEncoding().getNumberOfWords();
  //EXPAND(numwords, TARGET, targettuple, targetpacked);
}

#undef ADD_NEW_STATE


void NarrowProductExplorer::
setupReverseTransitionRelations()
{
  /*
  if (mReversedEventRecords == 0) {
    bool removing =
      getMode() == EXPLORER_MODE_NONBLOCKING && getTransitionLimit() == 0;
    int numreversed = 0;
    mReversedEventRecords = new NarrowEventRecord*[mNumEventRecords];
    for (int e = 0; e < mNumEventRecords; e++) {
      NarrowEventRecord* event = mEventRecords[e];
      if (removing) {
        event->removeTransitionsNotTaken();
      }
      if (!event->isGloballyDisabled() && !event->isOnlySelfloops()) {
        if (event->reverse()) {
          mReversedEventRecords[numreversed++] = event;
        }
      }
    }
    qsort(mReversedEventRecords, numreversed, sizeof(NarrowEventRecord*),
          NarrowEventRecord::compareForBackwardSearch);
  }
  */
}


#define ADD_NEW_STATE(source) checkTraceState()

void NarrowProductExplorer::
expandTraceState(const uint32* targettuple, const uint32* targetpacked)
{
  /*
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  const NarrowEventRecord* event;
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
  */
}

#undef ADD_NEW_STATE
#undef ADD_NEW_STATE_ALLOC
#undef EXPAND


//############################################################################
//# NarrowProductExplorer: Private Auxiliary Methods

void NarrowProductExplorer::
addSuccessorStates()
{
}


}  /* namespace waters */
