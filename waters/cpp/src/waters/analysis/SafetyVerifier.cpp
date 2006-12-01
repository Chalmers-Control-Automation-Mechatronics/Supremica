//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id: SafetyVerifier.cpp,v 1.8 2006-12-01 02:06:30 robi Exp $
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
    mCurrentTuple(0),
    mBadState(UNDEF_UINT32),
    mBadEvent(0),
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
  delete [] mCurrentTuple;
  delete mTraceList;
}


//############################################################################
//# SafetyVerifier: Invocation

bool SafetyVerifier::
run()
{
  try {
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
  vresult.setNumberOfStates(mNumStates);
}


//############################################################################
//# SafetyVerifier: Auxiliary Methods

void SafetyVerifier::
setup()
{
  // Establish automaton encoding ...
  mEncoding = new AutomatonEncoding(mModel, mKindTranslator, mCache);
  mIsTrivial = true;
  if (!mEncoding->hasSpecs()) {
    return;
  }
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

  // Prepare initial state ...
  const int numaut = mEncoding->getNumberOfRecords();
  mCurrentTuple = new uint32[numaut];
  int a;
  for (a = 0; a < numaut; a++) {
    mCurrentTuple[a] = UNDEF_UINT32;
  }    

  // Collect transitions ...
  const HashAccessor* stateaccessor = StateRecord::getHashAccessor();
  HashTable<jni::StateGlue*,StateRecord*> statemap(stateaccessor, 256);
  for (a = 0; a < numaut; a++) {
    const AutomatonRecord* autrecord = mEncoding->getRecord(a);
    const jni::AutomatonGlue aut = autrecord->getJavaAutomaton();
    const jni::SetGlue states = aut.getStatesGlue(mCache);
    const jni::IteratorGlue stateiter = states.iteratorGlue(mCache);
    uint32 code = 0;
    while (stateiter.hasNext()) {
      jobject javaobject = stateiter.next();
      jni::StateGlue state(javaobject, mCache);
      StateRecord* staterecord = new StateRecord(state, code, mCache);
      statemap.add(staterecord);
      if (state.isInitial()) {
        if (mCurrentTuple[a] == UNDEF_UINT32) {
          mCurrentTuple[a] = code;
        } else {
          jni::NondeterministicDESExceptionGlue
            exception(&aut, &state, mCache);
          throw mCache->throwJavaException(exception);
        }
      }
      code++;
    }
    const jni::CollectionGlue transitions = aut.getTransitionsGlue(mCache);
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
      bool det =
        eventrecord->addTransition(autrecord, sourcerecord, targetrecord);
      if (!det) {
        jni::NondeterministicDESExceptionGlue
          exception(&aut, &source, &event, mCache);
        throw mCache->throwJavaException(exception);
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
        EventRecord::compare);
}


bool SafetyVerifier::
checkProperty()
{
  // No interesting specs?
  if (mIsTrivial) {
    return true;
  }

  // Store initial state ...
  const int numwords = mEncoding->getNumberOfWords();
  const int numaut = mEncoding->getNumberOfRecords();
  for (int a = 0; a < numaut; a++) {
    if (mCurrentTuple[a] == UNDEF_UINT32) {
      return true;
    }
  }
  uint32* packedcurrent = mStateSpace->prepare();
  mEncoding->encode(mCurrentTuple, packedcurrent);
  mStateSpace->add();
  mNumStates = mStateSpace->size();

  uint32 nextlevel = mNumStates;
  mDepthMap->add(0);
  mDepthMap->add(nextlevel);

  // Main loop ...
  uint32 current = 0;
  uint32* packednext;
  while (current < mNumStates) {
    packedcurrent = mStateSpace->get(current);
    mEncoding->decode(packedcurrent, mCurrentTuple);
    for (int e = 0; e < mNumEventRecords; e++) {
      EventRecord* event = mEventRecords[e];
      for (TransitionRecord* trans = event->getTransitionRecord();
           trans != 0;
           trans = trans->getNextInSearch()) {
        const AutomatonRecord* aut = trans->getAutomaton();
        const int a = aut->getAutomatonIndex();
        const uint32 source = mCurrentTuple[a];
        const uint32 target = trans->getShiftedSuccessor(source);
        if (target == UNDEF_UINT32) {
          if (aut->isPlant() || event->isControllable()) {
            goto nextevent;
          } else {
            mBadState = current;
            mBadEvent = event;
            return false;
          }
        }
      }
      packednext = mStateSpace->prepare();
      for (int w = 0; w < numwords; w++) {
        TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w);
        if (update == 0) {
          packednext[w] = packedcurrent[w];
        } else {
          uint32 word = packedcurrent[w] &
            update->getKeptMask() | update->getCommonTargets();
          for (TransitionRecord* trans = update->getTransitionRecords();
               trans != 0;
               trans = trans->getNextInUpdate()) {
            const AutomatonRecord* aut = trans->getAutomaton();
            const int a = aut->getAutomatonIndex();
            const uint32 source = mCurrentTuple[a];
            word |= trans->getShiftedSuccessor(source);
          }
          packednext[w] = word;
        }
      }
      if (mStateSpace->add() == mNumStates) {
        mNumStates++;
      }
    nextevent:
      ;
    }
    if (++current == nextlevel) {
      nextlevel = mNumStates;
      mDepthMap->add(nextlevel);
    }
  }

  return true;
}


void SafetyVerifier::
computeCounterExample()
{
  mTraceList = new jni::LinkedListGlue(mCache);
  jni::EventGlue eventglue = mBadEvent->getJavaEvent();
  mTraceList->add(0, &eventglue);

  uint32 level = mDepthMap->size() - 2;
  if (level > 0) {
    for (int e = 0; e < mNumEventRecords; e++) {
      EventRecord* event = mEventRecords[e];
      event->sortTransitionRecordsForTrace();
    }
    const int numaut = mEncoding->getNumberOfRecords();
    const int numwords = mEncoding->getNumberOfWords();
    uint32* packedtarget = mStateSpace->get(mBadState);
    uint32* targettuple = new uint32[numaut];
    mEncoding->decode(packedtarget, targettuple);
    
    do {
      uint32 current = mDepthMap->get(--level);
      uint32* packedcurrent;
      const EventRecord* event;
      mEncoding->shift(targettuple);
      do {
        packedcurrent = mStateSpace->get(current++);
        bool decoded = false;
        for (int e = 0; e < mNumEventRecords; e++) {
          event = mEventRecords[e];
          for (int w = 0; w < numwords; w++) {
            TransitionUpdateRecord* update =
              event->getTransitionUpdateRecord(w);
            if (update == 0) {
              if (packedcurrent[w] != packedtarget[w]) {
                goto notfound;
              }
            } else {
              const uint32 kept = update->getKeptMask();
              if ((packedcurrent[w] & kept) != (packedtarget[w] & kept)) {
                goto notfound;
              } else if ((packedtarget[w] & update->getCommonMask()) !=
                         update->getCommonTargets()) {
                goto notfound;
              }
            }
          }
          if (!decoded) {
            mEncoding->decode(packedcurrent, mCurrentTuple);
            decoded = true;
          }
          for (TransitionRecord* trans = event->getTransitionRecord();
               trans != 0;
               trans = trans->getNextInSearch()) {
            const AutomatonRecord* aut = trans->getAutomaton();
            const int a = aut->getAutomatonIndex();
            const uint32 state = mCurrentTuple[a];
            const uint32 succ = trans->getShiftedSuccessor(state);
            if (succ != targettuple[a]) {
              goto notfound;
            }
          }
          goto found;
        notfound:
          ;
        }
      } while (true);
    found:
      jni::EventGlue eventglue = event->getJavaEvent();
      mTraceList->add(0, &eventglue);
      packedtarget = packedcurrent;
      uint32* temp = mCurrentTuple;
      mCurrentTuple = targettuple;
      targettuple = temp;
    } while (level > 0);

    delete [] targettuple;
  }
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
  delete [] mCurrentTuple;
  mCurrentTuple = 0;
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
      const int limit = gchecker.getStateLimit();
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
