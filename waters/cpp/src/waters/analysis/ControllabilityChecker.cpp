//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.cpp,v 1.8 2006-08-21 05:41:39 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>
#include <stdlib.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/CollectionGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/EventKindGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/NativeControllabilityCheckerGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/ControllabilityChecker.h"
#include "waters/analysis/EventRecord.h"
#include "waters/analysis/StateRecord.h"
#include "waters/base/HashTable.h"
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# class ControllabilityChecker
//############################################################################

//############################################################################
//# ControllabilityChecker: Constructors & Destructors

ControllabilityChecker::
ControllabilityChecker(const jni::ProductDESGlue des,
                       jni::ClassCache* cache)
  : mEncoding(des, cache)
{
  // Establish initial event map ...
  const jni::SetGlue events = des.getEventsGlue(cache);
  const int numevents = events.size();
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  const HashAccessor* eventaccessor = EventRecord::getHashAccessor();
  HashTable<jni::EventGlue*,EventRecord*> eventmap(eventaccessor, numevents);
  mEventRecords = new EventRecord*[numevents];
  mNumEventRecords = 0;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::EventGlue event(javaobject, cache);
    bool controllable;
    switch (event.getKindGlue(cache)) {
    case jni::EventKind_UNCONTROLLABLE:
      controllable = false;
      break;
    case jni::EventKind_CONTROLLABLE:
      controllable = true;
      break;
    default:
      continue;
    }
    EventRecord* record = new EventRecord(event, controllable, cache);
    eventmap.add(record);
  }

  //  Collect transitions ...
  const HashAccessor* stateaccessor = StateRecord::getHashAccessor();
  HashTable<jni::StateGlue*,StateRecord*> statemap(stateaccessor, 256);
  const int numaut = mEncoding.getNumRecords();
  for (int a = 0; a < numaut; a++) {
    const AutomatonRecord* autrecord = mEncoding.getRecord(a);
    const jni::AutomatonGlue aut = autrecord->getJavaAutomaton();
    const jni::SetGlue states = aut.getStatesGlue(cache);
    const jni::IteratorGlue stateiter = states.iteratorGlue(cache);
    uint32 code = 0;
    while (stateiter.hasNext()) {
      jobject javaobject = stateiter.next();
      jni::StateGlue state(javaobject, cache);
      StateRecord* staterecord = new StateRecord(state, code++, cache);
      statemap.add(staterecord);
    }
    const jni::CollectionGlue transitions = aut.getTransitionsGlue(cache);
    const jni::IteratorGlue transiter = transitions.iteratorGlue(cache);
    while (transiter.hasNext()) {
      jobject javaobject = transiter.next();
      jni::TransitionGlue trans(javaobject, cache);
      jni::EventGlue event = trans.getEventGlue(cache);
      EventRecord* eventrecord = eventmap.get(&event);
      jni::StateGlue source = trans.getSourceGlue(cache);
      StateRecord* sourcerecord = statemap.get(&source);
      jni::StateGlue target = trans.getTargetGlue(cache);
      StateRecord* targetrecord = statemap.get(&target);
      eventrecord->addTransition(autrecord, sourcerecord, targetrecord);
    }
    statemap.clear();
    const jni::SetGlue events = aut.getEventsGlue(cache);
    const jni::IteratorGlue eventiter = events.iteratorGlue(cache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      jni::EventKind kind = event.getKindGlue(cache);
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
      eventrecord->sortTransitionRecords();
      mEventRecords[i++] = eventrecord;
    }
  }
  qsort(mEventRecords, mNumEventRecords, sizeof(EventRecord*),
        EventRecord::compare);
}


ControllabilityChecker::
~ControllabilityChecker()
{
  for (int i = 0; i < mNumEventRecords; i++) {
    delete mEventRecords[i];
  }
  delete [] mEventRecords;
}


}  /* namespace waters */



//############################################################################
//# ControllabilityChecker: Invocation through JNI

JNIEXPORT jobject JNICALL 
Java_net_sourceforge_waters_cpp_analysis_NativeControllabilityChecker_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  try {
    jni::ClassCache cache(env);
    try {
      jni::NativeControllabilityCheckerGlue gchecker(jchecker, &cache);
      jni::ProductDESGlue des = gchecker.getInputGlue(&cache);
      waters::ControllabilityChecker checker(des, &cache);
      jni::ProductDESProxyFactoryGlue factory =
        gchecker.getFactoryGlue(&cache);
      jni::VerificationResultGlue result(true, 0, &cache);
      return result.returnJavaObject();
    } catch (const jni::PreJavaException& pre) {
      cache.throwJavaException(pre);
      return 0;
    }
  } catch (jthrowable exception) {
    return 0;
  }
}
