//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashAccessor64
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _HashAccessor64_h_
#define _HashAccessor64_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>


namespace waters {

//############################################################################
//# Some Hash Functions
//############################################################################

uint64_t hashInt(uint32_t key);

uint64_t hashInt(uint64_t key);

uint64_t hashIntArray(const uint32_t* key, const int len);

uint64_t hashIntArray(const uint32_t* key,
		      const int len,
		      const uint32_t mask0);

uint64_t hashString(const char* key);


inline uint64_t hashInt(int32_t key) { return hashInt((uint32_t) key); }

inline uint64_t hashInt(int64_t key) { return hashInt((uint64_t) key); }

inline uint64_t hashInt(const void* key) { return hashInt((intptr_t) key); }


//############################################################################
//# class HashAccessor
//############################################################################

template <class Key>
class HashAccessor64
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit HashAccessor64() {}
  virtual ~HashAccessor64() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(Key key) const = 0;
  virtual bool equals(Key key1, Key key2) const { return key1 == key2; }
  virtual Key getKey(Key value) const { return value; }

  //##########################################################################
  //# Default Value
  virtual Key getDefaultValue() const = 0;
};


//############################################################################
//# class UInt32HashAccessor
//############################################################################

class UInt32HashAccessor : public HashAccessor64<uint32_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit UInt32HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(uint32_t key) const { return hashInt(key); }

  //##########################################################################
  //# Default Value
  virtual uint32_t getDefaultValue() const { return UINT32_MAX; }
};


//############################################################################
//# class UInt64HashAccessor
//############################################################################

class UInt64HashAccessor : public HashAccessor64<uint64_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit UInt64HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(uint64_t key) const { return hashInt(key); }

  //##########################################################################
  //# Default Value
  virtual uint64_t getDefaultValue() const { return UINT64_MAX; }
};


//############################################################################
//# class PtrHashAccessor
//############################################################################

class PtrHashAccessor : public HashAccessor64<intptr_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit PtrHashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(intptr_t key) const { return hashInt(key); }

  //##########################################################################
  //# Default Value
  virtual intptr_t getDefaultValue() const { return 0; }
};


//############################################################################
//# class UInt32ArrayHashAccessor
//############################################################################

class UInt32ArrayHashAccessor : public PtrHashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit UInt32ArrayHashAccessor(uint32_t size) : mSize(size) {}

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(intptr_t key) const;
  virtual bool equals(intptr_t key1, intptr_t key2) const;

private:
  //##########################################################################
  //# Data Members
  uint32_t mSize;
};

}   /* namespace waters */

#endif  /* !_HashAccessor64_h_ */
