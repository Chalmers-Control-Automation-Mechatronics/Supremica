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

#include "waters/base/ArrayList.h"
#include "waters/base/IntTypes.h"
#include "waters/base/HashTable.h"


namespace waters {

class AutomatonEncoding;


//############################################################################
//# class StateSpace
//############################################################################

class StateSpace : public IntHashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit StateSpace(const AutomatonEncoding* encoding, uint32 limit);
  virtual ~StateSpace();

  //##########################################################################
  //# Simple Access
  inline int getEncodingSize() const {return mEncodingSize;}
  inline uint32 size() const {return mNumStates;}

  //##########################################################################
  //# Access
  uint32* get(uint32 index) const;
  uint32* prepare();
  uint32* prepare(uint32 index);
  uint32 add();
  inline uint32 find() const {return mLookupTable.get(mNumStates);}
  void clear();

  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const {return value;}

private:
  //##########################################################################
  //# Data Members
  int mEncodingSize;
  uint32 mNumStates;
  uint32 mStateLimit;
  ArrayList<uint32*> mBlocks;
  HashTable<uint32,uint32> mLookupTable;

  //##########################################################################
  //# Class Constants
  static const uint32 INITBLOCKS = 256;
  static const uint32 BLOCKSHIFT = 10;
  static const uint32 BLOCKSIZE = 1 << BLOCKSHIFT;
  static const uint32 BLOCKMASK = BLOCKSIZE - 1;
};


//############################################################################
//# class TaggedStateSpace
//############################################################################

class TaggedStateSpace : public StateSpace
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TaggedStateSpace(const AutomatonEncoding* encoding, uint32 limit);

  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;

private:
  //##########################################################################
  //# Data Members
  int mMask0;
};


}   /* namespace waters */

#endif  /* !_StateSpace_h_ */
