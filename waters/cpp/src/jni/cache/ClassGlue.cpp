//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   ClassGlue
//###########################################################################
//# $Id: ClassGlue.cpp,v 1.2 2005-11-05 09:47:15 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/ClassInfo.h"
#include "jni/cache/MethodInfo.h"
#include "jni/glue/Glue.h"
#include "waters/base/IntTypes.h"


namespace jni {


//###########################################################################
//# Class ClassGlue
//###########################################################################

//###########################################################################
//# ClassGlue: Constructors & Destructors

ClassGlue::
ClassGlue(const ClassInfo* info, jclass javaclass, JNIEnv* env)
{
  mClassInfo = info;
  mJavaClass = javaclass;
  if (waters::uint32 methodcount = info->getNumMethods()) {
    mMethodTable = new jmethodID[methodcount];
    for (waters::uint32 i = 0; i < methodcount; i++) {
      mMethodTable[i] = 0;
    }
  } else {
    mMethodTable = 0;
  }
  if (waters::uint32 fieldcount = info->getNumFields()) {
    mStaticFinalFieldTable = new jobject[fieldcount];
    for (waters::uint32 i = 0; i < fieldcount; i++) {
      mStaticFinalFieldTable[i] = 0;
    }
  } else {
    mStaticFinalFieldTable = 0;
  }
  mEnvironment = env;
}


ClassGlue::
~ClassGlue()
{
  delete [] mMethodTable;
  if (waters::uint32 fieldcount = mClassInfo->getNumFields()) {
    for (waters::uint32 i = 0; i < fieldcount; i++) {
      mEnvironment->DeleteLocalRef(mStaticFinalFieldTable[i]);
    }
    delete [] mStaticFinalFieldTable;
  }
  mEnvironment->DeleteLocalRef(mJavaClass);
}


//###########################################################################
//# ClassGlue: Access

jmethodID ClassGlue::
getMethodID(waters::uint32 methodcode)
{
  jmethodID result = mMethodTable[methodcode];
  if (result == 0) {
    const MethodInfo* methodinfo = mClassInfo->getMethodInfo(methodcode);
    const char* name = methodinfo->getName();
    const char* signature = methodinfo->getSignature();
    result = mEnvironment->GetMethodID(mJavaClass, name, signature);
    if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
      throw exception;
    }
    mMethodTable[methodcode] = result;
  }
  return result;
}


jmethodID ClassGlue::
getStaticMethodID(waters::uint32 methodcode)
{
  jmethodID result = mMethodTable[methodcode];
  if (result == 0) {
    const MethodInfo* methodinfo = mClassInfo->getMethodInfo(methodcode);
    const char* name = methodinfo->getName();
    const char* signature = methodinfo->getSignature();
    result = mEnvironment->GetStaticMethodID(mJavaClass, name, signature);
    if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
      throw exception;
    }
    mMethodTable[methodcode] = result;
  }
  return result;
}


jobject ClassGlue::
getStaticFinalField(waters::uint32 fieldcode)
{
  jobject result = mStaticFinalFieldTable[fieldcode];
  if (result == 0) {
    const MethodInfo* fieldinfo = mClassInfo->getFieldInfo(fieldcode);
    const char* name = fieldinfo->getName();
    const char* signature = fieldinfo->getSignature();
    jfieldID fid = mEnvironment->GetStaticFieldID(mJavaClass, name, signature);
    if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
      throw exception;
    }
    mStaticFinalFieldTable[fieldcode] = result =
      mEnvironment->GetStaticObjectField(mJavaClass, fid);
  }
  return result;
}


}  /* namespace jni */
