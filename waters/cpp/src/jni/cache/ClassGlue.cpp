//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   ClassGlue
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/ClassInfo.h"
#include "jni/cache/MethodInfo.h"
#include "jni/glue/Glue.h"
#include <stdint.h>


namespace jni {


//###########################################################################
//# Class ClassGlue
//###########################################################################

//###########################################################################
//# ClassGlue: Constructors & Destructors

ClassGlue::
ClassGlue(const ClassInfo* info, jclass javaclass, JNIEnv* env)
  : mKey(info, (jclass) env->NewGlobalRef(javaclass))
{
  if (uint32_t methodcount = info->getNumMethods()) {
    mMethodTable = new jmethodID[methodcount];
    for (uint32_t i = 0; i < methodcount; i++) {
      mMethodTable[i] = 0;
    }
  } else {
    mMethodTable = 0;
  }
  if (uint32_t fieldcount = info->getNumFields()) {
    mStaticFinalFieldTable = new jobject[fieldcount];
    for (uint32_t i = 0; i < fieldcount; i++) {
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
  const ClassInfo* info = mKey.getClassInfo();
  if (uint32_t fieldcount = info->getNumFields()) {
    for (uint32_t i = 0; i < fieldcount; i++) {
      mEnvironment->DeleteLocalRef(mStaticFinalFieldTable[i]);
    }
    delete [] mStaticFinalFieldTable;
  }
  jclass javaclass = mKey.getJavaClass();
  mEnvironment->DeleteGlobalRef(javaclass);
}


//###########################################################################
//# ClassGlue: Access

jmethodID ClassGlue::
getMethodID(uint32_t methodcode)
{
  jmethodID result = mMethodTable[methodcode];
  if (result == 0) {
    const ClassInfo* info = mKey.getClassInfo();
    const MethodInfo* methodinfo = info->getMethodInfo(methodcode);
    const char* name = methodinfo->getName();
    const char* signature = methodinfo->getSignature();
    jclass javaclass = mKey.getJavaClass();
    result = mEnvironment->GetMethodID(javaclass, name, signature);
    if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
      throw exception;
    }
    mMethodTable[methodcode] = result;
  }
  return result;
}


jmethodID ClassGlue::
getStaticMethodID(uint32_t methodcode)
{
  jmethodID result = mMethodTable[methodcode];
  if (result == 0) {
    const ClassInfo* info = mKey.getClassInfo();
    const MethodInfo* methodinfo = info->getMethodInfo(methodcode);
    const char* name = methodinfo->getName();
    const char* signature = methodinfo->getSignature();
    jclass javaclass = mKey.getJavaClass();
    result = mEnvironment->GetStaticMethodID(javaclass, name, signature);
    if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
      throw exception;
    }
    mMethodTable[methodcode] = result;
  }
  return result;
}


jobject ClassGlue::
getStaticFinalField(uint32_t fieldcode)
{
  jobject result = mStaticFinalFieldTable[fieldcode];
  if (result == 0) {
    const ClassInfo* info = mKey.getClassInfo();
    const MethodInfo* fieldinfo = info->getFieldInfo(fieldcode);
    const char* name = fieldinfo->getName();
    const char* signature = fieldinfo->getSignature();
    jclass javaclass = mKey.getJavaClass();
    jfieldID fid = mEnvironment->GetStaticFieldID(javaclass, name, signature);
    if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
      throw exception;
    }
    mStaticFinalFieldTable[fieldcode] = result =
      mEnvironment->GetStaticObjectField(javaclass, fid);
  }
  return result;
}


}  /* namespace jni */
