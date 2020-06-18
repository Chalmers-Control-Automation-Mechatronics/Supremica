//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
