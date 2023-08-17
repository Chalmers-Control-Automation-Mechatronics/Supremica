//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

#include <jni.h>

#include "jni/cache/PreJavaException.h"
#include "jni/glue/ObjectGlue.h"

#include "waters/base/HashAccessor.h"
#include "waters/base/JavaHashTable.h"
#include "waters/javah/Invocations.h"


namespace waters {

//############################################################################
//# class ObjectHashAccessor
//############################################################################

//############################################################################
//# ObjectHashAccessor: Hash Methods

uint64_t ObjectHashAccessor::
hash(intptr_t key)
  const
{
  const jobject object = (const jobject) key;
  const jni::ObjectGlue glue(object, mCache, true);
  const int javahash = glue.hashCode();
  return waters::hashInt(javahash);
}


bool ObjectHashAccessor::
equals(intptr_t key1, intptr_t key2)
  const
{
  const jobject object1 = (const jobject) key1;
  const jni::ObjectGlue glue1(object1, mCache, true);
  const jobject object2 = (const jobject) key2;
  return glue1.equals(object2) != JNI_FALSE;
}



//############################################################################
//# class JavaHashTable
//############################################################################

//############################################################################
//# JavaHashTable : Constructors & Destructors

JavaHashTable::
JavaHashTable(JNIEnv* env, jint initsize)
  : mCache(env),
    mAccessor(&mCache),
    mTable(&mAccessor, initsize)
{
}


}  /* namespace waters */



//############################################################################
//# HashTable: Invocation through JNI

JNIEXPORT jlong JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_createNativeHashSet
  (JNIEnv* env, jobject /* nset */, jint initsize)
{
  return (jlong) new waters::JavaHashTable(env, initsize);
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_destroyNativeHashSet
  (JNIEnv* env, jobject /* nset */, jlong handler)
{
  waters::JavaHashTable* table = (waters::JavaHashTable*) handler;
  waters::HashTableIterator iter = table->iterator();
  while (table->hasNext(iter) && !env->ExceptionOccurred()) {
    jobject victim = table->next(iter);
    env->DeleteGlobalRef(victim);
  }
  delete table;
}

JNIEXPORT jint JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_getNativeSize
  (JNIEnv* /* env */, jobject /* nset */, jlong handler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) handler;
  return table->size();
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_containsNative
  (JNIEnv* /* env */, jobject /* nset */, jlong handler, jobject item)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) handler;
  return table->get(item) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_addNative
  (JNIEnv *env, jobject /* nset */, jlong handler, jobject item)
{
  waters::JavaHashTable* table = (waters::JavaHashTable*) handler;
  jobject gitem = env->NewGlobalRef(item);
  jobject added = table->add(gitem);
  if (gitem == added) {
    return JNI_TRUE;
  } else {
    env->DeleteGlobalRef(gitem);
    return JNI_FALSE;
  }
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_clearNative
  (JNIEnv* /* env */, jobject /* nset */, jlong handler)
{
  waters::JavaHashTable* table = (waters::JavaHashTable*) handler;
  table->clear();
}

JNIEXPORT jlong JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_createNativeIterator
  (JNIEnv* /* env */, jobject /* nset */, jlong handler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) handler;
  waters::HashTableIterator iter = table->iterator();
  return (jlong) new waters::HashTableIterator(iter);
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_destroyNativeIterator
  (JNIEnv* /* env */, jobject /* nset */, jlong handler)
{
  waters::HashTableIterator* iter = (waters::HashTableIterator*) handler;
  delete iter;
}

JNIEXPORT jobject JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_getNativeNext
  (JNIEnv* /* env */, jobject /* nset */, jlong thandler, jlong ihandler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) thandler;
  waters::HashTableIterator* iter = (waters::HashTableIterator*) ihandler;
  return table->next(*iter);
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_hasNativeNext
  (JNIEnv* /* env */, jobject /* nset */, jlong thandler, jlong ihandler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) thandler;
  waters::HashTableIterator* iter = (waters::HashTableIterator*) ihandler;
  const bool result = table->hasNext(*iter);
  return result ? JNI_TRUE : JNI_FALSE;
}
