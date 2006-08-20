//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   JavaHashTable
//###########################################################################
//# $Id: JavaHashTable.cpp,v 1.1 2006-08-20 08:39:41 robi Exp $
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
//# ObjectHashAccessor: Class Variables

const ObjectHashAccessor ObjectHashAccessor::theInstance;


//############################################################################
//# ObjectHashAccessor: Hash Methods

uint32 ObjectHashAccessor::
hash(const void* key)
  const
{
  const jni::ObjectGlue* object = (const jni::ObjectGlue*) key;
  const int javahash = object->hashCode();
  return waters::hashInt(javahash);
}


bool ObjectHashAccessor::
equals(const void* key1, const void* key2)
  const
{
  const jni::ObjectGlue* object1 = (const jni::ObjectGlue*) key1;
  const jni::ObjectGlue* object2 = (const jni::ObjectGlue*) key2;
  return object1->equals(object2);
}



//############################################################################
//# class JavaHashTable
//############################################################################

//############################################################################
//# JavaHashTable : Constructors & Destructors

JavaHashTable::
JavaHashTable(JNIEnv* env, jint initsize)
  : mCache(env),
    mTable(waters::ObjectHashAccessor::getInstance(), initsize)
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
    jni::ObjectGlue* next = table->next(iter);
    jobject victim = next->getJavaObject();
    env->DeleteGlobalRef(victim);
    delete next;
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
  jni::ClassCache* cache = table->getCache();
  try {
    jni::ObjectGlue glue(item, cache);
    return table->get(&glue) ? JNI_TRUE : JNI_FALSE;
  } catch (const jni::PreJavaException& pre) {
    cache->throwJavaException(pre);
    return JNI_FALSE;
  }
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_addNative
  (JNIEnv *env, jobject /* nset */, jint handler, jobject item)
{
  waters::JavaHashTable* table = (waters::JavaHashTable*) handler;
  jni::ClassCache* cache = table->getCache();
  try {
    jobject gitem = env->NewGlobalRef(item);
    jni::ObjectGlue* gglue = new jni::ObjectGlue(gitem, cache);
    jni::ObjectGlue* added = table->add(gglue);
    if (gglue == added) {
      return JNI_TRUE;
    } else {
      env->DeleteGlobalRef(gitem);
      delete gglue;
      return JNI_FALSE;
    }
  } catch (const jni::PreJavaException& pre) {
    cache->throwJavaException(pre);
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
  jni::ObjectGlue* next = table->next(*iter);
  return next->getJavaObject();
}

JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_NativeHashSet_hasNativeNext
  (JNIEnv* /* env */, jobject /* nset */, jint thandler, jint ihandler)
{
  const waters::JavaHashTable* table = (const waters::JavaHashTable*) thandler;
  waters::HashTableIterator* iter = (waters::HashTableIterator*) ihandler;
  bool result = table->hasNext(*iter);
  return result ? JNI_TRUE : JNI_FALSE;
}
