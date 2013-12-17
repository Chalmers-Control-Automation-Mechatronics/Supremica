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
#include "jni/glue/MapGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/NarrowEventRecord.h"
#include "waters/analysis/NarrowProductExplorer.h"
#include "waters/analysis/NarrowPreTransitionTable.h"
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
                      const jni::EventGlue& premarking,
                      const jni::EventGlue& marking,
                      jni::ClassCache* cache)
  : ProductExplorer(factory, des, translator, premarking, marking, cache),
    mNumEventRecords(0),
    mFirstSpecOnlyUncontrollable(0),
    mNumPlants(0),
    mEventRecords(0),
    mTransitionTables(0),
    mNonReversedTransitionTables(0),
    mIterator(0),
    mNondetIterator(0),
    mCurrentAutomata(0),
    mTargetTuple(0)
{
}

NarrowProductExplorer::
~NarrowProductExplorer()
{
  if (mEventRecords != 0) {
    for (uint32_t e = 0; e < mNumEventRecords; e++) {
      delete mEventRecords[e];
    }
    delete[] mEventRecords;
  }
  const uint32_t numaut = getNumberOfAutomata();
  if (mTransitionTables != 0) {
    for (uint32_t a = 0; a < numaut; a++) {
      mTransitionTables[a].~NarrowTransitionTable();
    }
    delete[] (char*) mTransitionTables;
  }
  if (mNonReversedTransitionTables != 0) {
    for (uint32_t a = 0; a < numaut; a++) {
      mNonReversedTransitionTables[a].~NarrowTransitionTable();
    }
    delete[] (char*) mNonReversedTransitionTables;
  }
  delete[] mIterator;
  delete[] mNondetIterator;
  delete[] mCurrentAutomata;
  delete[] mTargetTuple;
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
  const uint32_t numaut = getNumberOfAutomata();
  mNumPlants = 0;
  for (uint32_t a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    if (aut->isPlant()) {
      mNumPlants++;
    }
    if (aut->getNumberOfInitialStates() == 0) {
      setTrivial();
    }
  }

  // Establish initial event map ...
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const uint32_t numevents = events.size();
  const EventRecordHashAccessor* eventaccessor =
    NarrowEventRecord::getHashAccessor();
  PtrHashTable<const jni::EventGlue*,NarrowEventRecord*>
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

  // Build pre-transition tables ...
  NarrowPreTransitionTable* pretrans = (NarrowPreTransitionTable*)
    new char[numaut * sizeof(NarrowPreTransitionTable)];
  for (uint32_t a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    new (&pretrans[a]) NarrowPreTransitionTable(aut, cache, eventmap);
  }

  // Establish event ordering ...
  mNumEventRecords = eventmap.size();
  mEventRecords = new NarrowEventRecord*[mNumEventRecords];
  HashTableIterator hiter = eventmap.iterator();
  uint32_t e = 0;
  while (eventmap.hasNext(hiter)) {
    mEventRecords[e++] = eventmap.next(hiter);
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(NarrowEventRecord*),
        NarrowEventRecord::compare);
  e = mFirstSpecOnlyUncontrollable = mNumEventRecords;
  while (e > 0) {
    NarrowEventRecord* event = mEventRecords[--e];
    event->setEventCode(e);
    if (event->isSpecOnly() && !event->isControllable()) {
      mFirstSpecOnlyUncontrollable = e;
    }
  }

  // Collect transitions ...
  mNonReversedTransitionTables = 0;
  mTransitionTables =
    (NarrowTransitionTable*) new char[numaut * sizeof(NarrowTransitionTable)];
  for (uint32_t a = 0; a < numaut; a++) {
    NarrowPreTransitionTable* pre = &pretrans[a];
    new (&mTransitionTables[a]) NarrowTransitionTable(pre, cache, eventmap);
    // mTransitionTables[a].dump(a, mEventRecords);
    pretrans[a].~NarrowPreTransitionTable();    
  }
  delete (const char*) pretrans;

  // More allocation ...
  mIterator = new uint32_t[numaut];
  mNondetIterator = new uint32_t[numaut];
  mCurrentAutomata = new uint32_t[numaut];
  mTargetTuple = new uint32_t[numaut];
}


void NarrowProductExplorer::
teardown()
{
  if (mEventRecords != 0) {
    for (uint32_t e = 0; e < mNumEventRecords; e++) {
      delete mEventRecords[e];
    }
    mNumEventRecords = 0;
    delete [] mEventRecords;
    mEventRecords = 0;
  }
  const uint32_t numaut = getNumberOfAutomata();
  if (mTransitionTables != 0) {
    for (uint32_t a = 0; a < numaut; a++) {
      mTransitionTables[a].~NarrowTransitionTable();
    }
    delete [] (char*) mTransitionTables;
    mTransitionTables = 0;
  }
  if (mNonReversedTransitionTables != 0) {
    for (uint32_t a = 0; a < numaut; a++) {
      mNonReversedTransitionTables[a].~NarrowTransitionTable();
    }
    delete[] (char*) mNonReversedTransitionTables;
    mNonReversedTransitionTables = 0;
  }
  mNumPlants = 0;
  delete [] mIterator;
  mIterator = 0;
  delete [] mNondetIterator;
  mNondetIterator = 0;
  delete [] mCurrentAutomata;
  mCurrentAutomata = 0;
  delete [] mTargetTuple;
  mTargetTuple = 0;
  ProductExplorer::teardown();
}


#define EXPAND(source, sourcetuple, minevent, numaut, TAG)              \
  {                                                                     \
    minevent = mNumEventRecords;                                        \
    uint32_t mincount = UINT32_MAX;                                     \
    for (uint32_t a = 0; a < numaut; a++) {                             \
      const NarrowTransitionTable& table = mTransitionTables[a];        \
      mIterator[a] = table.iterator(sourcetuple[a]);                    \
      uint32_t e = table.getEvent(mIterator[a]);                        \
      if (e < minevent) {                                               \
        minevent = e;                                                   \
        mincount = 1;                                                   \
        mCurrentAutomata[0] = a;                                        \
      } else if (e == minevent) {                                       \
        mCurrentAutomata[mincount++] = a;                               \
      }                                                                 \
    }                                                                   \
    while (minevent < mNumEventRecords) {                               \
      if (mincount == mEventRecords[minevent]->getNumberOfAutomata()) { \
        ADD_SUCCESSORS(source, sourcetuple, mincount, numaut, TAG);     \
      }                                                                 \
      uint32_t newminevent = mNumEventRecords;                          \
      for (uint32_t a = 0; a < numaut; a++) {                           \
        const NarrowTransitionTable& table = mTransitionTables[a];      \
        uint32_t e = table.getEvent(mIterator[a]);                      \
        if (e == minevent) {                                            \
          mIterator[a] = table.next(mIterator[a]);                      \
          e = table.getEvent(mIterator[a]);                             \
        }                                                               \
        if (e < newminevent) {                                          \
          newminevent = e;                                              \
          mincount = 1;                                                 \
          mCurrentAutomata[0] = a;                                      \
        } else if (e == newminevent) {                                  \
          mCurrentAutomata[mincount++] = a;                             \
        }                                                               \
      }                                                                 \
      minevent = newminevent;                                           \
    }                                                                   \
  }

#define ADD_SUCCESSORS(source, sourcetuple, autcount, numaut, TAG)      \
  {                                                                     \
    for (uint32_t a = 0; a < numaut; a++) {                               \
      mTargetTuple[a] = sourcetuple[a];                                 \
    }                                                                   \
    uint32_t ndcount = 0;                                                 \
    for (uint32_t i = 0; i < autcount; i++) {                             \
      const uint32_t a = mCurrentAutomata[i];                             \
      const uint32_t iter = mIterator[a];                                 \
      const NarrowTransitionTable& table = mTransitionTables[a];        \
      const uint32_t raw = table.getRawSuccessors(iter);                  \
      if (raw & TAG) {                                                  \
        mTargetTuple[a] = raw & ~TAG;                                   \
      } else {                                                          \
        mNondetIterator[ndcount] = raw;                                 \
        mCurrentAutomata[ndcount++] = a;                                \
        mTargetTuple[a] = table.getRawNondetSuccessor(raw);             \
      }                                                                 \
    }                                                                   \
    uint32_t ndindex = 0;                                                 \
    do {                                                                \
      uint32_t* packed = getStateSpace().prepare();                       \
      getAutomatonEncoding().encode(mTargetTuple, packed);              \
      ADD_NEW_STATE(source);                                            \
      for (ndindex = 0; ndindex < ndcount; ndindex++) {                 \
        const uint32_t a = mCurrentAutomata[ndindex];                     \
        const NarrowTransitionTable& table = mTransitionTables[a];      \
        const uint32_t offset = mNondetIterator[ndindex];                 \
        const uint32_t raw = table.getRawNondetSuccessor(offset);         \
        if (raw & TAG) {                                                \
          const uint32_t iter = mIterator[a];                             \
          const uint32_t next =                                           \
            mNondetIterator[ndindex] = table.getRawSuccessors(iter);    \
          mTargetTuple[a] = table.getRawNondetSuccessor(next);          \
        } else {                                                        \
          const uint32_t next = mNondetIterator[ndindex] = offset + 1;    \
          mTargetTuple[a] = table.getRawNondetSuccessor(next) & ~TAG;   \
          break;                                                        \
        }                                                               \
      }                                                                 \
    } while (ndindex < ndcount);                                        \
  }



#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    if (getStateSpace().add() == getNumberOfStates()) {                 \
      incNumberOfStates();                                              \
    }                                                                   \
    incNumberOfTransitions();                                           \
  }


bool NarrowProductExplorer::
expandSafetyState(const uint32_t* sourcetuple, const uint32_t* sourcepacked)
{
  const uint32_t numaut = getNumberOfAutomata();
  const uint32_t TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  uint32_t minevent = mNumEventRecords;
  uint32_t mincount = UINT32_MAX;
  uint32_t plantcount = UINT32_MAX;
  uint32_t speconly = mFirstSpecOnlyUncontrollable;

  for (uint32_t a = 0; a < numaut; a++) {
    const NarrowTransitionTable& table = mTransitionTables[a];
    mIterator[a] = table.iterator(sourcetuple[a]);
    uint32_t e = table.getEvent(mIterator[a]);
    if (e < minevent) {
      minevent = e;
      mCurrentAutomata[0] = a;
      mincount = 1;
      plantcount = table.isPlant() ? 1 : 0;
    } else if (e == minevent) {
      mCurrentAutomata[mincount++] = a;
      if (table.isPlant()) {
        plantcount++;
      }
    }
  }

  while (minevent < mNumEventRecords) {
    if (minevent >= speconly) {
      if (minevent == speconly) {
        speconly++;
      } else {
        const NarrowEventRecord* event = mEventRecords[speconly];
        setTraceEvent(event);
        return false;
      }
    }
    const NarrowEventRecord* event = mEventRecords[minevent];
    if (mincount == event->getNumberOfAutomata()) {
      ADD_SUCCESSORS(SOURCE, sourcetuple, mincount, numaut, TAG);
    } else if (event->isControllable()) {
      // controllable event blocked, no problem ...
    } else if (plantcount == event->getNumberOfPlants()) {
      // uncontrollable event accepted by plant, but not spec ...
      setTraceEvent(event);
      return false;
    }
    uint32_t newminevent = mNumEventRecords;
    for (uint32_t a = 0; a < numaut; a++) {
      const NarrowTransitionTable& table = mTransitionTables[a];
      uint32_t e = table.getEvent(mIterator[a]);
      if (e == minevent) {
        mIterator[a] = table.next(mIterator[a]);
        e = table.getEvent(mIterator[a]);
      }
      if (e < newminevent) {
        newminevent = e;
        mCurrentAutomata[0] = a;
        mincount = 1;
        plantcount = table.isPlant() ? 1 : 0;
      } else if (e == newminevent) {
        mCurrentAutomata[mincount++] = a;
        if (table.isPlant()) {
          plantcount++;
        }
      }
    }
    minevent = newminevent;
  }

  if (speconly < mNumEventRecords) {
    const NarrowEventRecord* event = mEventRecords[speconly];
    setTraceEvent(event);
    return false;
  }
  return true;
}


#undef ADD_NEW_STATE
#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    const uint32_t target = getStateSpace().add();                        \
    if (target != source) {                                             \
      setConflictKind(jni::ConflictKind_CONFLICT);                      \
      if (target == getNumberOfStates()) {                              \
        incNumberOfStates();                                            \
      }                                                                 \
      ADD_TRANSITION(source, target);                                   \
    }                                                                   \
  }


bool NarrowProductExplorer::
expandNonblockingReachabilityState(uint32_t source,
                                   const uint32_t* sourcetuple,
                                   const uint32_t* sourcepacked)
{
  const uint32_t numaut = getNumberOfAutomata();
  const uint32_t TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  uint32_t minevent = UINT32_MAX;
  setConflictKind(jni::ConflictKind_DEADLOCK);
  if (getTransitionLimit() > 0) {
#   define ADD_TRANSITION addCoreachabilityTransition
    EXPAND(source, sourcetuple, minevent, numaut, TAG);
#   undef ADD_TRANSITION
  } else {
#   define ADD_TRANSITION(source,target) incNumberOfTransitions()
    EXPAND(source, sourcetuple, minevent, numaut, TAG);
#   undef ADD_TRANSITION
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


void NarrowProductExplorer::
expandNonblockingCoreachabilityState(const uint32_t* targettuple,
                                     const uint32_t* targetpacked)
{
  const uint32_t TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  const uint32_t numaut = getNumberOfAutomata();
  uint32_t minevent = UINT32_MAX;
  EXPAND(TARGET, targettuple, minevent, numaut, TAG);
}


#undef ADD_NEW_STATE


void NarrowProductExplorer::
setupReverseTransitionRelations()
{
  if (mNonReversedTransitionTables == 0) {
    const uint32_t numaut = getNumberOfAutomata();
    mNonReversedTransitionTables = mTransitionTables;
    mTransitionTables = (NarrowTransitionTable*)
      new char[numaut * sizeof(NarrowTransitionTable)];
    for (uint32_t a = 0; a < numaut; a++) {
      // mNonReversedTransitionTables[a].dump(a, mEventRecords);
      const NarrowTransitionTable* orig = &mNonReversedTransitionTables[a];
      new (&mTransitionTables[a]) NarrowTransitionTable(orig, mEventRecords);
      // mTransitionTables[a].dump(a, mEventRecords);
    }
  }
}


#define ADD_NEW_STATE(source) checkTraceState()

void NarrowProductExplorer::
expandTraceState(const uint32_t* targettuple, const uint32_t* targetpacked)
{
  const uint32_t TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  const uint32_t numaut = getNumberOfAutomata();
  uint32_t minevent = UINT32_MAX;
  try {
    EXPAND(TARGET, targettuple, minevent, numaut, TAG);
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    NarrowEventRecord* event = mEventRecords[minevent];
    setTraceEvent(event);
  }
}

#undef ADD_NEW_STATE


#define ADD_NEW_STATE(source)                                         \
  uint32_t offset = getStateSpace().size();                           \
  const uint32_t* foundpacked = getStateSpace().get(offset);          \
  if (getStateSpace().equalTuples(foundpacked, targetpacked)) {       \
    throw SearchAbort();                                              \
  }

const EventRecord* NarrowProductExplorer::
findEvent(const uint32_t* sourcepacked,
          const uint32_t* sourcetuple,
          const uint32_t* targetpacked)
{
  const uint32_t TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  const uint32_t numaut = getNumberOfAutomata();
  uint32_t minevent = UINT32_MAX;
  try {
    EXPAND(SOURCE, sourcetuple, minevent, numaut, TAG);
    return 0;
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    return mEventRecords[minevent];
  }
}

#undef ADD_NEW_STATE
#undef ADD_SUCCESSORS
#undef EXPAND


void NarrowProductExplorer::
storeNondeterministicTargets(const uint32_t* sourcetuple,
                             const uint32_t* targettuple,
                             const jni::MapGlue& statemap)
{
  const uint32_t numaut = getNumberOfAutomata();
  const NarrowEventRecord* event = (const NarrowEventRecord*) getTraceEvent();
  const uint32_t e = event->getEventCode();
  for (uint32_t a = 0; a < numaut; a++) {
    const NarrowTransitionTable& table =
      mNonReversedTransitionTables ?
      mNonReversedTransitionTables[a] : mTransitionTables[a];
    uint32_t iter = table.iterator(sourcetuple[a]);
    uint32_t current = table.getEvent(iter);
    while (current < e) {
      iter = table.next(iter);
      current = table.getEvent(iter);
    }
    if (current == e &&
        (table.getRawSuccessors(iter) &
         NarrowTransitionTable::TAG_END_OF_LIST) == 0) {
      const AutomatonRecord* autrecord = getAutomatonEncoding().getRecord(a);
      const jni::AutomatonGlue& aut = autrecord->getJavaAutomaton();
      const jni::StateGlue& state = autrecord->getJavaState(targettuple[a]);
      statemap.put(&aut, &state);
    }
  }
}


}  /* namespace waters */
