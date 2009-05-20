//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectReference
//###########################################################################
//# $Id$
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
ObjectReference(waters::uint32 classcode, ClassCache* cache)
{
  mClass = cache->getClass(classcode);
}

ObjectReference::
ObjectReference(jobject javaobject,
                waters::uint32 classcode,
                ClassCache* cache,
                bool global)
{
  if ( (mJavaObject = javaobject) ) {
    JNIEnv* env = cache->getEnvironment();
    jclass javaclass = env->GetObjectClass(javaobject);
    mClass = cache->getClass(javaclass, classcode);
    mRefCount = global ? UNDEF_UINT32 : 1;
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

waters::uint32 ObjectReference::
addReference()
{
  if (mRefCount < UNDEF_UINT32) {
    mRefCount++;
  }
  return mRefCount;
}

waters::uint32 ObjectReference::
removeReference()
{
  if (mRefCount < UNDEF_UINT32) {
    mRefCount--;
  }
  return mRefCount;
}


}  /* namespace jni */
