//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   JavaHashTable
//###########################################################################
//# $Id: JavaHashTable.cpp,v 1.3 2006-08-21 05:41:39 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

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

uint32 ObjectHashAccessor::
hash(const void* key)
  const
{
  const jobject object = (const jobject) key;
  const jni::ObjectGlue glue(object, mCache, true);
  const int javahash = glue.hashCode();
  return waters::hashInt(javahash);
}


bool ObjectHashAccessor::
equals(const void* key1, const void* key2)
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

JNIEXPORT jint JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_createNativeHashSet
  (JNIEnv* env, jobject /* nset */, jint initsize)
{
  return (jint) new waters::JavaHashTable(env, initsize);
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_destroyNativeHashSet
  (JNIEnv* env, jobject /* nset */, jint handler)
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
  (JNIEnv* /* env */, jobject /* nset */, jint handler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) handler;
  return table->size();
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_containsNative
  (JNIEnv* /* env */, jobject /* nset */, jint handler, jobject item)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) handler;
  return table->get(item) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_addNative
  (JNIEnv *env, jobject /* nset */, jint handler, jobject item)
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
  (JNIEnv* /* env */, jobject /* nset */, jint handler)
{
  waters::JavaHashTable* table = (waters::JavaHashTable*) handler;
  table->clear();
}

JNIEXPORT jint JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_createNativeIterator
  (JNIEnv* /* env */, jobject /* nset */, jint handler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) handler;
  waters::HashTableIterator iter = table->iterator();
  return (jint) new waters::HashTableIterator(iter);
}

JNIEXPORT void JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_destroyNativeIterator
  (JNIEnv* /* env */, jobject /* nset */, jint handler)
{
  waters::HashTableIterator* iter = (waters::HashTableIterator*) handler;
  delete iter;
}

JNIEXPORT jobject JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_getNativeNext
  (JNIEnv* /* env */, jobject /* nset */, jint thandler, jint ihandler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) thandler;
  waters::HashTableIterator* iter = (waters::HashTableIterator*) ihandler;
  return table->next(*iter);
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_hasNativeNext
  (JNIEnv* /* env */, jobject /* nset */, jint thandler, jint ihandler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) thandler;
  waters::HashTableIterator* iter = (waters::HashTableIterator*) ihandler;
  const bool result = table->hasNext(*iter);
  return result ? JNI_TRUE : JNI_FALSE;
}
