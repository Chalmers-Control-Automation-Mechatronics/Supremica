//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   ObjectBase
//###########################################################################
//# $Id: ObjectBase.cpp,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassCache.h"
#include "jni/cache/ObjectBase.h"
#include "jni/glue/Glue.h"


namespace jni {


//###########################################################################
//# Class ObjectBase
//###########################################################################

//###########################################################################
//# ObjectBase: Constructors & Destructors

ObjectBase::
ObjectBase(waters::uint32 classcode, ClassCache* cache)
{
  mClass = cache->getClass(classcode);
}

ObjectBase::
ObjectBase(jobject javaobject, waters::uint32 classcode, ClassCache* cache)
{
  if ( (mJavaObject = javaobject) ) {
    JNIEnv* env = cache->getEnvironment();
    jclass javaclass = env->GetObjectClass(javaobject);
    mClass = cache->getClass(javaclass, classcode);
  } else {
    cache->throwJavaException
      (CLASS_NullPointerException, "Trying to create NULL object!");
  }
}

ObjectBase::
~ObjectBase()
{
  if (mJavaObject) {
    JNIEnv* env = mClass->getEnvironment();
    env->DeleteLocalRef(mJavaObject);
  }
}


//###########################################################################
//# ObjectBase: Access

jobject ObjectBase::
returnJavaObject()
{
  jobject result = mJavaObject;
  mJavaObject = 0;
  return result;
}


}  /* namespace jni */
