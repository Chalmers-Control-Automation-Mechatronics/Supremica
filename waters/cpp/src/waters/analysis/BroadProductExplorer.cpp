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
#include "waters/analysis/EventTree.h"
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
    mDumpStates(0)
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


//############################################################################
//# BroadProductExplorer: State Expansion Procedures

//----------------------------------------------------------------------------
// isLocalDumpState()

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


//----------------------------------------------------------------------------
// setupReverseTransitionRelations()

void BroadProductExplorer::
setupReverseTransitionRelations()
{
  if (mReversedEventRecords == 0) {
    int maxupdates = 0;
    BroadEventRecord** reversed = new BroadEventRecord*[mNumEventRecords];
    for (int e = 0; e < mNumEventRecords; e++) {
      BroadEventRecord* event = mEventRecords[e];
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


//----------------------------------------------------------------------------
// expandSafetyState()

// I know this is really kludgy,
// but inlining this code is 15% faster than using method calls.
// ~~~Robi

#define EXPAND_RAW(source, sourceTuple, sourcePacked,                   \
                   records, numRecords, callBack)                       \
  {                                                                     \
    const int numWords = getAutomatonEncoding().getEncodingSize();      \
    for (int e = 0; e < numRecords; e++) {                              \
      BroadEventRecord* event = records[e];                             \
      const AutomatonRecord* dis = 0;                                   \
      FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);                \
      if (dis == 0) {                                                   \
        EXPAND_ENABLED_TRANSITIONS                                      \
          (numWords, source, sourceTuple, sourcePacked, event, callBack); \
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

#define EXPAND_ENABLED_TRANSITIONS(numWords, source,                    \
                                   sourceTuple, sourcePacked,           \
                                   event, callBack)                     \
  {                                                                     \
    uint32_t* bufferPacked = getStateSpace().prepare();                 \
    if (event->isDeterministic()) {                                     \
      for (int w = 0; w < numWords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferPacked[w] = sourcePacked[w];                            \
        } else {                                                        \
          uint32_t word =                                               \
            (sourcePacked[w] & update->getKeptMask()) |                 \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32_t source = sourceTuple[a];                     \
            word |= trans->getDeterministicSuccessorShifted(source);    \
          }                                                             \
          bufferPacked[w] = word;                                       \
        }                                                               \
      }                                                                 \
      ADD_NEW_STATE(source, event, callBack, INC_NO_ALLOC);             \
    } else {                                                            \
      int ndend = 0;                                                    \
      for (int w = 0; w < numWords; w++) {                              \
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w); \
        if (update == 0) {                                              \
          bufferPacked[w] = sourcePacked[w];                            \
        } else {                                                        \
          uint32_t word = (sourcePacked[w] & update->getKeptMask()) |   \
            update->getCommonTargets();                                 \
          for (TransitionRecord* trans = update->getTransitionRecords(); \
               trans != 0;                                              \
               trans = trans->getNextInUpdate()) {                      \
            const AutomatonRecord* aut = trans->getAutomaton();         \
            const int a = aut->getAutomatonIndex();                     \
            const uint32_t source = sourceTuple[a];                     \
            uint32_t succ = trans->getDeterministicSuccessorShifted(source); \
            if (succ == TransitionRecord::MULTIPLE_TRANSITIONS) {       \
              succ = mNondeterministicTransitionIterators[ndend++].     \
                setup(trans, source);                                   \
            }                                                           \
            word |= succ;                                               \
          }                                                             \
          bufferPacked[w] = word;                                       \
        }                                                               \
      }                                                                 \
      if (ndend == 0) {                                                 \
        ADD_NEW_STATE(source, event, callBack, INC_NO_ALLOC);           \
      } else {                                                          \
        int ndindex;                                                    \
        do {                                                            \
          ADD_NEW_STATE(source, event, callBack, INC_ALLOC);            \
          for (ndindex = 0; ndindex < ndend; ndindex++) {               \
            if (!mNondeterministicTransitionIterators[ndindex].         \
                advance(bufferPacked)) {                                \
              break;                                                    \
            }                                                           \
          }                                                             \
        } while (ndindex < ndend);                                      \
      }                                                                 \
    }                                                                   \
  }

#define INC_NO_ALLOC incNumberOfStates()
#define INC_ALLOC bufferPacked = getStateSpace().prepare(incNumberOfStates())

#define ADD_NEW_STATE(source, event, callBack, INC)                     \
  {                                                                     \
    incNumberOfTransitionsExplored();                                   \
    uint32_t target = getStateSpace().add();                            \
    if (source != target) {                                             \
      incNumberOfTransitions();                                         \
      if (target == getNumberOfStates()) {                              \
        INC;                                                            \
      }                                                                 \
      if (callBack != 0) {                                              \
        bool cont = (this->*callBack)(source, event, target);           \
        if (!cont) {                                                    \
          return false;                                                 \
        }                                                               \
      }                                                                 \
    }                                                                   \
  }


bool BroadProductExplorer::
expandSafetyState(uint32_t source,
                  const uint32_t* sourceTuple,
                  const uint32_t* sourcePacked,
                  TransitionCallBack callBack)
{
  const int numWords = getAutomatonEncoding().getEncodingSize();
  for (int e = 0; e < mNumEventRecords; e++) {
    const BroadEventRecord* event = mEventRecords[e];
    const AutomatonRecord* dis = 0;
    FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);
    if (dis == 0) {
      EXPAND_ENABLED_TRANSITIONS
        (numWords, source, sourceTuple, sourcePacked, event, callBack);
    } else if (!dis->isPlant() && !event->isControllable()) {
      setTraceEvent(event, dis);
      return false;
    }
  }
  return true;
}


//----------------------------------------------------------------------------
// expandForward()

bool BroadProductExplorer::
expandForward(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              TransitionCallBack callBack)
{
  EXPAND_RAW(source, sourceTuple, sourcePacked,
             mEventRecords, mNumEventRecords, callBack);
  return true;
}

#undef ADD_NEW_STATE


//----------------------------------------------------------------------------
// expandForwardAgain()

#define ADD_NEW_STATE(source, event, callBack, INC)                     \
  {                                                                     \
    incNumberOfTransitionsExplored();                                   \
    uint32_t target = getStateSpace().find();                           \
    if (source != target && callBack != 0) {                            \
      bool cont = (this->*callBack)(source, event, target);             \
      if (!cont) {                                                      \
        return false;                                                   \
      }                                                                 \
    }                                                                   \
  }

bool BroadProductExplorer::
expandForwardAgain(uint32_t source,
                   const uint32_t* sourceTuple,
                   const uint32_t* sourcePacked,
                   TransitionCallBack callBack)
{
  EXPAND_RAW(source, sourceTuple, sourcePacked,
             mEventRecords, mNumEventRecords, callBack);
  return true;
}

#undef ADD_NEW_STATE


//----------------------------------------------------------------------------
// expandReverse()

#define ADD_NEW_STATE(source, event, callBack, INC)                     \
  {                                                                     \
    incNumberOfTransitionsExplored();                                   \
    uint32_t target = getStateSpace().find();                           \
    if (target != UINT32_MAX && source != target && callBack != 0) {    \
      bool cont = (this->*callBack)(source, event, target);             \
      if (!cont) {                                                      \
        return false;                                                   \
      }                                                                 \
    }                                                                   \
  }

bool BroadProductExplorer::
expandReverse(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              TransitionCallBack callBack)
{
  EXPAND_RAW(source, sourceTuple, sourcePacked,
             mReversedEventRecords, mNumReversedEventRecords, callBack);
  return true;
}

#undef ADD_NEW_STATE


//----------------------------------------------------------------------------
// expandTraceState()

void BroadProductExplorer::
expandTraceState(const uint32_t target,
                 const uint32_t* targetTuple,
                 const uint32_t* targetPacked,
                 uint32_t level)
{
  const int numwords = getAutomatonEncoding().getEncodingSize();
  const uint32_t prevLevelStart = getFirstState(level - 1);
  const uint32_t prevLevelEnd = getFirstState(level);
  const int prevLevelSize = prevLevelEnd - prevLevelStart;
  const BroadEventRecord* event = 0;
  const BroadEventRecord** deferred = 0;
  uint32_t* sourceTuple = new uint32_t[getNumberOfAutomata()];
  try {
#   define ADD_NEW_STATE(source, event, callBack, INC) {        \
      incNumberOfTransitionsExplored();                         \
      uint32_t found = getStateSpace().find();                  \
      if (found < prevLevelEnd) {                               \
        setTraceState(found);                                   \
        throw SearchAbort();                                    \
      }                                                         \
    }
    int numDeferred = 0;
    for (int e = 0; e < mNumReversedEventRecords; e++) {
      event = mReversedEventRecords[e];
      const AutomatonRecord* dis = 0;
      FIND_DISABLING_AUTOMATON(targetTuple, event, dis);
      if (dis == 0) {
        if (event->getFanout(targetTuple) <= prevLevelSize) {
          EXPAND_ENABLED_TRANSITIONS
            (numwords, target, targetTuple, targetPacked, event, 0);
        } else {
          // If the event has too many transitions to this state,
          // do not try to expand. Search later in forward direction,
          // if really necessary ...
          if (deferred == 0) {
            deferred = new const BroadEventRecord*[mNumReversedEventRecords];
          }
          deferred[numDeferred++] = event;
        }
      }
    }
#   undef ADD_NEW_STATE
#   define ADD_NEW_STATE(source, event, callBack, INC) {                \
      incNumberOfTransitionsExplored();                                 \
      if (getStateSpace().equalTuples(bufferPacked, targetPacked)) {    \
        setTraceState(source);                                          \
        throw SearchAbort();                                            \
      }                                                                 \
    }
    for (uint32_t source = prevLevelStart; source < prevLevelEnd; source++) {
      uint32_t* sourcePacked = getStateSpace().get(source);
      getAutomatonEncoding().decode(sourcePacked, sourceTuple);
      for (int e = 0; e < numDeferred; e++) {
        event = deferred[e]->getForwardRecord();
        const AutomatonRecord* dis = 0;
        FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);
        if (dis == 0) {
          EXPAND_ENABLED_TRANSITIONS
            (numwords, source, sourceTuple, sourcePacked, event, 0);
        }
      }
    }
#   undef ADD_NEW_STATE
    // We should never get here ...
    delete [] deferred;
    delete [] sourceTuple;
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    setTraceEvent(event);
    delete [] deferred;
    delete [] sourceTuple;
  } catch (...) {
    delete [] deferred;
    delete [] sourceTuple;
    throw;
  }
}


//----------------------------------------------------------------------------
// findEvent()

#define ADD_NEW_STATE(source, event, callBack, INC)              \
  incNumberOfTransitionsExplored();                              \
  if (getStateSpace().equalTuples(bufferPacked, targetPacked)) { \
    foundEvent = event;                                          \
    throw SearchAbort();                                         \
  }

const EventRecord* BroadProductExplorer::
findEvent(const uint32_t* sourcePacked,
          const uint32_t* sourceTuple,
          const uint32_t* targetPacked)
{
  const BroadEventRecord* foundEvent = 0;
  try {
    EXPAND_RAW(source, sourceTuple, sourcePacked,
               mEventRecords, mNumEventRecords, 0);
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
  }
  return foundEvent;
}

#undef ADD_NEW_STATE


//----------------------------------------------------------------------------
// storeNondeterministicTargets()

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
  while (eventmap.hasNext(hiter2)) {
    BroadEventRecord* event = eventmap.next(hiter2);
    if (event->isSkippable(checkType)) {
      delete event;
    } else {
      event->optimizeTransitionRecordsForSearch(checkType);
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
