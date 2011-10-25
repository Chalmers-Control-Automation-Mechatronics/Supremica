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
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/TreeSetGlue.h"
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
                     const jni::EventGlue& premarking,
                     const jni::EventGlue& marking,
                     jni::ClassCache* cache)
  : ProductExplorer(factory, des, translator, premarking, marking, cache),
    mNumEventRecords(0),
    mEventRecords(0),
    mReversedEventRecords(0),
    mMaxNondeterministicUpdates(0),
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
      const uint32_t source = sourcetuple[a];                             \
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
    uint32_t* bufferpacked = getStateSpace().prepare();                   \
    if (event->isDeterministic()) {                                     \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferpacked[w] = sourcepacked[w];                            \
        } else {                                                        \
          uint32_t word = (sourcepacked[w] & update->getKeptMask()) |     \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32_t source = sourcetuple[a];                       \
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
          uint32_t word = (sourcepacked[w] & update->getKeptMask()) |     \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32_t source = sourcetuple[a];                       \
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
expandSafetyState(const uint32_t* sourcetuple, const uint32_t* sourcepacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfSignificantWords();
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
    uint32_t code = getStateSpace().add();                                \
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
    uint32_t code = getStateSpace().add();                                \
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
  const int numwords = getAutomatonEncoding().getNumberOfSignificantWords();
  setConflictKind(jni::ConflictKind_DEADLOCK);
  if (getTransitionLimit() > 0) {
#   define ADD_TRANSITION addCoreachabilityTransition
#   define ADD_TRANSITION_ALLOC ADD_TRANSITION
    EXPAND(numwords, source, sourcetuple, sourcepacked);
#   undef ADD_TRANSITION
#   undef ADD_TRANSITION_ALLOC
  } else {
    // Nondeterministic transitions only need to be marked off once per
    // state and event. The 'markedoff' flag suppresses the call to
    // markTransitionsTakenFast() for subsequent nondeterministic transitions.
    const BroadEventRecord* markedoff = 0;
#   define ADD_TRANSITION(source, target) {                             \
      incNumberOfTransitions();                                         \
      event->markTransitionsTakenFast(sourcetuple);                     \
    }
#   define ADD_TRANSITION_ALLOC(source, target) {                       \
      incNumberOfTransitions();                                         \
      if (markedoff != event) {                                         \
        event->markTransitionsTakenFast(sourcetuple);                   \
        markedoff = event;                                              \
      }                                                                 \
    }
    EXPAND(numwords, source, sourcetuple, sourcepacked);
#   undef ADD_TRANSITION
#   undef ADD_TRANSITION_ALLOC
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
#define ADD_NEW_STATE(source) checkCoreachabilityState()
#undef ADD_NEW_STATE_ALLOC
#define ADD_NEW_STATE_ALLOC(source, bufferpacked) ADD_NEW_STATE(source)

void BroadProductExplorer::
expandNonblockingCoreachabilityState(const uint32_t* targettuple,
                                     const uint32_t* targetpacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfSignificantWords();
  EXPAND(numwords, TARGET, targettuple, targetpacked);
}

#undef ADD_NEW_STATE


/*
void BroadProductExplorer::
doIterativeTarjan(uint32_t start)
{
  TarjanControlStack controlStack;
  TarjanStateStack stateStack;
  uint32_t* currenttuple = new uint32_t[mNumAutomata];
  uint32_t succ = UINT32_MAX;


  storeTarjanIndex(start);
  controlStack.push(start);

  while (!controlStack.isEmpty()) {
    TarjanStackFrame& frame = controlStack.top();
    uint32_t current = frame.getStateCode();
    uint32_t* currentpacked = mStateSpace->get(current);
    getAutomatonEncoding().decode(currentpacked, currenttuple);
    uint32_t currentindex = getTarjanIndex(current);
    while (true) {
      if (frame.hasNondeterministicIterators()) {
        uint32_t first = frame.getFirstNondeterministicSuccessor();
        uint32_t* succpacked = getStateSpace().prepare(first);
        if (frame.advanceNondeterministicTransitionIterators(succpacked)) {
          succ = getStateSpace().add();
        } else {
          continue;
        }
      } else {
        int e = frame.getEventCode();
        AutomatonRecord* dis = 0;
        while (e < mNumEventRecords) {
          BroadEventRecord* event = mEventRecords[e];
          const AutomatonRecord* dis = 0;
          FIND_DISABLING_AUTOMATON(currenttuple, event, dis);
          if (dis != 0) {
            break;
          }
          e++;
        }
        if (dis == 0) {
          succ = UINT32_MAX;
        } else {
          uint32_t* succpacked = getStateSpace().prepare();
          if (event->isDeterministic()) {
            for (int w = 0; w < numwords; w++) {
              TransitionUpdateRecord* update =
                event->getTransitionUpdateRecord(w);
              if (update == 0) {
                succpacked[w] = currentpacked[w];
              } else {
                uint32_t word = (sourcepacked[w] & update->getKeptMask()) |
                  update->getCommonTargets();
                for (TransitionRecord* trans = update->getTransitionRecords();
                     trans != 0;
                     trans = trans->getNextInUpdate()) {
                  const AutomatonRecord* aut = trans->getAutomaton();
                  const int a = aut->getAutomatonIndex();
                  const uint32_t source = sourcetuple[a];
                  word |= trans->getDeterministicSuccessorShifted(source);
                }
                succpacked[w] = word;
              }
            }
            succ = getStateSpace().add();
          } else { // nondeterministic event
            for (int w = 0; w < numwords; w++) {
              TransitionUpdateRecord* update =
                event->getTransitionUpdateRecord(w);
              if (update == 0) {                                        
                succpacked[w] = sourcepacked[w];                      
              } else {                                                  
                uint32_t word = (sourcepacked[w] & update->getKeptMask()) |
                  update->getCommonTargets(); 
                for (TransitionRecord* trans = update->getTransitionRecords();
                     trans != 0;                                        
                     trans = trans->getNextInUpdate()) {                
                  const AutomatonRecord* aut = trans->getAutomaton();
                  const int a = aut->getAutomatonIndex();
                  const uint32_t source = sourcetuple[a];
                  uint32_t succ =
                    trans->getDeterministicSuccessorShifted(source); 
                  if (succ == TransitionRecord::MULTIPLE_TRANSITIONS) {
                    frame.createNondeterministicTransitionIterators
                      (mMaxNondeterministicUpdates);
                    succ = frame.setupNondeterministicTransitionIterator
                      (trans, source);
                  }                         
                  word |= succ;             
                }                           
                succpacked[w] = word;       
              }                             
            }
            succ = getStateSpace().add();
            frame.setFirstNondeterministicSuccessor(succ);
          }
        }
      }
      if (succ == UINT32_MAX) {
        // All successors have been processed---
        // complete the visit() method, then return.
        if (frame.isRoot()) {
          setInComponent(current);
          while (!stateStack.isEmpty()) {
            uint32_t next = stateStack.top();
            if (currentindex <= getTarjanIndex(next)) {
              stateStack.pop();
              setTarjanIndex(next, currentindex);
              setInComponent(next);
            } else {
              break;
            }
          }
        } else {
          stateStack.push(current);
        }
        controlStack.pop();
        if (!inComponent(current)) {
          TarjanStackFrame& parentframe = controlStack.top();
          uint32_t parent = parentframe.getStateCode();
          uint32_t parentindex = getTarjanIndex(current);
          if (currentindex < parentindex) {
            setTarjanIndex(parent, currentindex);
            parentframe.setRoot(false);
          }
        }
        break;
      } else if (getTarjanIndex(succ) == 0) {
        // We have got an unvisited successor to visit recursively.
        frame.setEventCode(e);
        controlStack.push(succ);
        // TODO-visit uncontrollable successors of succ
        break;
      } else if (!inComponent(succ)) {
        // Skip the recursive call because the successor was visited already.
        uint32_t succindex = getTarjanIndex(succ);
        if (succindex < currentindex) {
          currentindex = succindex;
          setTarjanIndex(current, currentindex);
          frame.setRoot(false);
        }
      }
    }
  }
}
*/


void BroadProductExplorer::
setupReverseTransitionRelations()
{
  if (mReversedEventRecords == 0) {
    bool removing =
      getMode() == EXPLORER_MODE_NONBLOCKING && getTransitionLimit() == 0;
    int numreversed = 0;
    int maxupdates = 0;
    mReversedEventRecords = new BroadEventRecord*[mNumEventRecords];
    for (int e = 0; e < mNumEventRecords; e++) {
      BroadEventRecord* event = mEventRecords[e];
      if (removing) {
        event->removeTransitionsNotTaken();
      }
      if (!event->isGloballyDisabled() && !event->isOnlySelfloops()) {
        if (event->reverse()) {
          mReversedEventRecords[numreversed++] = event;
          const int numupdates = event->getNumberOfNondeterministicUpdates();
          if (numupdates > maxupdates) {
            maxupdates = numupdates;
          }
        }
      }
    }
    qsort(mReversedEventRecords, numreversed, sizeof(BroadEventRecord*),
          BroadEventRecord::compareForBackwardSearch);
    if (maxupdates > mMaxNondeterministicUpdates) {
      mMaxNondeterministicUpdates = maxupdates;
      delete [] mNondeterministicTransitionIterators;
      mNondeterministicTransitionIterators =
        new NondeterministicTransitionIterator[mMaxNondeterministicUpdates];
    }
  }
}


#define ADD_NEW_STATE(source) checkTraceState()

void BroadProductExplorer::
expandTraceState(const uint32_t* targettuple, const uint32_t* targetpacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfSignificantWords();
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
  }

  // Establish compact event list ...
  setupCompactEventList(eventmap);
}


void BroadProductExplorer::
setupEventMap(PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  const int numwords = getAutomatonEncoding().getNumberOfSignificantWords();
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
  int maxpass = 1;
  for (int pass = 1; pass <= maxpass; pass++) {
    const jni::IteratorGlue transiter = uniqtrans.iteratorGlue(cache);
    while (transiter.hasNext()) {
      jobject javaobject = transiter.next();
      jni::TransitionGlue trans(javaobject, cache);
      const jni::EventGlue& eventglue = trans.getEventGlue(cache);
      BroadEventRecord* eventrecord = eventmap.get(&eventglue);
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
  (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  mMaxNondeterministicUpdates = 0;
  mNumEventRecords = eventmap.size();
  ExplorerMode mode = getMode();
  HashTableIterator hiter1 = eventmap.iterator();
  while (eventmap.hasNext(hiter1)) {
    const BroadEventRecord* event = eventmap.next(hiter1);
    if (event->isSkippable(mode)) {
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
  if (mMaxNondeterministicUpdates > 0) {
    mNondeterministicTransitionIterators =
      new NondeterministicTransitionIterator[mMaxNondeterministicUpdates];
  }
}


}  /* namespace waters */
