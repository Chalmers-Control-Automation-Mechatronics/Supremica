//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadProductExplorer
//###########################################################################
//# $Id: BroadProductExplorer.cpp,v 1.13 2007-11-02 00:30:37 robi Exp $
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

#include "waters/analysis/AutomatonStateMap.h"
#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/StateRecord.h"
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
BroadProductExplorer(const jni::ProductDESGlue des,
                     const jni::KindTranslatorGlue translator,
                     jni::ClassCache* cache)
  : ProductExplorer(des, translator, cache),
    mNumEventRecords(0),
    mEventRecords(0),
    mReversedEventRecords(0),
    mNumNondetInitialStates(0),
    mNondeterministicTransitionIterators(0),
    mTraceEvent(0)
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
//# BroadProductExplorer: Auxiliary Methods

void BroadProductExplorer::
setup()
{
  // Establish automaton encoding ...
  ProductExplorer::setup();

  // Establish initial event map ...
  jni::ClassCache* cache = getCache();
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  const HashAccessor* eventaccessor = BroadEventRecord::getHashAccessor();
  HashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  mEventRecords = new BroadEventRecord*[numevents];
  mNumEventRecords = 0;
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

  // Collect initial states and transitions ...
  const int numaut = getNumberOfAutomata();
  mNumNondetInitialStates = 0;
  mNondeterministicTransitionIterators =
    new NondeterministicTransitionIterator[numaut];
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* autrecord = getAutomatonEncoding().getRecord(a);
    const jni::AutomatonGlue& aut = autrecord->getJavaAutomaton();
    AutomatonStateMap statemap(cache, autrecord);
    const uint32 numinit = autrecord->getNumberOfInitialStates();
    switch (numinit) {
    case 0:
      setTrivial();
      return;
    case 1:
      break;
    default:
      if (autrecord->isPlant()) {
        mNondeterministicTransitionIterators[mNumNondetInitialStates++].
          setupInit(autrecord);
        break;
      } else {
        const jni::StateGlue& state = statemap.getJavaState(1);
        jni::NondeterministicDESExceptionGlue exception(&aut, &state, cache);
        throw cache->throwJavaException(exception);
      }
    }
    const jni::CollectionGlue transitions = aut.getTransitionsGlue(cache);
    int maxpass = 1;
    for (int pass = 1; pass <= maxpass; pass++) {
      const jni::IteratorGlue transiter = transitions.iteratorGlue(cache);
      while (transiter.hasNext()) {
        jobject javaobject = transiter.next();
        jni::TransitionGlue trans(javaobject, cache);
        const jni::EventGlue& event = trans.getEventGlue(cache);
        BroadEventRecord* eventrecord = eventmap.get(&event);
        const jni::StateGlue& source = trans.getSourceGlue(cache);
        StateRecord* sourcerecord = statemap.getState(source);
        const jni::StateGlue& target = trans.getTargetGlue(cache);
        StateRecord* targetrecord = statemap.getState(target);
        if (pass == 1) {
          const bool det = eventrecord->addDeterministicTransition
            (autrecord, sourcerecord, targetrecord);
          if (!det) {
            if (autrecord->isPlant()) {
              maxpass = 2;
            } else {
              jni::NondeterministicDESExceptionGlue
                exception(&aut, &source, &event, cache);
              throw cache->throwJavaException(exception);
            }
          }
        } else {
          eventrecord->addNondeterministicTransition
            (autrecord, sourcerecord, targetrecord);
        }
      }
    }
    const jni::SetGlue& events = aut.getEventsGlue(cache);
    const jni::IteratorGlue& eventiter = events.iteratorGlue(cache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      jni::EventKind kind = event.getKindGlue(cache);
      if (kind == jni::EventKind_UNCONTROLLABLE ||
          kind == jni::EventKind_CONTROLLABLE) {
        BroadEventRecord* eventrecord = eventmap.get(&event);
        eventrecord->normalize(autrecord);
      }
    }
  }

  // Establish compact event list ...
  mNumEventRecords = eventmap.size();
  HashTableIterator hiter1 = eventmap.iterator();
  while (eventmap.hasNext(hiter1)) {
    const BroadEventRecord* eventrecord = eventmap.next(hiter1);
    if (eventrecord->isSkippable()) {
      mNumEventRecords--;
    }
  }
  mEventRecords = new BroadEventRecord*[mNumEventRecords];
  HashTableIterator hiter2 = eventmap.iterator();
  int i = 0;
  bool trivial = true;
  while (eventmap.hasNext(hiter2)) {
    BroadEventRecord* eventrecord = eventmap.next(hiter2);
    if (!eventrecord->isSkippable()) {
      eventrecord->sortTransitionRecordsForSearch();
      mEventRecords[i++] = eventrecord;
      if (!eventrecord->isControllable() && eventrecord->isDisabledInSpec()) {
        trivial = false;
      }
    }
  }
  if (trivial) {
    setTrivial();
    return;
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(BroadEventRecord*),
        BroadEventRecord::compareForForwardSearch);
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


void BroadProductExplorer::
storeInitialStates()
{
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  uint32* initpacked = getStateSpace().prepare();
  for (int w = 0; w < numwords; w++) {
    initpacked[w] = 0;
  }
  int ndindex;
  do {
    getStateSpace().add();
    initpacked = getStateSpace().prepare(incNumberOfStates());
    for (ndindex = 0; ndindex < mNumNondetInitialStates; ndindex++) {
      if (!mNondeterministicTransitionIterators[ndindex].
          advance(initpacked)) {
        break;
      }
    }
  } while (ndindex < mNumNondetInitialStates);
}


// I know this is really kludgy,
// but inlining this code is 15% faster than using method calls.
// ~~~Robi

#define FIND_DISABLING_AUTOMATON(tuple, event, dis)                     \
  {                                                                     \
    for (TransitionRecord* trans = event->getTransitionRecord();        \
         trans != 0;                                                    \
         trans = trans->getNextInSearch()) {                            \
      const AutomatonRecord* aut = trans->getAutomaton();               \
      const int a = aut->getAutomatonIndex();                           \
      const uint32 source = tuple[a];                                   \
      const uint32 target = trans->getDeterministicSuccessorShifted(source); \
      if (target == TransitionRecord::NO_TRANSITION) {                  \
        dis = aut;                                                      \
        break;                                                          \
      }                                                                 \
    }                                                                   \
  }

#define ADD_NEW_STATE (getStateSpace().add() == getNumberOfStates())

#define EXPAND_ENABLED_TRANSITIONS(numwords, tuple, packedtuple, event) \
  {                                                                     \
    uint32* packednext = getStateSpace().prepare();                     \
    if (event->isDeterministic()) {                                     \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          packednext[w] = packedtuple[w];                               \
        } else {                                                        \
          uint32 word = (packedtuple[w] & update->getKeptMask()) |      \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32 source = tuple[a];                             \
            word |= trans->getDeterministicSuccessorShifted(source);    \
          }                                                             \
          packednext[w] = word;                                         \
        }                                                               \
      }                                                                 \
      if (ADD_NEW_STATE) {                                              \
        incNumberOfStates();                                            \
      }                                                                 \
    } else {                                                            \
      int ndcount = 0;                                                  \
      for (int w = 0; w < numwords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          packednext[w] = packedtuple[w];                               \
        } else {                                                        \
          uint32 word = (packedtuple[w] & update->getKeptMask()) |      \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32 source = tuple[a];                             \
            uint32 succ = trans->getDeterministicSuccessorShifted(source); \
            if (succ == TransitionRecord::MULTIPLE_TRANSITIONS) {       \
              succ = mNondeterministicTransitionIterators[ndcount++].   \
                setup(trans, source);                                   \
            }                                                           \
            word |= succ;                                               \
          }                                                             \
          packednext[w] = word;                                         \
        }                                                               \
      }                                                                 \
      if (ndcount == 0) {                                               \
        if (ADD_NEW_STATE) {                                            \
          incNumberOfStates();                                          \
        }                                                               \
      } else {                                                          \
        int ndindex;                                                    \
        do {                                                            \
          if (ADD_NEW_STATE) {                                          \
            packednext = getStateSpace().prepare(incNumberOfStates());  \
          }                                                             \
          for (ndindex = 0; ndindex < ndcount; ndindex++) {             \
            if (!mNondeterministicTransitionIterators[ndindex].         \
                advance(packednext)) {                                  \
              break;                                                    \
            }                                                           \
          }                                                             \
        } while (ndindex < ndcount);                                    \
      }                                                                 \
    }                                                                   \
  }


bool BroadProductExplorer::
expandState(const uint32* currenttuple, const uint32* currentpacked)
{
  const int numwords = getAutomatonEncoding().getNumberOfWords();
  for (int e = 0; e < mNumEventRecords; e++) {
    const BroadEventRecord* event = mEventRecords[e];
    const AutomatonRecord* dis = 0;
    FIND_DISABLING_AUTOMATON(currenttuple, event, dis);
    if (dis == 0) {
      EXPAND_ENABLED_TRANSITIONS(numwords, currenttuple,
                                 currentpacked, event);
    } else if (!dis->isPlant() && !event->isControllable()) {
      mTraceEvent = event;
      return false;
    }
  }
  return true;
}




const jni::EventGlue& BroadProductExplorer::
getTraceEvent()
{
  return mTraceEvent->getJavaEvent();
}


void BroadProductExplorer::
setupReverseTransitionRelations()
{
  int numreversed = 0;
  mReversedEventRecords = new BroadEventRecord*[mNumEventRecords];
  for (int e = 0; e < mNumEventRecords; e++) {
    BroadEventRecord* event = mEventRecords[e];
    if (!event->isOnlySelfloops() && event->reverse()) {
      mReversedEventRecords[numreversed++] = event;
    }
  }
  qsort(mReversedEventRecords, numreversed, sizeof(BroadEventRecord*),
        BroadEventRecord::compareForBackwardSearch);
}


#undef ADD_NEW_STATE
#define ADD_NEW_STATE checkTraceState()

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
        EXPAND_ENABLED_TRANSITIONS(numwords, targettuple,
                                   targetpacked, event);
      }
    } while (true);
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    mTraceEvent = event;
  }
}


}  /* namespace waters */
