//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   JavaHashTable
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _JavaHashTable_h_
#define _JavaHashTable_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/cache/ClassCache.h"

#include "waters/base/HashTable.h"


namespace waters {


//###########################################################################
//# Class ObjectHashAccessor
//###########################################################################

class ObjectHashAccessor : public PtrHashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ObjectHashAccessor(jni::ClassCache* cache) : mCache(cache) {};

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(intptr_t key) const;
  virtual bool equals(intptr_t key1, intptr_t key2) const;

private:
  //##########################################################################
  //# Data Members
  jni::ClassCache* mCache;
};


//###########################################################################
//# Class JavaHashTable
//###########################################################################

class JavaHashTable
{
public:
  //##########################################################################
  //# Construction
  JavaHashTable(JNIEnv* env, jint initsize = 0);

  //##########################################################################
  //# Access
  jni::ClassCache* getCache() const {return &mCache;}
  void clear() {mTable.clear();}
  jobject get(jobject key) const {return mTable.get(key);}
  jobject add(jobject value) {return mTable.add(value);}

  //##########################################################################
  //# Iteration
  int size() const {return mTable.size();}
  HashTableIterator iterator() const {return mTable.iterator();}
  bool hasNext(HashTableIterator& iter) const {return mTable.hasNext(iter);}
  jobject next(HashTableIterator& iter) const {return mTable.next(iter);}

private:
  //##########################################################################
  //# Data Members
  mutable jni::ClassCache mCache;
  const ObjectHashAccessor mAccessor;
  PtrHashTable<jobject,jobject> mTable;
};


}   /* namespace waters */

#endif  /* !_JavaHashTable_h_ */
