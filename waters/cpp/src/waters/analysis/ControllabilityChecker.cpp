//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.cpp,v 1.1 2006-08-15 01:43:06 robi Exp $
//###########################################################################


#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/NativeControllabilityCheckerGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/VerificationResultGlue.h"

#include "waters/base/IntTypes.h"
#include "waters/javah/Invocations.h"


namespace waters {
}


JNIEXPORT jobject JNICALL 
Java_net_sourceforge_waters_cpp_analysis_NativeControllabilityChecker_runNativeAlgorithm
  (JNIEnv* env, jobject jchecker)
{
  try {
    jni::ClassCache cache(env);
    try {
      jni::NativeControllabilityCheckerGlue checker(jchecker, &cache);
      jni::ProductDESGlue des = checker.getInputGlue(&cache);
      jstring name = des.getName();
      jni::ProductDESProxyFactoryGlue factory = checker.getFactoryGlue(&cache);
      jni::VerificationResultGlue result(true, 0, &cache);
      return result.returnJavaObject();
    } catch (const jni::PreJavaException& pre) {
      cache.throwJavaException(pre);
    }
  } catch (jthrowable exception) {
    return 0;
  }
}
