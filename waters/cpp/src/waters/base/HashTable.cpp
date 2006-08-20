//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashTable
//###########################################################################
//# $Id: HashTable.cpp,v 1.4 2006-08-20 08:39:41 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "waters/base/HashAccessor.h"
#include "waters/base/HashTable.h"


namespace waters {

//###########################################################################
//# Class HashOverflowPair
//###########################################################################

class HashOverflowPair {
  //#########################################################################
  //# Friends
  friend class UntypedHashTable;
  friend class HashOverflowBucket;

  //#########################################################################
  //# Data Members
private:
  void* mValue;
  void* mNext;
};


//###########################################################################
//# Class HashOverflowBucket
//###########################################################################

const uint32 OVERFLOWBUCKETSIZE = 256;
const uint32 OVERFLOWBUCKETSLOTS = (OVERFLOWBUCKETSIZE - 2) >> 1;


class HashOverflowBucket {
public:
  //#########################################################################
  //# Constructors & Destructors
  HashOverflowBucket();
  HashOverflowBucket(HashOverflowBucket* next);
  ~HashOverflowBucket();

  //#########################################################################
  //# Access
  void reset() {mNextSlot = 0;};
  HashOverflowPair* newSlot();
  HashOverflowBucket* getNext() const {return mNextBucket;};
  void setNext(HashOverflowBucket* next) {mNextBucket = next;};

  //#########################################################################
  //# Iteration
  uint32 iterationSize() const {return mNextSlot << 1;};
  void* iterationGet(uint32 iter) const;

  //#########################################################################
  //# Recycling
  void rehash(UntypedHashTable* table);

private:
  //#########################################################################
  //# Data Members
  HashOverflowBucket* mNextBucket;
  uint32 mNextSlot;
  HashOverflowPair mSlots[OVERFLOWBUCKETSLOTS];  
};


//###########################################################################
//# HashOverflowBucket: Constructors & Destructors

HashOverflowBucket::
HashOverflowBucket()
{
  mNextBucket = 0;
  mNextSlot = 0;
}

HashOverflowBucket::
HashOverflowBucket(HashOverflowBucket* next)
{
  mNextBucket = next;
  mNextSlot = 0;
}

HashOverflowBucket::
~HashOverflowBucket()
{
  delete mNextBucket;
}


//###########################################################################
//# HashOverflowBucket: Acccess

HashOverflowPair* HashOverflowBucket::
newSlot()
{
  if (mNextSlot < OVERFLOWBUCKETSLOTS) {
    return &mSlots[mNextSlot++];
  } else {
    return 0;
  }
}


//###########################################################################
//# HashOverflowBucket: Iteration

void* HashOverflowBucket::
iterationGet(uint32 iter)
  const
{
  const HashOverflowPair& pair = mSlots[iter >> 1];
  if (iter & 1) {
    return pair.mValue;
  } else {
    return pair.mNext;
  }
}


//###########################################################################
//# HashOverflowBucket: Recycling

void HashOverflowBucket::
rehash(UntypedHashTable* table)
{
  for (uint32 i = 0; i < mNextSlot; i++) {
    void* value = mSlots[i].mValue;
    table->add(value);
    value = mSlots[i].mNext;
    if (((uint32) value & 1) == 0) {
      table->add(value);
    }
  }
}


//############################################################################
//# class HashTableIterator
//############################################################################

//############################################################################
//# HashTableIterator: Iteration

void HashTableIterator::
skip()
{
  mIndex++;
  if (mBucket && mIndex == mBucket->iterationSize()) {
    mIndex = 0;
    mBucket = mBucket->getNext();
  }
}


//###########################################################################
//# Class UntypedHashTable
//###########################################################################

//###########################################################################
//# UntypedHashTable: Constructors & Destructors

UntypedHashTable::
UntypedHashTable(const HashAccessor* accessor, uint32 size)
{
  mAccessor = accessor;
  mDefault = mAccessor->getDefaultValue();
  size = size < 16 ? 16 : tablesize(size);
  mNumElements = 0;
  mTableMask = size - 1;
  mTable = new void*[size];
  for (uint32 i = 0; i < size; i++) {
    mTable[i] = mDefault;
  }
  mOverflowList = 0;
  mRecycledList = 0;
}


UntypedHashTable::
~UntypedHashTable()
{
  delete [] mTable;
  delete mOverflowList;
  delete mRecycledList;
}


//###########################################################################
//# UntypedHashTable: Access

void UntypedHashTable::
clear()
{
  mNumElements = 0;
  for (uint32 i = 0; i <= mTableMask; i++) {
    mTable[i] = mDefault;
  }
  if (mOverflowList != 0) {
    HashOverflowBucket* last = mOverflowList;
    while (HashOverflowBucket* next = last->getNext()) {
      last = next;
    }
    last->setNext(mRecycledList);
    mRecycledList = mOverflowList;
    mOverflowList = 0;
  }
}

void* UntypedHashTable::
get(const void* key)
  const
{
  uint32 index = mAccessor->hash(key) & mTableMask;
  void* value = mTable[index];
  if (value == mDefault) {
    return value;
  }
  while ((uint32) value & 1) {
    HashOverflowPair* pair = (HashOverflowPair*) ((uint32) value & ~1);
    value = pair->mValue;
    if (isfound(key, value)) {
      return value;
    }
    value = pair->mNext;
  }
  return found(key, value);
}


void* UntypedHashTable::
add(void* value)
{
  if (mNumElements >= (int) mTableMask) {
    rehash(mTableMask + 2);
  }

  const void* key = mAccessor->getKey(value);
  uint32 index = mAccessor->hash(key) & mTableMask;
  void** ref = &mTable[index];
  void* present = *ref;
  if (present == mDefault) {
    mNumElements++;
    *ref = value;
    return value;
  }
  while ((uint32) present & 1) {
    HashOverflowPair* pair = (HashOverflowPair*) ((uint32) present & ~1);
    present = pair->mValue;
    if (isfound(key, present)) {
      return present;
    }
    ref = &pair->mNext;
    present = *ref;
  }
  if (isfound(key, present)) {
    return present;
  } else {
    mNumElements++;
    HashOverflowPair* pair = newSlot();
    *ref = (void*) ((uint32) pair | 1);
    pair->mValue = present;
    pair->mNext = value;
    return value;
  }
}


void UntypedHashTable::
rehash(uint32 newsize)
{
  uint32 oldsize = mTableMask + 1;
  void** oldtable = mTable;
  HashOverflowBucket* oldoverflow = mOverflowList;
  uint32 i;

  newsize = newsize < 16 ? 16 : tablesize(newsize);
  mNumElements = 0;
  mTableMask = newsize - 1;
  mTable = new void*[newsize];
  for (i = 0; i < newsize; i++) {
    mTable[i] = mDefault;
  }
  mOverflowList = 0;

  while (oldoverflow != 0) {
    HashOverflowBucket* next = oldoverflow->getNext();
    oldoverflow->rehash(this);
    recycle(oldoverflow);
    oldoverflow = next;
  }
  for (i = 0; i < oldsize; i++) {
    void* value = oldtable[i];
    if ((value != mDefault) && (((uint32) value & 1) == 0)) {
      add(value);
    }
  }

  delete [] oldtable;
}


//###########################################################################
//# UntypedHashTable: Iteration

HashTableIterator UntypedHashTable::
iterator()
  const
{
  HashTableIterator iter(mOverflowList);
  return iter;
}


bool UntypedHashTable::
hasNext(HashTableIterator& iter)
  const
{
  return advance(iter) != mDefault;
}


void* UntypedHashTable::
untypedNext(HashTableIterator& iter)
  const
{
  void* item = advance(iter);
  if (item != mDefault) {
    skip(iter);
  }
  return item;
}


//###########################################################################
//# UntypedHashTable: Auxiliary Methods

bool UntypedHashTable::
isfound(const void* key, void* value) const
{
  const void* foundkey = mAccessor->getKey(value);
  return mAccessor->equals(key, foundkey);
}


void* UntypedHashTable::
found(const void* key, void* value) const
{
  if (isfound(key, value)) {
    return value;
  } else {
    return mDefault;
  }
}

  
void* UntypedHashTable::
advance(HashTableIterator& iter)
  const
{
  while (true) {
    const uint32 index = iter.getIndex();
    if (const HashOverflowBucket* bucket = iter.getBucket()) {
      void* item = bucket->iterationGet(index);
      if (((uint32) item & 1) == 0) {
        return item;
      }
    } else {
      if (index > mTableMask) {
        return mDefault;
      }
      void* item = mTable[index];
      if (item != mDefault && ((uint32) item & 1) == 0) {
        return item;
      }
    }
    skip(iter);
  }
}
      

void UntypedHashTable::
skip(HashTableIterator& iter)
  const
{
  iter.skip();
}


HashOverflowBucket* UntypedHashTable::
newBucket()
{
  HashOverflowBucket* bucket;
  if (mRecycledList == 0) {
    bucket = new HashOverflowBucket(mOverflowList);
  } else {
    bucket = mRecycledList;
    mRecycledList = bucket->getNext();
    bucket->reset();
    bucket->setNext(mOverflowList);
  }
  mOverflowList = bucket;
  return bucket;
}


HashOverflowPair* UntypedHashTable::
newSlot()
{
  if (mOverflowList == 0) {
    return newBucket()->newSlot();
  } else {
    HashOverflowPair* slot = mOverflowList->newSlot();
    if (slot != 0) {
      return slot;
    } else {
      return newBucket()->newSlot();
    }
  }
}


void UntypedHashTable::
recycle(HashOverflowBucket* bucket)
{
  bucket->setNext(mRecycledList);
  mRecycledList = bucket;
}


}  /* namespace waters */
