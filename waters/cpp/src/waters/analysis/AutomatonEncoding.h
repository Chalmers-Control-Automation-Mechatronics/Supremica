//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonEncoding
//###########################################################################
//# $Id: AutomatonEncoding.h,v 1.7 2006-11-22 21:27:57 robi Exp $
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
  class JavaString;
  class KindTranslatorGlue;
  class ProductDESGlue;
  class StateGlue;
}


namespace waters {


//###########################################################################
//# Class AutomatonRecordHashAccessor
//###########################################################################

class AutomatonRecordHashAccessor : public PtrHashAccessor
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
  ~AutomatonRecord();

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
  jni::JavaString getName() const;
  jni::JavaString getStateName(uint32 code) const;

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
  jni::StateGlue* mJavaStates;
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
  explicit AutomatonEncoding(jni::ProductDESGlue des,
			     jni::KindTranslatorGlue translator,
			     jni::ClassCache* cache);
  ~AutomatonEncoding();

  //##########################################################################
  //# Simple Access
  int getNumberOfWords() const {return mNumWords;}
  int getNumberOfRecords() const {return mNumRecords;}
  const AutomatonRecord* getRecord(int index) const
    {return mAutomatonRecords[index];}

  //##########################################################################
  //# Encoding and Decoding
  void encode(const uint32* decoded, uint32* encoded) const;
  void decode(const uint32* encoded, uint32* decoded) const;
  uint32 get(const uint32* encoded, int index) const;
  void set(uint32* encoded, int index, uint32 code) const;
  void shift(uint32* decoded) const;

  //##########################################################################
  //# Masking
  void initMask(uint32* mask) const;
  void addToMask(uint32* mask, int index) const;
  bool equals(const uint32* encoded1,
	      const uint32* encoded2,
	      const uint32* nmask) const;

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump() const;
  void dumpEncodedState(const uint32* encoded) const;
  void dumpDecodedState(const uint32* decoded) const;
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
