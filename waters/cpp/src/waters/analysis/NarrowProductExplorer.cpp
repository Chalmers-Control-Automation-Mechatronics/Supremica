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
    mNumPlants(0),
    mEventRecords(0),
    mTransitionTables(0),
    mPlantTransitionTables(0),
    mSpecTransitionTables(0),
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
    for (uint32 e = 0; e < mNumEventRecords; e++) {
      delete mEventRecords[e];
    }
    delete[] mEventRecords;
  }
  if (mTransitionTables != 0) {
    const uint32 numaut = getNumberOfAutomata();
    for (uint32 a = 0; a < numaut; a++) {
      mTransitionTables[a].~NarrowTransitionTable();
    }
    delete[] (char*) mTransitionTables;
  }
  delete[] mPlantTransitionTables;
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
  const uint32 numaut = getNumberOfAutomata();
  mNumPlants = 0;
  for (uint32 a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    if (aut->isPlant()) {
      mNumPlants++;
    }
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
  const uint32 numevents = events.size();
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
  uint32 e = 0;
  while (eventmap.hasNext(hiter)) {
    mEventRecords[e++] = eventmap.next(hiter);
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(NarrowEventRecord*),
        NarrowEventRecord::compare);
  for (e = 0; e < mNumEventRecords; e++) {
    mEventRecords[e]->setEventCode(e);
  }

  // Collect transitions ...
  mTransitionTables =
    (NarrowTransitionTable*) new char[numaut * sizeof(NarrowTransitionTable)];
  for (uint32 a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    new (&mTransitionTables[a]) NarrowTransitionTable(aut, cache, eventmap);
  }
  for (uint32 a = 0; a < numaut; a++) {
    mTransitionTables[a].removeSkipped(mEventRecords);
  }
  if (getMode() == EXPLORER_MODE_SAFETY) {
    mPlantTransitionTables = new NarrowTransitionTable*[numaut];
    mSpecTransitionTables = &mPlantTransitionTables[mNumPlants];
    uint32 nextplant = 0;
    uint32 nextspec = 0;
    for (uint32 a = 0; a < numaut; a++) {
      const AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
      if (aut->isPlant()) {
        mPlantTransitionTables[nextplant++] = &mTransitionTables[a];
      } else {
        mSpecTransitionTables[nextspec++] = &mTransitionTables[a];
      }
    }
  }

  // More allocation ...
  mIterator = new uint32[numaut];
  mNondetIterator = new uint32[numaut];
  mCurrentAutomata = new uint32[numaut];
  mTargetTuple = new uint32[numaut];
}

void NarrowProductExplorer::
teardown()
{
  if (mEventRecords != 0) {
    for (uint32 e = 0; e < mNumEventRecords; e++) {
      delete mEventRecords[e];
    }
    mNumEventRecords = 0;
    delete [] mEventRecords;
    mEventRecords = 0;
  }
  if (mTransitionTables != 0) {
    const uint32 numaut = getNumberOfAutomata();
    for (uint32 a = 0; a < numaut; a++) {
      mTransitionTables[a].~NarrowTransitionTable();
    }
    delete [] (char*) mTransitionTables;
    mTransitionTables = 0;
  }
  delete [] mPlantTransitionTables;
  mPlantTransitionTables = mSpecTransitionTables = 0;
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


// I know this is really kludgy,
// but inlining this code is 15% faster than using method calls.
// ~~~Robi

#define EXPAND(source, sourcetuple, minevent, numaut, TAG)              \
  {                                                                     \
    uint32 mincount = UNDEF_UINT32;                                     \
    for (uint32 a = 0; a < numaut; a++) {                               \
      const NarrowTransitionTable& table = mTransitionTables[a];        \
      mIterator[a] = table.iterator(sourcetuple[a]);                    \
      uint32 event = table.getEvent(mIterator[a]);                      \
      if (event < minevent) {                                           \
        minevent = event;                                               \
        mincount = 1;                                                   \
        mCurrentAutomata[0] = a;                                        \
      } else if (event == minevent) {                                   \
        mCurrentAutomata[mincount++] = a;                               \
      }                                                                 \
    }                                                                   \
    while (minevent != UNDEF_UINT32) {                                  \
      if (mincount == mEventRecords[minevent]->getNumberOfAutomata()) { \
        ADD_SUCCESSORS(source, sourcetuple, mincount, numaut, TAG);     \
      }                                                                 \
      uint32 newminevent = UNDEF_UINT32;                                \
      for (uint32 a = 0; a < numaut; a++) {                             \
        const NarrowTransitionTable& table = mTransitionTables[a];      \
        uint32 event = table.getEvent(mIterator[a]);                    \
        if (event == minevent) {                                        \
          mIterator[a] = table.next(mIterator[a]);                      \
          event = table.getEvent(mIterator[a]);                         \
        }                                                               \
        if (event < newminevent) {                                      \
          newminevent = event;                                          \
          mincount = 1;                                                 \
          mCurrentAutomata[0] = a;                                      \
        } else if (event == newminevent) {                              \
          mCurrentAutomata[mincount++] = a;                             \
        }                                                               \
      }                                                                 \
      minevent = newminevent;                                           \
    }                                                                   \
  }

#define ADD_SUCCESSORS(source, sourcetuple, autcount, numaut, TAG)      \
  {                                                                     \
    for (uint32 a = 0; a < numaut; a++) {                               \
      mTargetTuple[a] = sourcetuple[a];                                 \
    }                                                                   \
    uint32 ndcount = 0;                                                 \
    for (uint32 i = 0; i < autcount; i++) {                             \
      const uint32 a = mCurrentAutomata[i];                             \
      const uint32 iter = mIterator[a];                                 \
      const NarrowTransitionTable& table = mTransitionTables[a];        \
      const uint32 raw = table.getRawSuccessors(iter);                  \
      if (raw & TAG) {                                                  \
        mTargetTuple[a] = raw & ~TAG;                                   \
      } else {                                                          \
        mNondetIterator[ndcount] = raw;                                 \
        mCurrentAutomata[ndcount++] = a;                                \
        mTargetTuple[a] = table.getRawNondetSuccessor(raw);             \
      }                                                                 \
    }                                                                   \
    uint32 ndindex = 0;                                                 \
    do {                                                                \
      uint32* packed = getStateSpace().prepare();                       \
      getAutomatonEncoding().encode(mTargetTuple, packed);               \
      ADD_NEW_STATE(source);                                            \
      for (ndindex = 0; ndindex < ndcount; ndindex++) {                 \
        const uint32 a = mCurrentAutomata[ndindex];                     \
        const NarrowTransitionTable& table = mTransitionTables[a];      \
        const uint32 offset = mNondetIterator[ndindex];                 \
        const uint32 raw = table.getRawNondetSuccessor(offset);         \
        if (raw & TAG) {                                                \
          const uint32 iter = mIterator[a];                             \
          const uint32 next =                                           \
            mNondetIterator[ndindex] = table.getRawSuccessors(iter);    \
          mTargetTuple[a] = table.getRawNondetSuccessor(next);          \
        } else {                                                        \
          const uint32 next = mNondetIterator[ndindex] = offset + 1;    \
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
expandSafetyState(const uint32* sourcetuple, const uint32* sourcepacked)
{
  uint32 minevent = UNDEF_UINT32;
  uint32 mincount = UNDEF_UINT32;     
  for (uint32 t = 0; t < mNumPlants; t++) {
    const NarrowTransitionTable* table = mPlantTransitionTables[t];
    const uint32 a = table->getAutomatonIndex();
    mIterator[a] = table->iterator(sourcetuple[a]);
    uint32 event = table->getEvent(mIterator[a]);
    if (event < minevent) {
      minevent = event;
      mincount = 1;
      mCurrentAutomata[0] = a;
    } else if (event == minevent) {
      mCurrentAutomata[mincount++] = a;
    }
  }
  while (minevent != UNDEF_UINT32) {
    if (mincount == mEventRecords[minevent]->getNumberOfPlants()) {
      break;
    }
    uint32 newminevent = UNDEF_UINT32;
    for (uint32 t = 0; t < mNumPlants; t++) {
      const NarrowTransitionTable* table = mPlantTransitionTables[t];
      const uint32 a = table->getAutomatonIndex();
      uint32 event = table->getEvent(mIterator[a]);
      if (event == minevent) {
        mIterator[a] = table->next(mIterator[a]);
        event = table->getEvent(mIterator[a]);
      }
      if (event < newminevent) {
        newminevent = event;
        mincount = 1;
        mCurrentAutomata[0] = a;
      } else if (event == newminevent) {
        mCurrentAutomata[mincount++] = a;
      }
    }
    minevent = newminevent;
  }

  // TBD
  return true;
}


#undef ADD_NEW_STATE
#define ADD_NEW_STATE(source)                                           \
  {                                                                     \
    const uint32 target = getStateSpace().add();                        \
    if (target != source) {                                             \
      setConflictKind(jni::ConflictKind_CONFLICT);                      \
      if (target == getNumberOfStates()) {                              \
        incNumberOfStates();                                            \
      }                                                                 \
      ADD_TRANSITION(source, target);                                   \
    }                                                                   \
  }


bool NarrowProductExplorer::
expandNonblockingReachabilityState(uint32 source,
                                   const uint32* sourcetuple,
                                   const uint32* sourcepacked)
{
  const uint32 numaut = getNumberOfAutomata();
  const uint32 TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  uint32 minevent = UNDEF_UINT32;
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
  } else if (getAutomatonEncoding().isMarkedStateTuple(sourcetuple)) {
    setConflictKind(jni::ConflictKind_CONFLICT);
    return true;
  } else {
    return false;
  }
}


#undef ADD_NEW_STATE
#define ADD_NEW_STATE(source) checkCoreachabilityState()


void NarrowProductExplorer::
expandNonblockingCoreachabilityState(const uint32* targettuple,
                                     const uint32* targetpacked)
{
  const uint32 TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  const uint32 numaut = getNumberOfAutomata();
  uint32 minevent = UNDEF_UINT32;
  EXPAND(TARGET, targettuple, minevent, numaut, TAG);
}


#undef ADD_NEW_STATE


void NarrowProductExplorer::
setupReverseTransitionRelations()
{
  const uint32 numaut = getNumberOfAutomata();
  for (uint32 a = 0; a < numaut; a++) {
    mTransitionTables[a].reverse(mEventRecords);
  }
}


#define ADD_NEW_STATE(source) checkTraceState()


void NarrowProductExplorer::
expandTraceState(const uint32* targettuple, const uint32* targetpacked)
{
  const uint32 TAG = NarrowTransitionTable::TAG_END_OF_LIST;
  const uint32 numaut = getNumberOfAutomata();
  uint32 minevent = UNDEF_UINT32;
  try {
    EXPAND(TARGET, targettuple, minevent, numaut, TAG);
  } catch (const SearchAbort& abort) {
    // OK. That's what we have been waiting for.
    NarrowEventRecord* event = mEventRecords[minevent];
    setTraceEvent(event);
  }
}


#undef ADD_NEW_STATE
#undef ADD_SUCCESSORS
#undef EXPAND

}  /* namespace waters */
