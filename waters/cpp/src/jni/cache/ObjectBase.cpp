//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectBase
//###########################################################################
//# $Id: ObjectBase.cpp,v 1.2 2005-11-06 09:01:52 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassCache.h"
#include "jni/cache/ObjectBase.h"
#include "jni/cache/ObjectReference.h"


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
  mObjectReference = new ObjectReference(javaobject, classcode, cache);
}

ObjectBase::
ObjectBase(const ObjectBase& partner)
{
  mObjectReference = partner.mObjectReference;
  mObjectReference->addReference();
}

ObjectBase::
~ObjectBase()
{
  if (mObjectReference->removeReference() == 0) {
    delete mObjectReference;
  }
}

ObjectBase& ObjectBase::
operator = (const ObjectBase& partner)
{
  if (this != &partner) {
    if (mObjectReference->removeReference() == 0) {
      delete mObjectReference;
    }
    mObjectReference = partner.mObjectReference;
    mObjectReference->addReference();
  }
  return *this;
}


//###########################################################################
//# ObjectBase: Access

jobject ObjectBase::
getJavaObject()
  const
{
  return mObjectReference->getJavaObject();
}

jobject ObjectBase::
returnJavaObject()
  const
{
  return mObjectReference->returnJavaObject();
}

ClassGlue* ObjectBase::
getClass()
  const
{
  return mObjectReference->getClass();
}

void ObjectBase::
initJavaObject(jobject javaobject)
{
  mObjectReference->initJavaObject(javaobject);
}


}  /* namespace jni */
