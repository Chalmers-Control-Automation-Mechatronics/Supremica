//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashTable
//###########################################################################
//# $Id$
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
