//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonEncoding
//###########################################################################
//# $Id: AutomatonEncoding.h,v 1.1 2006-08-17 05:02:25 robi Exp $
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

#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class ProductDESGlue;
}


namespace waters {

class HashAccessor;


//###########################################################################
//# Class AutomatonRecordHashAccessor
//###########################################################################

class AutomatonRecordHashAccessor : public HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit AutomatonRecordHashAccessor() {};

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
			   bool spec,
			   jni::ClassCache* cache);

  //##########################################################################
  //# Simple Access
  const jni::AutomatonGlue& getJavaAutomaton() const {return mJavaAutomaton;}
  bool isSpec() const {return mIsSpec;}
  int getNumberOfStates() const {return mNumStates;}
  int getNumberOfBits() const {return mNumBits;}
  int getWordIndex() const {return mWordIndex;}
  int getShift() const {return mShift;}
  int getBitMask() const {return mBitMask;}
  void allocate(int wordindex, int shift);

  //##########################################################################
  //# Comparing
  int compareTo(const AutomatonRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);

private:
  //##########################################################################
  //# Data Members
  jni::AutomatonGlue mJavaAutomaton;
  bool mIsSpec;
  int mNumStates;
  int mNumBits;
  int mWordIndex;
  int mShift;
  uint32 mBitMask;
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
