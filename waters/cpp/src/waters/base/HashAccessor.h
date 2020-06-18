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
