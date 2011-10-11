//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectBase
//###########################################################################
//# $Id: ObjectBase.cpp 4707 2009-05-20 22:45:16Z robi $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ObjectBase.h"
#include "jni/cache/ObjectReference.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/Glue.h"


namespace jni {


//###########################################################################
//# Class ObjectBase
//###########################################################################

//###########################################################################
//# ObjectBase: Constructors, Destructors & Co.

ObjectBase::
ObjectBase(uint32_t classcode, ClassCache* cache)
{
  mObjectReference = new ObjectReference(classcode, cache);
}

ObjectBase::
ObjectBase(jobject javaobject,
           uint32_t classcode,
           ClassCache* cache,
           bool global)
{
  if (javaobject == 0) {
    mObjectReference = 0;
  } else {
    mObjectReference =
      new ObjectReference(javaobject, classcode, cache, global);
  }
}

ObjectBase::
ObjectBase(const ObjectBase& partner)
{
  if ( (mObjectReference = partner.mObjectReference) ) {
    mObjectReference->addReference();
  }
}

ObjectBase::
~ObjectBase()
{
  if (mObjectReference && mObjectReference->removeReference() == 0) {
    delete mObjectReference;
  }
}

ObjectBase& ObjectBase::
operator = (const ObjectBase& partner)
{
  if (this != &partner) {
    if (mObjectReference && mObjectReference->removeReference() == 0) {
      delete mObjectReference;
    }
    if ( (mObjectReference = partner.mObjectReference) ) {
      mObjectReference->addReference();
    }
  }
  return *this;
}


//###########################################################################
//# ObjectBase: Access

bool ObjectBase::
isSameObject(const ObjectBase& other, const ClassCache* cache)
  const
{
  if (mObjectReference == 0) {
    return other.mObjectReference == 0;
  } else {
    return cache->isSameObject(getJavaObject(), other.getJavaObject());
  }
}

jobject ObjectBase::
getJavaObject()
  const
{
  return mObjectReference ? mObjectReference->getJavaObject() : 0;
}

jobject ObjectBase::
returnJavaObject()
  const
{
  return mObjectReference ? mObjectReference->returnJavaObject() : 0;
}

ClassGlue* ObjectBase::
getClass()
  const
{
  return mObjectReference ? mObjectReference->getClass() : 0;
}

void ObjectBase::
initJavaObject(jobject javaobject)
{
  mObjectReference->initJavaObject(javaobject);
}
  

//############################################################################
//# Error Handling

void ObjectBase::
checkNonNull()
  const
{
  if (mObjectReference == 0) {
    throw PreJavaException(CLASS_NullPointerException,
                           "Trying to access NULL object!",
                           true);
  }
}


}  /* namespace jni */
