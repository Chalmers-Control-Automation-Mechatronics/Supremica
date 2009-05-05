//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id: SafetyVerifier.cpp,v 1.13 2007-11-02 00:30:37 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>
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
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/EventRecord.h"
#include "waters/analysis/SafetyVerifier.h"
#include "waters/analysis/StateRecord.h"
#include "waters/analysis/StateSpace.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/analysis/TransitionUpdateRecord.h"
#include "waters/base/HashTable.h"
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# exception TraceAbort
//############################################################################

class TraceAbort {
public:
  //##########################################################################
  //# Constructors & Destructors
  TraceAbort() {}
  ~TraceAbort() {}
};


//############################################################################
//# class SafetyVerifier
//############################################################################

//############################################################################
//# SafetyVerifier: Constructors & Destructors

SafetyVerifier::
SafetyVerifier(const jni::ProductDESGlue des,
               const jni::KindTranslatorGlue translator,
               jni::ClassCache* cache)
  : mCache(cache),
    mModel(des),
    mKindTranslator(translator),
    mStateLimit(UNDEF_UINT32),
    mEncoding(0),
    mStateSpace(0),
    mDepthMap(0),
    mNumEventRecords(0),
    mIsTrivial(false),
    mEventRecords(0),
    mNondeterministicTransitionIterators(0),
    mTraceState(UNDEF_UINT32),
    mTraceEvent(0),
    mTraceList(0),
    mNumStates(0)
{
}


SafetyVerifier::
~SafetyVerifier()
{
  delete mEncoding;
  delete mStateSpace;
  delete mDepthMap;
  for (int i = 0; i < mNumEventRecords; i++) {
    delete mEventRecords[i];
  }
  delete [] mEventRecords;
  delete [] mNondeterministicTransitionIterators;
  delete mTraceList;
}


//############################################################################
//# SafetyVerifier: Invocation

bool SafetyVerifier::
run()
{
  try {
    // const jni::JavaString name(mCache->getEnvironment(), mModel.getName());
    // std::cerr << (const char*) name << std::endl;
    setup();
    bool result = checkProperty();
    if (!result) {
      computeCounterExample();
    }
    teardown();
    return result;
  } catch (...) {
    teardown();
    throw;
  }
}

jni::SafetyTraceGlue SafetyVerifier::
getCounterExample(const jni::ProductDESProxyFactoryGlue& factory)
  const
{
  return factory.createSafetyTraceProxyGlue(&mModel, mTraceList, mCache);
}


void SafetyVerifier::
addStatistics(const jni::VerificationResultGlue& vresult)
  const
{
  vresult.setNumberOfAutomata(mNumAutomata);
  vresult.setNumberOfStates(mNumStates);
  vresult.setPeakNumberOfNodes(mNumStates);
}


//############################################################################
//# SafetyVerifier: Auxiliary Methods

void SafetyVerifier::
setup()
{
  // Establish automaton encoding ...
  mEncoding = new AutomatonEncoding(mModel, mKindTranslator, mCache);
  // mEncoding->dump();
  mIsTrivial = true;
  if (!mEncoding->hasSpecs()) {
    return;
  }
  mNumAutomata = mEncoding->getNumberOfRecords();
  mStateSpace = new StateSpace(mEncoding, mStateLimit);
  mDepthMap = new ArrayList<uint32>(128);

  // Establish initial event map ...
  const int numwords = mEncoding->getNumberOfWords();
  const jni::SetGlue events = mModel.getEventsGlue(mCache);
  const int numevents = events.size();
  const jni::IteratorGlue iter = events.iteratorGlue(mCache);
  const HashAccessor* eventaccessor = EventRecord::getHashAccessor();
  HashTable<jni::EventGlue*,EventRecord*> eventmap(eventaccessor, numevents);
  mEventRecords = new EventRecord*[numevents];
  mNumEventRecords = 0;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::EventGlue event(javaobject, mCache);
    bool controllable;
    switch (mKindTranslator.getEventKindGlue(&event, mCache)) {
    case jni::EventKind_UNCONTROLLABLE:
      controllable = false;
      break;
    case jni::EventKind_CONTROLLABLE:
      controllable = true;
      break;
    default:
      continue;
    }
    EventRecord* record = new EventRecord(event, controllable, numwords);
    eventmap.add(record);
  }

  // Collect initial states and transitions ...
  mNumNondetInitialStates = 0;
  mNondeterministicTransitionIterators =
    new NondeterministicTransitionIterator[mNumAutomata];
  const HashAccessor* stateaccessor = StateRecord::getHashAccessor();
  HashTable<jni::StateGlue*,StateRecord*> statemap(stateaccessor, 256);
  for (int a = 0; a < mNumAutomata; a++) {
    const AutomatonRecord* autrecord = mEncoding->getRecord(a);
    const jni::AutomatonGlue aut = autrecord->getJavaAutomaton();
    const jni::SetGlue states = aut.getStatesGlue(mCache);
    const jni::IteratorGlue stateiter = states.iteratorGlue(mCache);
    uint32 nextinit = 0;
    uint32 nextnoninit = states.size() - 1;
    while (stateiter.hasNext()) {
      jobject javaobject = stateiter.next();
      jni::StateGlue state(javaobject, mCache);
      uint32 code;
      if (!state.isInitial()) {
        code = nextnoninit--;
      } else if (nextinit == 0 || autrecord->isPlant()) {
        code = nextinit++;
      } else {
        jni::NondeterministicDESExceptionGlue exception(&aut, &state, mCache);
        throw mCache->throwJavaException(exception);
      }
      StateRecord* staterecord = new StateRecord(state, code, mCache);
      statemap.add(staterecord);
    }
    mEncoding->setNumberOfInitialStates(a, nextinit);
    switch (nextinit) {
    case 0:
      mIsTrivial = true;
      return;
    case 1:
      break;
    default:
      mNondeterministicTransitionIterators[mNumNondetInitialStates++].
        setupInit(autrecord);
      break;
    }
    const jni::CollectionGlue transitions = aut.getTransitionsGlue(mCache);
    int maxpass = 1;
    for (int pass = 1; pass <= maxpass; pass++) {
      const jni::IteratorGlue transiter = transitions.iteratorGlue(mCache);
      while (transiter.hasNext()) {
        jobject javaobject = transiter.next();
        jni::TransitionGlue trans(javaobject, mCache);
        jni::EventGlue event = trans.getEventGlue(mCache);
        EventRecord* eventrecord = eventmap.get(&event);
        jni::StateGlue source = trans.getSourceGlue(mCache);
        StateRecord* sourcerecord = statemap.get(&source);
        jni::StateGlue target = trans.getTargetGlue(mCache);
        StateRecord* targetrecord = statemap.get(&target);
        if (pass == 1) {
          const bool det = eventrecord->addDeterministicTransition
            (autrecord, sourcerecord, targetrecord);
          if (!det) {
            if (autrecord->isPlant()) {
              maxpass = 2;
            } else {
              jni::NondeterministicDESExceptionGlue
                exception(&aut, &source, &event, mCache);
              throw mCache->throwJavaException(exception);
            }
          }
        } else {
          eventrecord->addNondeterministicTransition
            (autrecord, sourcerecord, targetrecord);
        }
      }
    }
    statemap.clear();
    const jni::SetGlue events = aut.getEventsGlue(mCache);
    const jni::IteratorGlue eventiter = events.iteratorGlue(mCache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, mCache);
      jni::EventKind kind = event.getKindGlue(mCache);
      if (kind == jni::EventKind_UNCONTROLLABLE ||
          kind == jni::EventKind_CONTROLLABLE) {
        EventRecord* eventrecord = eventmap.get(&event);
        eventrecord->normalize(autrecord);
      }
    }
  }

  // Establish compact event list ...
  mNumEventRecords = eventmap.size();
  HashTableIterator hiter1 = eventmap.iterator();
  while (eventmap.hasNext(hiter1)) {
    const EventRecord* eventrecord = eventmap.next(hiter1);
    if (eventrecord->isSkippable()) {
      mNumEventRecords--;
    }
  }
  mEventRecords = new EventRecord*[mNumEventRecords];
  HashTableIterator hiter2 = eventmap.iterator();
  int i = 0;
  while (eventmap.hasNext(hiter2)) {
    EventRecord* eventrecord = eventmap.next(hiter2);
    if (!eventrecord->isSkippable()) {
      eventrecord->sortTransitionRecordsForSearch();
      mEventRecords[i++] = eventrecord;
      if (!eventrecord->isControllable() && eventrecord->isDisabledInSpec()) {
        mIsTrivial = false;
      }
    }
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(EventRecord*),
        EventRecord::compareForForwardSearch);
}


void SafetyVerifier::
teardown()
{
  delete mEncoding;
  mEncoding = 0;
  delete mStateSpace;
  mStateSpace = 0;
  delete mDepthMap;
  mDepthMap = 0;
  for (int i = 0; i < mNumEventRecords; i++) {
    delete mEventRecords[i];
  }
  mNumEventRecords = 0;
  delete [] mEventRecords;
  mEventRecords = 0;
  delete [] mNondeterministicTransitionIterators;
  mNondeterministicTransitionIterators = 0;
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

#define ADD_NEW_STATE (mStateSpace->add() == mNumStates)

#define EXPAND_ENABLED_TRANSITIONS(numwords, tuple, packedtuple, event) \
  {                                                                     \
    uint32* packednext = mStateSpace->prepare();                        \
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
        mNumStates++;                                                   \
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
          mNumStates++;                                                 \
        }                                                               \
      } else {                                                          \
        int ndindex;                                                    \
        do {                                                            \
          if (ADD_NEW_STATE) {                                          \
            packednext = mStateSpace->prepare(mNumStates++);            \
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


bool SafetyVerifier::
checkProperty()
{
  // No interesting specs or no initial state?
  if (mIsTrivial) {
    return true;
  }

  // Store initial states ...
  const int numwords = mEncoding->getNumberOfWords();
  uint32* initpacked = mStateSpace->prepare();
  for (int w = 0; w < numwords; w++) {
    initpacked[w] = 0;
  }
  mNumStates = 0;
  int ndindex;
  do {
    mStateSpace->add();
    initpacked = mStateSpace->prepare(mNumStates++);
    for (ndindex = 0; ndindex < mNumNondetInitialStates; ndindex++) {
      if (!mNondeterministicTransitionIterators[ndindex].
          advance(initpacked)) {
        break;
      }
    }
  } while (ndindex < mNumNondetInitialStates);

  // Prepare depth map ...
  uint32 nextlevel = mNumStates;
  mDepthMap->add(0);
  mDepthMap->add(nextlevel);

  // Main loop ...
  uint32 current = 0;
  uint32* currenttuple = new uint32[mNumAutomata];
  while (current < mNumStates) {
    uint32* currentpacked = mStateSpace->get(current);
    mEncoding->decode(currentpacked, currenttuple);
    for (int e = 0; e < mNumEventRecords; e++) {
      const EventRecord* event = mEventRecords[e];
      const AutomatonRecord* dis = 0;
      FIND_DISABLING_AUTOMATON(currenttuple, event, dis)
      if (dis == 0) {
        EXPAND_ENABLED_TRANSITIONS(numwords, currenttuple,
                                   currentpacked, event);
      } else if (!dis->isPlant() && !event->isControllable()) {
        mTraceState = current;
        mTraceEvent = event;
        delete [] currenttuple;
        return false;
      }
    }
    if (++current == nextlevel) {
      nextlevel = mNumStates;
      mDepthMap->add(nextlevel);
    }
  }
  delete [] currenttuple;
  return true;
}


#undef ADD_NEW_STATE
#define ADD_NEW_STATE checkTraceState()


void SafetyVerifier::
computeCounterExample()
{
  mTraceList = new jni::LinkedListGlue(mCache);
  jni::EventGlue eventglue0 = mTraceEvent->getJavaEvent();
  mTraceList->add(0, &eventglue0);
  uint32 level = mDepthMap->size() - 2;
  if (level > 0) {
    int e;
    int numreversed = 0;
    EventRecord** reversed = new EventRecord*[mNumEventRecords];
    for (e = 0; e < mNumEventRecords; e++) {
      EventRecord* event = mEventRecords[e];
      if (!event->isOnlySelfloops() && event->reverse()) {
        reversed[numreversed++] = event;
      }
    }
    qsort(reversed, numreversed, sizeof(EventRecord*),
          EventRecord::compareForBackwardSearch);
    const int numwords = mEncoding->getNumberOfWords();
    uint32* targettuple = new uint32[mNumAutomata];
    do {
      mTraceLimit = mDepthMap->get(level);
      uint32* packedtarget = mStateSpace->get(mTraceState);
      mEncoding->decode(packedtarget, targettuple);
      e = -1;
      try {
        do {
          const EventRecord* event = reversed[++e];
          const AutomatonRecord* dis = 0;
          FIND_DISABLING_AUTOMATON(targettuple, event, dis);
          if (dis == 0) {
            EXPAND_ENABLED_TRANSITIONS(numwords, targettuple,
                                       packedtarget, event);
          }
        } while (true);
      } catch (const TraceAbort& abort) {
        // OK. That's what we have been waiting for.
      }
      jni::EventGlue eventglue = reversed[e]->getJavaEvent();
      mTraceList->add(0, &eventglue);
    } while (level-- > 1);
    delete [] targettuple;
  }
}

bool SafetyVerifier::
checkTraceState()
{
  uint32 found = mStateSpace->find();
  if (found >= mTraceLimit) {
    return false;
  } else {
    mTraceState = found;
    throw TraceAbort();
  }
}


}  /* namespace waters */



//############################################################################
//# SafetyVerifier: Invocation through JNI

JNIEXPORT jobject JNICALL 
Java_net_sourceforge_waters_cpp_analysis_NativeSafetyVerifier_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  try {
    jni::ClassCache cache(env);
    try {
      jni::NativeSafetyVerifierGlue gchecker(jchecker, &cache);
      jni::ProductDESGlue des = gchecker.getModelGlue(&cache);
      jni::KindTranslatorGlue translator =
        gchecker.getKindTranslatorGlue(&cache);
      waters::SafetyVerifier checker(des, translator, &cache);
      const int limit = gchecker.getNodeLimit();
      if (limit != UNDEF_INT32) {
        checker.setStateLimit(limit);
      }
      bool result = checker.run();
      if (result) {
        jni::VerificationResultGlue vresult(result, 0, &cache);
        checker.addStatistics(vresult);
        return vresult.returnJavaObject();
      } else {
        jni::ProductDESProxyFactoryGlue factory =
          gchecker.getFactoryGlue(&cache);
        jni::SafetyTraceGlue trace = checker.getCounterExample(factory);
        jni::VerificationResultGlue vresult(result, &trace, &cache);
        checker.addStatistics(vresult);
        return vresult.returnJavaObject();
      }
    } catch (const jni::PreJavaException& pre) {
      cache.throwJavaException(pre);
      return 0;
    }
  } catch (jthrowable exception) {
    return 0;
  }
}
