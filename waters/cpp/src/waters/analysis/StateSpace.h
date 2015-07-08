//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   StateSpace
//###########################################################################
//# $Id$
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
