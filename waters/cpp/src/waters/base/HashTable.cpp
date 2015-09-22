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

#include "waters/base/HashAccessor.h"
#include "waters/base/HashTable.h"


namespace waters {


//###########################################################################
//# Class RawHashTable
//###########################################################################

//###########################################################################
//# RawHashTable: Constructors & Destructors

template <typename K, typename V>
RawHashTable<K,V>::
RawHashTable(const HashAccessor<K,V>* accessor, hashindex_t size)
{
  mAccessor = accessor;
  mDefault = mAccessor->getDefaultValue();
  size = size < 16 ? 16 : tablesize(size);
  mMask = size - 1;
  mShiftDown = 64 - log2(size);
  mThreshold = size >> 1;
  mNumElements = 0;
  mTable = new V[size];
  for (hashindex_t i = 0; i < size; i++) {
    mTable[i] = mDefault;
  }
}


template <typename K, typename V>
RawHashTable<K,V>::
~RawHashTable()
{
  delete [] mTable;
}


//###########################################################################
//# RawHashTable: Access

template <typename K, typename V>
void RawHashTable<K,V>::
clear()
{
  mNumElements = 0;
  hashindex_t size = allocatedSize();
  for (hashindex_t i = 0; i < size; i++) {
    mTable[i] = mDefault;
  }
}


template <typename K, typename V>
V RawHashTable<K,V>::
get(K key)
  const
{
  hashindex_t index = mAccessor->hash(key) >> mShiftDown;
  V value = mTable[index];
  hashindex_t probe = 1;
  while (value != mDefault && !isFound(key, value)) {
    index = (index + probe++) & mMask;
    value = mTable[index];
  }
  return value;
}


template <typename K, typename V>
V RawHashTable<K,V>::
add(V value)
{
  if (mNumElements >= mThreshold) {
    rehash(mMask + 2);
  }  
  K key = mAccessor->getKey(value);
  hashindex_t index = mAccessor->hash(key) >> mShiftDown;
  hashindex_t probe = 1;
  for (hashindex_t i = 0; i <= mNumElements; i++) {
    V present = mTable[index];
    if (present == mDefault) {
      mNumElements++;
      return mTable[index] = value;
    } else if (isFound(key, present)) {
      return present;
    }
    index = (index + probe++) & mMask;
  }
  rehash(mMask + 2);
  return add(value);
}


template <typename K, typename V>
void RawHashTable<K,V>::
rehash(hashindex_t newsize)
{
  hashindex_t i;
  hashindex_t oldsize = allocatedSize();
  V* oldtable = mTable;
  newsize = newsize < 16 ? 16 : tablesize(newsize);
  mMask = newsize - 1;
  mShiftDown = 64 - log2(newsize);
  mThreshold = newsize >> 1;
  mNumElements = 0;
  mTable = new V[newsize];
  for (i = 0; i < newsize; i++) {
    mTable[i] = mDefault;
  }
  for (i = 0; i < oldsize; i++) {
    V value = oldtable[i];
    if (value != mDefault)  {
      add(value);
    }
  }
  delete [] oldtable;
}


//###########################################################################
//# RawHashTable: Iteration

template <typename K, typename V>
bool RawHashTable<K,V>::
hasNext(HashTableIterator& iter)
  const
{
  hashindex_t index = iter.getIndex();
  while (index <= mMask) {
    V item = mTable[index];
    if (item != mDefault) {
      return true;
    }
    index = iter.skip();
  }
  return false;
}


template <typename K, typename V>
V RawHashTable<K,V>::
rawNext(HashTableIterator& iter)
  const
{
  hashindex_t index = iter.getIndex();
  while (index <= mMask) {
    V item = mTable[index];
    index = iter.skip();
    if (item != mDefault) {
      return item;
    }
  }
  return mDefault;
}


//###########################################################################
//# RawHashTable: Auxiliary Methods

template <typename K, typename V>
bool RawHashTable<K,V>::
isFound(const K key, V value) const
{
  K foundKey = mAccessor->getKey(value);
  return mAccessor->equals(key, foundKey);
}


template <typename K, typename V>
V RawHashTable<K,V>::
found(const K key, V value) const
{
  if (isFound(key, value)) {
    return value;
  } else {
    return mDefault;
  }
}
      

//###########################################################################
//# RawHashTable: Instances

template class RawHashTable<int32_t,int32_t>;
template class RawHashTable<int64_t,int64_t>;
template class RawHashTable<int64_t,int32_t>;

}  /* namespace waters */








