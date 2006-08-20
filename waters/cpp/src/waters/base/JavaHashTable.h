//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   JavaHashTable
//###########################################################################
//# $Id: JavaHashTable.h,v 1.1 2006-08-20 08:39:41 robi Exp $
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

namespace jni {
  class ObjectGlue;
}


namespace waters {

class HashAccessor;


//###########################################################################
//# Class ObjectHashAccessor
//###########################################################################

class ObjectHashAccessor : public HashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit ObjectHashAccessor() {};

public:
  //##########################################################################
  //# Construction
  static const HashAccessor* getInstance() {return &theInstance;}

  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const {return value;};
  virtual void* getDefaultValue() const {return 0;}  

private:
  //##########################################################################
  //# Class Variables
  static const ObjectHashAccessor theInstance;
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
  jni::ObjectGlue* get(jni::ObjectGlue* key) const
    {return mTable.get(key);}
  jni::ObjectGlue* add(jni::ObjectGlue* value) {return mTable.add(value);}

  //##########################################################################
  //# Iteration
  int size() const {return mTable.size();}
  HashTableIterator iterator() const {return mTable.iterator();}
  bool hasNext(HashTableIterator& iter) const {return mTable.hasNext(iter);}
  jni::ObjectGlue* next(HashTableIterator& iter) const
    {return mTable.next(iter);}

private:
  //##########################################################################
  //# Data Members
  mutable jni::ClassCache mCache;
  waters::HashTable<jni::ObjectGlue*,jni::ObjectGlue*> mTable;
};


}   /* namespace waters */

#endif  /* !_JavaHashTable_h_ */
