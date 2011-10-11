//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectReference
//###########################################################################
//# $Id: ObjectReference.cpp 4707 2009-05-20 22:45:16Z robi $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassCache.h"
#include "jni/cache/ObjectReference.h"
#include "jni/glue/Glue.h"


namespace jni {

//###########################################################################
//# Class ObjectReference
//###########################################################################

//###########################################################################
//# ObjectReference: Constructors & Destructors

ObjectReference::
ObjectReference(uint32_t classcode, ClassCache* cache)
{
  mClass = cache->getClass(classcode);
}

ObjectReference::
ObjectReference(jobject javaobject,
                uint32_t classcode,
                ClassCache* cache,
                bool global)
{
  if ( (mJavaObject = javaobject) ) {
    JNIEnv* env = cache->getEnvironment();
    jclass javaclass = env->GetObjectClass(javaobject);
    mClass = cache->getClass(javaclass, classcode);
    mRefCount = global ? UINT32_MAX : 1;
  } else {
    cache->throwJavaException
      (CLASS_NullPointerException, "Trying to create NULL object!");
  }
}

ObjectReference::
~ObjectReference()
{
  JNIEnv* env = mClass->getEnvironment();
  env->DeleteLocalRef(mJavaObject);
}


//###########################################################################
//# ObjectReference: Access

jobject ObjectReference::
returnJavaObject()
{
  addReference();
  return getJavaObject();
}

void ObjectReference::
initJavaObject(jobject javaobject)
{
  mJavaObject = javaobject;
  mRefCount = 1;
}

uint32_t ObjectReference::
addReference()
{
  if (mRefCount < UINT32_MAX) {
    mRefCount++;
  }
  return mRefCount;
}

uint32_t ObjectReference::
removeReference()
{
  if (mRefCount < UINT32_MAX) {
    mRefCount--;
  }
  return mRefCount;
}


}  /* namespace jni */
