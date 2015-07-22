//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashTable
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _HashTable_h_
#define _HashTable_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "HashAccessor.h"


namespace waters {


//############################################################################
//# class HashTableIterator
//############################################################################

class HashTableIterator
{
public:
  //##########################################################################
  //# Constructors
  explicit HashTableIterator() : mIndex(0) {}

  //##########################################################################
  //# Simple Access
  hashindex_t getIndex() const {return mIndex;}

  //##########################################################################
  //# Iteration
  hashindex_t skip() {return ++mIndex;}

private:
  //##########################################################################
  //# Data Members
  hashindex_t mIndex;
};


//############################################################################
//# template RawHashTable
//############################################################################

template <typename K, typename V>
class RawHashTable
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit RawHashTable(const HashAccessor<K,V>* accessor,
                        hashindex_t size);
  ~RawHashTable();

  //##########################################################################
  //# Access
  void clear();
  V get(K key) const;
  V add(V value);
  void rehash(hashindex_t newsize);
  const HashAccessor<K,V>* getHashAccessor() const {return mAccessor;}

  //##########################################################################
  //# Iteration
  hashindex_t size() const {return mNumElements;}
  HashTableIterator iterator() const {return HashTableIterator();}
  bool hasNext(HashTableIterator& iter) const;
  V rawNext(HashTableIterator& iter) const;

private:
  //##########################################################################
  //# Auxiliary Methods
  hashindex_t allocatedSize() const {return mMask + 1;}
  bool isFound(K key, V value) const;
  V found(K key, V value) const;

  //##########################################################################
  //# Data Members
  const HashAccessor<K,V>* mAccessor;
  V mDefault;
  hashindex_t mMask;
  int mShiftDown;
  hashindex_t mThreshold;
  hashindex_t mNumElements;
  V* mTable;
};


//############################################################################
//# template Int32HashTable <typed>
//############################################################################

template <typename K, typename V>
class Int32HashTable : public RawHashTable<int32_t,int32_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit Int32HashTable(const HashAccessor<int32_t,int32_t>* accessor,
			  hashindex_t size = 16) :
    RawHashTable<int32_t,int32_t>(accessor, size)
  {
  }

  //##########################################################################
  //# Access
  V get(K key) const
  {
    return (V) RawHashTable<int32_t,int32_t>::get((int32_t) key);
  }

  V add(const V value)
  {
    return (V) RawHashTable<int32_t,int32_t>::add((int32_t) value);
  }

  //##########################################################################
  //# Iteration
  V next(HashTableIterator& iter) const
  {
    return (V) rawNext(iter);
  }
};


//############################################################################
//# template Int64HashTable <typed>
//############################################################################

template <typename K, typename V>
class Int64HashTable : public RawHashTable<int64_t,int64_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit Int64HashTable(const HashAccessor<int64_t,int64_t>* accessor,
			  hashindex_t size = 16) :
    RawHashTable<int64_t,int64_t>(accessor, size)
  {
  }

  //##########################################################################
  //# Access
  V get(K key) const
  {
    return (V) RawHashTable<int64_t,int64_t>::get((int64_t) key);
  }

  V add(const V value)
  {
    return (V) RawHashTable<int64_t,int64_t>::add((int64_t) value);
  }

  //##########################################################################
  //# Iteration
  V next(HashTableIterator& iter) const
  {
    return (V) rawNext(iter);
  }
};


//############################################################################
//# template PtrHashTable <typed>
//############################################################################

template <typename K, typename V>
class PtrHashTable : public RawHashTable<intptr_t,intptr_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit PtrHashTable(const HashAccessor<intptr_t,intptr_t>* accessor,
			hashindex_t size = 16) :
    RawHashTable<intptr_t,intptr_t>(accessor, size)
  {
  }

  //##########################################################################
  //# Access
  V get(K key) const
  {
    return (V) RawHashTable<intptr_t,intptr_t>::get((intptr_t) key);
  }

  V add(const V value)
  {
    return (V) RawHashTable<intptr_t,intptr_t>::add((intptr_t) value);
  }

  //##########################################################################
  //# Iteration
  V next(HashTableIterator& iter) const
  {
    return (V) rawNext(iter);
  }
};


//############################################################################
//# template Int32PtrHashTable <typed>
//############################################################################

template <typename K, typename V>
class Int32PtrHashTable : public RawHashTable<intptr_t,int32_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit Int32PtrHashTable(const HashAccessor<intptr_t,int32_t>* accessor,
			     hashindex_t size = 16) :
    RawHashTable<intptr_t,int32_t>(accessor, size)
  {
  }

  //##########################################################################
  //# Access
  V get(K key) const
  {
    return (V) RawHashTable<intptr_t,int32_t>::get((intptr_t) key);
  }

  V add(const V value)
  {
    return (V) RawHashTable<intptr_t,int32_t>::add((intptr_t) value);
  }

  //##########################################################################
  //# Iteration
  V next(HashTableIterator& iter) const
  {
    return (V) rawNext(iter);
  }
};

}   /* namespace waters */

#endif  /* !_HashTable_h_ */
