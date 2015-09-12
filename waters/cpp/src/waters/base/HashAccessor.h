//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashAccessor
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _HashAccessor_h_
#define _HashAccessor_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "waters/base/WordSize.h"


namespace waters {

//############################################################################
//# Elementary Arithmetic
//############################################################################

#if __WORDSIZE == 64
typedef uint64_t hashindex_t;
#else
typedef uint32_t hashindex_t;
#endif

int log2(hashindex_t x);

inline hashindex_t tablesize(hashindex_t x)
{
  return 1 << log2(x);
}

inline hashindex_t bitmask(hashindex_t x)
{
  return tablesize(x) - 1;
}


//############################################################################
//# Some Hash Functions
//############################################################################

void initHashFactors64(uint32_t size);

void initHashFactors32(uint32_t size);

uint64_t hashInt(uint64_t key);

uint64_t hashInt32Array(const uint32_t* array,
			uint32_t size,
			uint32_t mask0 = ~0);

uint64_t hashInt64Array(const uint64_t* array,
			uint32_t size,
			uint64_t mask0 = ~0);

uint64_t hashString(const char* key);

inline uint64_t hashInt(const void* key) { return hashInt((intptr_t) key); }


//############################################################################
//# class HashAccessor
//############################################################################

template <typename K, typename V>
class HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit HashAccessor() {}
  virtual ~HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(K key) const = 0;
  virtual bool equals(K key1, K key2) const { return key1 == key2; }
  virtual K getKey(V value) const = 0;

  //##########################################################################
  //# Default Value
  virtual V getDefaultValue() const = 0;
};


//############################################################################
//# class Int32HashAccessor
//############################################################################

class Int32HashAccessor : public HashAccessor<int32_t,int32_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit Int32HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(int32_t key) const { return hashInt(key); }
  virtual int32_t getKey(int32_t value) const { return value; }

  //##########################################################################
  //# Default Value
  virtual int32_t getDefaultValue() const { return -1; }
};


//############################################################################
//# class Int64HashAccessor
//############################################################################

class Int64HashAccessor : public HashAccessor<int64_t,int64_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit Int64HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(int64_t key) const { return hashInt(key); }
  virtual int64_t getKey(int64_t value) const { return value; }

  //##########################################################################
  //# Default Value
  virtual int64_t getDefaultValue() const { return -1; }
};


//############################################################################
//# class PtrHashAccessor
//############################################################################

class PtrHashAccessor : public HashAccessor<intptr_t,intptr_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit PtrHashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(intptr_t key) const { return hashInt(key); }
  virtual intptr_t getKey(intptr_t value) const { return value; }

  //##########################################################################
  //# Default Value
  virtual intptr_t getDefaultValue() const { return 0; }
};


//############################################################################
//# class UInt32PtrHashAccessor
//############################################################################

class Int32PtrHashAccessor : public HashAccessor<intptr_t,int32_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit Int32PtrHashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(int32_t key) const { return hashInt(key); }

  //##########################################################################
  //# Default Value
  virtual int32_t getDefaultValue() const { return -1; }
};

}   /* namespace waters */

#endif  /* !_HashAccessor_h_ */
