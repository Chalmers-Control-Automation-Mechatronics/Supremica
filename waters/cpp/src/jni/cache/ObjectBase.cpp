//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectBase
//###########################################################################
//# $Id: ObjectBase.cpp,v 1.4 2005-11-07 23:45:47 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

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
ObjectBase(waters::uint32 classcode, ClassCache* cache)
{
  mObjectReference = new ObjectReference(classcode, cache);
}

ObjectBase::
ObjectBase(jobject javaobject, waters::uint32 classcode, ClassCache* cache)
{
  if (javaobject == 0) {
    mObjectReference = 0;
  } else {
    mObjectReference = new ObjectReference(javaobject, classcode, cache);
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
