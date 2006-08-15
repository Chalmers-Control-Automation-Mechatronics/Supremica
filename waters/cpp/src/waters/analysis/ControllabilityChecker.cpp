//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.cpp,v 1.2 2006-08-15 03:08:53 robi Exp $
//###########################################################################


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
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# class ControllabilityChecker
//############################################################################

//############################################################################
//# ControllabilityChecker: Constructors & Destructors

ControllabilityChecker::
ControllabilityChecker(const jni::ProductDESGlue& des,
                       jni::ClassCache* cache)
{
  const jni::SetGlue events = des.getEventsGlue(cache);
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  mNumEventRecords = events.size();
  mEventRecords = new EventRecord*[mNumEventRecords];
  int i = 0;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::EventGlue event(javaobject, cache);
    switch (event.getKindGlue(cache)) {
    case jni::EventKind_UNCONTROLLABLE:
    case jni::EventKind_CONTROLLABLE:
      mEventRecords[i++] = new EventRecord(event, cache);
      break;
    default:
      break;
    }
  }
  mNumEventRecords = i;
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
