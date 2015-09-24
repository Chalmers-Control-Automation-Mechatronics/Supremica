//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <stdint.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/ClassInfo.h"
#include "jni/cache/MethodInfo.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/Glue.h"


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
