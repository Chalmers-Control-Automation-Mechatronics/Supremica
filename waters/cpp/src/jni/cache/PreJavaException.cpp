//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   PreJavaException
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <string.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreJavaException.h"


namespace jni {


//###########################################################################
//# Class PreJavaException
//###########################################################################

//###########################################################################
//# PreJavaException: Constructors, Destructors & Co.

PreJavaException::
PreJavaException()
  : mClassCode(UINT32_MAX)
{
  initMessage(0,true);
}

PreJavaException::
PreJavaException(uint32_t classcode)
  : mClassCode(classcode)
{
  initMessage(0,true);
}

PreJavaException::
PreJavaException(uint32_t classcode,
                 const char* msg,
                 bool staticString)
  : mClassCode(classcode)
{
  initMessage(msg, staticString);
}

PreJavaException::
PreJavaException(const PreJavaException& partner)
  : mClassCode(partner.mClassCode)
{
  initMessage(partner.mMessage, partner.mStaticString);
}

PreJavaException::
~PreJavaException()
{
  if (!mStaticString) {
    delete [] mMessage;
  }
}

PreJavaException& PreJavaException::
operator=(const PreJavaException& partner)
{
  if (this != &partner) {
    if (!mStaticString) {
      delete [] mMessage;
    }
    mClassCode = partner.mClassCode;
    initMessage(partner.mMessage, partner.mStaticString);
  }
  return *this;
}


//###########################################################################
//# PreJavaException: Throwing Exceptions

jint PreJavaException::
throwJavaException(ClassCache& cache)
  const
{
  if (mClassCode < UINT32_MAX) {
    std::cerr << "howdy1" << std::endl;
    return cache.throwJavaException(mClassCode, mMessage);
  } else {
    std::cerr << "howdy2" << std::endl;
    JNIEnv* env = cache.getEnvironment();
    jthrowable exception = env->ExceptionOccurred();
    std::cerr << exception << std::endl;
    return 0;
  }
}


//###########################################################################
//# PreJavaException: Auxiliary Methods

void PreJavaException::
initMessage(const char* msg, bool staticString)
{
  if ( (mStaticString = staticString) ) {
    mMessage = (char*) msg;
  } else {
    int len = strlen(msg);
    mMessage = new char[len + 1];
    strcpy(mMessage, msg);
  }
}


}  /* namespace jni */
