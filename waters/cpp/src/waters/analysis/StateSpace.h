//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

#ifndef _StateSpace_h_
#define _StateSpace_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "waters/base/ArrayList.h"
#include "waters/base/HashTable.h"


namespace jni {
  class NativeVerificationResultGlue;
}


namespace waters {

class AutomatonEncoding;


//############################################################################
//# class StateSpace
//############################################################################

class StateSpace : public Int32HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit StateSpace(const AutomatonEncoding* encoding,
		      uint32_t limit,
		      int extraWords = 0);
  virtual ~StateSpace();

  //##########################################################################
  //# Simple Access
  inline int getExtendedTupleSize() const {return mExtendedTupleSize;}
  inline int getSignificantTupleSize() const {return mSignificantTupleSize;}
  inline uint32_t size() const {return mNumStates;}

  //##########################################################################
  //# Access
  uint32_t* get(uint32_t index) const;
  uint32_t* prepare();
  uint32_t* prepare(uint32_t index);
  virtual uint32_t add();
  inline uint32_t find() const {return mLookupTable.get(mNumStates);}
  void clear();

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(int32_t key) const;
  bool equals(int32_t key1, int32_t key2) const;
  virtual bool equalTuples(const uint32_t* tuple1,
			   const uint32_t* tuple2) const;
  virtual int32_t getKey(int32_t value) const {return value;}

  //##########################################################################
  //# Statistics
  virtual void addStatistics
    (const jni::NativeVerificationResultGlue& vresult) const {}

private:
  //##########################################################################
  //# Data Members
  int mExtendedTupleSize;
  int mSignificantTupleSize;
  uint32_t mNumStates;
  uint32_t mStateLimit;
  ArrayList<uint32_t*> mBlocks;
  Int32HashTable<uint32_t,uint32_t> mLookupTable;

  //##########################################################################
  //# Class Constants
  static const uint32_t INITBLOCKS = 256;
  static const uint32_t BLOCKSHIFT = 10;
  static const uint32_t BLOCKSIZE = 1 << BLOCKSHIFT;
  static const uint32_t BLOCKMASK = BLOCKSIZE - 1;
};


//############################################################################
//# class TaggedStateSpace
//############################################################################

class TaggedStateSpace : public StateSpace
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TaggedStateSpace(const AutomatonEncoding* encoding, uint32_t limit);

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(int32_t key) const;
  virtual bool equalTuples(const uint32_t* tuple1,
			   const uint32_t* tuple2) const;

private:
  //##########################################################################
  //# Data Members
  int mMask0;
};


}   /* namespace waters */

#endif  /* !_StateSpace_h_ */
