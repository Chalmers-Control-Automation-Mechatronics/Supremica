//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.cpp,v 1.4 2006-08-17 05:02:25 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/NativeControllabilityCheckerGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/analysis/ControllabilityChecker.h"
#include "waters/analysis/EventRecord.h"
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
  const jni::SetGlue events = des.getEventsGlue(cache);
  const int numevents = events.size();
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  EventRecordHashAccessor accessor;
  HashTable<jni::EventGlue*,EventRecord*> eventmap(&accessor, numevents);
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
 
  mNumEventRecords = eventmap.size();
  if (mNumEventRecords > 0) {
    mEventRecords = new EventRecord*[mNumEventRecords];
    HashTableIterator hiter = eventmap.iterator();
    int i = 0;
    while (eventmap.hasNext(hiter)) {
      mEventRecords[i++] = eventmap.next(hiter);
    }
  } else {
    mEventRecords = 0;
  }
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
    }
  } catch (jthrowable exception) {
    return 0;
  }
}
