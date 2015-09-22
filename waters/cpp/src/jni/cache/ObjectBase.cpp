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








