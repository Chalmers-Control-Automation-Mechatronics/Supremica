//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonEncoding
//###########################################################################
//# $Id: AutomatonEncoding.h,v 1.3 2006-08-21 05:41:39 robi Exp $
//###########################################################################


#ifndef _AutomatonEncoding_h_
#define _AutomatonEncoding_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/AutomatonGlue.h"

#include "waters/base/HashAccessor.h"
#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class ProductDESGlue;
}


namespace waters {


//###########################################################################
//# Class AutomatonRecordHashAccessor
//###########################################################################

class AutomatonRecordHashAccessor : public HashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit AutomatonRecordHashAccessor() {};
  friend class AutomatonRecord;

public:
  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const;
  virtual void* getDefaultValue() const {return 0;}  
};



//############################################################################
//# class AutomatonRecord
//############################################################################

class AutomatonRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit AutomatonRecord(const jni::AutomatonGlue aut,
			   bool plant,
			   jni::ClassCache* cache);

  //##########################################################################
  //# Simple Access
  const jni::AutomatonGlue& getJavaAutomaton() const {return mJavaAutomaton;}
  bool isPlant() const {return mIsPlant;}
  int getNumberOfStates() const {return mNumStates;}
  int getNumberOfBits() const {return mNumBits;}
  int getAutomatonIndex() const {return mAutomatonIndex;}
  int getWordIndex() const {return mWordIndex;}
  int getShift() const {return mShift;}
  int getBitMask() const {return mBitMask;}

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const AutomatonRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}

  //##########################################################################
  //# Setting up
  void allocate(int wordindex, int shift);
  void setAutomatonIndex(int index) {mAutomatonIndex = index;}

private:
  //##########################################################################
  //# Data Members
  jni::AutomatonGlue mJavaAutomaton;
  bool mIsPlant;
  int mNumStates;
  int mNumBits;
  int mAutomatonIndex;
  int mWordIndex;
  int mShift;
  uint32 mBitMask;

  //##########################################################################
  //# Class Variables
  static const AutomatonRecordHashAccessor theHashAccessor;
};



//############################################################################
//# class AutomatonEncoding
//############################################################################

class AutomatonEncoding
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit AutomatonEncoding(const jni::ProductDESGlue des,
			     jni::ClassCache* cache);
  ~AutomatonEncoding();

  //##########################################################################
  //# Simple Access
  int getNumRecords() const {return mNumRecords;}
  const AutomatonRecord* getRecord(int index) const
    {return mAutomatonRecords[index];}

  //##########################################################################
  //# Encoding and Decoding
  void encode(const uint32* decoded, uint32* encoded) const;
  void decode(const uint32* encoded, uint32* decoded) const;
  uint32 get(const uint32* encoded, int index) const;
  void set(uint32* encoded, int index, uint32 code) const;

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump(jni::ClassCache* cache) const;
#endif /* DEBUG */

private:
  //##########################################################################
  //# Data Members
  AutomatonRecord** mAutomatonRecords;
  int mNumRecords;
  int mNumWords;
  int* mWordStop;
};


}   /* namespace waters */


#endif  /* !_AutomatonEncoding_h_ */
