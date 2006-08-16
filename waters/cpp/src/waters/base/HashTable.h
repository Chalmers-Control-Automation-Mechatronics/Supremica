//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashTable
//###########################################################################
//# $Id: HashTable.h,v 1.2 2006-08-16 02:56:42 robi Exp $
//###########################################################################


#ifndef _HashTable_h_
#define _HashTable_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"


namespace waters {

class HashAccessor;
class HashOverflowBucket;
class HashOverflowPair;


//############################################################################
//# class HashTableIterator
//############################################################################

class HashTableIterator
{
public:
  //##########################################################################
  //# Constructors
  explicit HashTableIterator(const HashOverflowBucket* bucket) :
    mIndex(0), mBucket(bucket)
  {}

  //##########################################################################
  //# Simple Access
  uint32 getIndex() const {return mIndex;};
  const HashOverflowBucket* getBucket() const {return mBucket;};

  //##########################################################################
  //# Iteration
  void skip();

private:
  //##########################################################################
  //# Data Members
  uint32 mIndex;
  const HashOverflowBucket* mBucket;
};


//############################################################################
//# class HashTable <typeless>
//############################################################################

class UntypedHashTable
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit UntypedHashTable(const HashAccessor* accessor, uint32 size);
  ~UntypedHashTable();

  //##########################################################################
  //# Access
  void* get(const void* key) const;
  void* add(void* value);
  void rehash(uint32 newsize);

  //##########################################################################
  //# Iteration
  int size() const {return mNumElements;}
  HashTableIterator iterator() const;
  bool hasNext(HashTableIterator& iter) const;
  void* untypedNext(HashTableIterator& iter) const;

private:
  //##########################################################################
  //# Auxiliary Methods
  bool isfound(const void* key, void* value) const;
  void* found(const void* key, void* value) const;
  void* advance(HashTableIterator& iter) const;
  void skip(HashTableIterator& iter) const;
  HashOverflowBucket* newBucket();
  HashOverflowPair* newSlot();
  void recycle(HashOverflowBucket* bucket);

  //##########################################################################
  //# Data Members
  const HashAccessor* mAccessor;
  void* mDefault;
  int mNumElements;
  uint32 mTableMask;
  void** mTable;
  HashOverflowBucket* mOverflowList;
  HashOverflowBucket* mRecycledList;
};


//############################################################################
//# template HashTable <typed>
//############################################################################

template <class Key, class Value>
class HashTable : public UntypedHashTable
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit HashTable(const HashAccessor* accessor, uint32 size = 16) :
    UntypedHashTable(accessor, size)
  {
  }

  ~HashTable()
  {
  }

  //##########################################################################
  //# Access
  Value get(Key key) const
  {
    return (Value) UntypedHashTable::get((void*) key);
  }

  bool add(const Value value)
  {
    return UntypedHashTable::add((void*) value);
  }

  //##########################################################################
  //# Iteration
  Value next(HashTableIterator& iter) const
  {
    return (Value) untypedNext(iter);
  }
};

}   /* namespace waters */

#endif  /* !_HashTable_h_ */
