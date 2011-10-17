//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   ClassCache
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassInfo.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/ExceptionGlue.h"
#include "jni/glue/Glue.h"


namespace jni {


//###########################################################################
//# ClassCacheHashAccessor: Constructors & Destructors

ClassCacheHashAccessor::
ClassCacheHashAccessor(JNIEnv* env)
{
  const ClassInfo* info = &CLASSINFO[CLASS_Class];
  const char* name = info->getName();
  jclass javaclass = env->FindClass(name);
  jthrowable exception = env->ExceptionOccurred();
  if (exception) {
    throw exception;
  }
  mEnvironment = env;
  mClassGlue = new ClassGlue(info, javaclass, env);
}


//############################################################################
//# ClassCacheHashAccessor: Hash Methods

uint64_t ClassCacheHashAccessor::
hash(intptr_t key)
  const
{
  jmethodID mid = mClassGlue->getMethodID(METHOD_Object_hashCode);
  const ClassKey* ckey = (const ClassKey*) key;
  jobject javaobject = ckey->getJavaClass();
  uint64_t lkey = mEnvironment->CallIntMethod(javaobject, mid);
  if (jthrowable exception = mEnvironment->ExceptionOccurred()) {
    throw exception;
  }
  const ClassInfo* info = ckey->getClassInfo();
  lkey = (lkey << 32) || info->getClassCode();
  return waters::hashInt(lkey);
}


bool ClassCacheHashAccessor::
equals(intptr_t val1, intptr_t val2)
  const
{
  const ClassKey* ckey1 = (const ClassKey*) val1;
  const ClassKey* ckey2 = (const ClassKey*) val2;
  if (ckey1->getClassInfo() != ckey2->getClassInfo()) {
    return false;
  }
  jobject javaobject1 = ckey1->getJavaClass();
  jobject javaobject2 = ckey2->getJavaClass();
  return mEnvironment->IsSameObject(javaobject1, javaobject2) == JNI_TRUE;
}



//###########################################################################
//# Class ClassCache
//###########################################################################

//###########################################################################
//# ClassCache: Constructors & Destructors
  
ClassCache::
ClassCache(JNIEnv* env)
  : mEnvironment(env),
    mAccessor(env),
    mClassMap(&mAccessor)
{
  mCodeMap = new ClassGlue* [CLASS_COUNT];
  for (uint32_t i = 0; i < CLASS_COUNT; i++) {
    mCodeMap[i] = 0;
  }
  ClassGlue* classglue1 = mAccessor.getClassGlue();
  mCodeMap[CLASS_Class] = classglue1;
  mClassMap.add(classglue1);
}


ClassCache::
~ClassCache()
{
  waters::HashTableIterator iter = mClassMap.iterator();
  while (mClassMap.hasNext(iter)) {
    ClassGlue* victim = mClassMap.next(iter);
    delete victim;
  }
  delete [] mCodeMap;
}


//############################################################################
//# ClassCache: Access

ClassGlue* ClassCache::
getClass(uint32_t classcode)
{
  ClassGlue* result = mCodeMap[classcode];
  if (result == 0) {
    const ClassInfo* info = &CLASSINFO[classcode];
    const char* name = info->getName();
    jclass javaclass = mEnvironment->FindClass(name);
    jthrowable exception = mEnvironment->ExceptionOccurred();
    if (exception) {
      throw exception;
    }
    mCodeMap[classcode] = result = getClass(javaclass, info);
  }
  return result;
}

ClassGlue* ClassCache::
getClass(jclass javaclass, uint32_t classcode)
{
  const ClassInfo* info = &CLASSINFO[classcode];
  return getClass(javaclass, info);
}

ClassGlue* ClassCache::
getClass(jclass javaclass, const ClassInfo* info)
{
  ClassKey key(info, javaclass);
  ClassGlue* result = mClassMap.get(&key);
  if (result) {
    mEnvironment->DeleteLocalRef(javaclass);
  } else {
    result = new ClassGlue(info, javaclass, mEnvironment);
    mClassMap.add(result);
  }
  return result;
}

bool ClassCache::
isSameObject(jobject obj1, jobject obj2)
  const
{
  return mEnvironment->IsSameObject(obj1, obj2) == JNI_TRUE;
}


//############################################################################
//# ClassCache: Exceptions

jint ClassCache::
throwJavaException(uint32_t classcode, const char* msg)
{
  const ClassGlue* cls = getClass(classcode);
  const jclass javaclass = cls->getJavaClass();
  return mEnvironment->ThrowNew(javaclass, msg);
}

jint ClassCache::
throwJavaException(const ExceptionGlue& glue)
{
  jthrowable exception = (jthrowable) glue.returnJavaObject();
  return mEnvironment->Throw(exception);
}


}  /* namespace jni */
