//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
