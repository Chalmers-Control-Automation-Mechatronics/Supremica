//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonEncoding
//###########################################################################
//# $Id$
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
#include "waters/base/HashTable.h"
#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class EventGlue;
  class JavaString;
  class KindTranslatorGlue;
  class MapGlue;
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
  friend class AutomatonRecord;
  explicit AutomatonRecordHashAccessor() {};

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

class AutomatonRecord : public IntHashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit AutomatonRecord(const jni::AutomatonGlue& aut,
			   bool plant,
			   const jni::EventGlue& alpha,
			   const jni::EventGlue& omega,
			   jni::ClassCache* cache);
  ~AutomatonRecord();

  //##########################################################################
  //# Simple Access
  const jni::AutomatonGlue& getJavaAutomaton() const {return mJavaAutomaton;}
  inline bool isPlant() const {return mIsPlant;}
  inline uint32 getNumberOfStates() const {return mNumStates;}
  inline uint32 getFirstInitialState1() const {return mFirstInitialState1;}
  inline uint32 getEndOfInitialStates1() const {return mEndInitialStates1;}
  inline uint32 getFirstInitialState2() const {return mFirstInitialState2;}
  inline uint32 getEndOfInitialStates2() const {return mEndInitialStates2;}
  uint32 getNumberOfInitialStates() const;
  inline uint32 getFirstMarkedState() const {return mFirstMarkedState;}
  inline uint32 getNumberOfMarkedStates() const
    {return mNumStates - mFirstMarkedState;}
  inline bool isMarkedState(uint32 code) const
    {return code >= mFirstMarkedState;}
  inline bool isAllMarked() const {return mFirstMarkedState == 0;}
  inline uint32 getNumberOfPreMarkedStates() const
    {return mEndPreMarkedStates - mFirstPreMarkedState;}
  inline bool isPreMarkedState(uint32 code) const
    {return code >= mFirstPreMarkedState && code < mEndPreMarkedStates;}
  inline bool isAllPreMarked() const
    {return mFirstPreMarkedState == 0 && mEndPreMarkedStates == mNumStates;}
  inline int getNumberOfBits() const {return mNumBits;}
  inline int getAutomatonIndex() const {return mAutomatonIndex;}
  inline int getWordIndex() const {return mWordIndex;}
  inline int getShift() const {return mShift;}
  inline int getBitMask() const {return mBitMask;}
  jni::JavaString getName() const;
  jni::JavaString getStateName(uint32 code) const;
  const jni::StateGlue& getJavaState(uint32 code) const;

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const AutomatonRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);
  int compareToByMarking(const AutomatonRecord* partner) const;
  static int compareByMarking(const void* elem1, const void* elem2);
  int compareToByPreMarking(const AutomatonRecord* partner) const;
  static int compareByPreMarking(const void* elem1, const void* elem2);
  static inline const HashAccessor* getHashAccessor()
    {return &theHashAccessor;}

  //##########################################################################
  //# Setting up
  inline void setAutomatonIndex(int index) {mAutomatonIndex = index;}
  void allocate(int wordindex, int shift);
  HashTable<const jni::StateGlue*,uint32>* createStateMap();
  void deleteStateMap(HashTable<const jni::StateGlue*,uint32>* statemap);

  //##########################################################################
  //# Hash Methods (for states!!!)
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const;

private:
  //##########################################################################
  //# Auxiliary Methods
  void initNonMarking(jni::ClassCache* cache, bool allmarked);
  void initMarking(const jni::EventGlue& marking,
		   uint32& firstmarkedref,
		   jni::ClassCache* cache);
  void initMarking(const jni::EventGlue& alpha,
		   const jni::EventGlue& omega,
		   jni::ClassCache* cache);
  static int getCategory(const jni::StateGlue& state,
			 const jni::EventGlue& marking,
			 jni::ClassCache* cache);
  static int getCategory(const jni::StateGlue& state,
			 const jni::EventGlue& alpha,
			 const jni::EventGlue& omega,
			 jni::ClassCache* cache);

  //##########################################################################
  //# Data Members
  jni::AutomatonGlue mJavaAutomaton;
  jni::StateGlue* mJavaStates;
  bool mIsPlant;
  uint32 mNumStates;
  int mNumBits;
  int mAutomatonIndex;
  int mWordIndex;
  int mShift;
  uint32 mBitMask;
  uint32 mFirstInitialState1;
  uint32 mEndInitialStates1;
  uint32 mFirstInitialState2;
  uint32 mEndInitialStates2;
  uint32 mFirstMarkedState;
  uint32 mFirstPreMarkedState;
  uint32 mEndPreMarkedStates;

  //##########################################################################
  //# Class Constants
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
  explicit AutomatonEncoding(const jni::ProductDESGlue& des,
			     const jni::KindTranslatorGlue& translator,
			     const jni::EventGlue& alpha,
			     const jni::EventGlue& omega,
			     jni::ClassCache* cache,
			     int numtags = 0);
  ~AutomatonEncoding();

  //##########################################################################
  //# Simple Access
  inline int getNumberOfTagBits() const {return mNumTags;}
  inline int getNumberOfWords() const {return mNumWords;}
  inline int getNumberOfRecords() const {return mNumRecords;}
  inline AutomatonRecord* getRecord(int index) const
    {return mAutomatonRecords[index];}
  uint32 getInverseTagMask() const;
  bool hasSpecs() const;
  int getNumberOfNondeterministicInitialAutomata() const;

  //##########################################################################
  //# Encoding and Decoding
  void encode(const uint32* decoded, uint32* encoded) const;
  void decode(const uint32* encoded, uint32* decoded) const;
  uint32 get(const uint32* encoded, int index) const;
  void set(uint32* encoded, int index, uint32 code) const;
  void shift(uint32* decoded) const;

  //##########################################################################
  //# Marking
  bool isTriviallyNonblocking() const {return mIsTriviallyNonblocking;}
  bool isTriviallyBlocking() const {return mIsTriviallyBlocking;}
  bool isMarkedStateTuplePacked(const uint32* encoded) const;
  bool isMarkedStateTuple(const uint32* decoded) const;
  bool isPreMarkedStateTuplePacked(const uint32* encoded) const;
  bool isPreMarkedStateTuple(const uint32* decoded) const;

  //##########################################################################
  //# Masking
  void initMask(uint32* mask) const;
  void addToMask(uint32* mask, int index) const;
  bool equals(const uint32* encoded1,
	      const uint32* encoded2,
	      const uint32* nmask) const;

  //##########################################################################
  //# Tagging
  inline bool hasTag(const uint32* encoded, const uint32 tag) const
    {return (encoded[0] & tag) != 0;}
  inline void setTag(uint32* encoded, const uint32 tag) const
    {encoded[0] |= tag;}
  inline void clearTag(uint32* encoded, const uint32 tag) const
    {encoded[0] &= ~tag;}

  //##########################################################################
  //# Trace Computation
  void storeNondeterministicInitialStates
    (const uint32* tuple, const jni::MapGlue& statemap) const;

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump() const;
  void dumpEncodedState(const uint32* encoded) const;
  void dumpDecodedState(const uint32* decoded) const;
#endif /* DEBUG */

  //##########################################################################
  //# Public Class Constants
  static const uint32 TAG0 = 0x00000001;
  static const uint32 TAG1 = 0x00000002;
  static const uint32 TAG2 = 0x00000004;
  static const uint32 TAG3 = 0x00000008;

private:
  //##########################################################################
  //# Auxiliary Methods
  void setupMarkingTest();

  //##########################################################################
  //# Data Members
  AutomatonRecord** mAutomatonRecords;
  int mNumTags;
  int mNumRecords;
  int mNumWords;
  int* mWordStop;
  bool mIsTriviallyNonblocking;
  bool mIsTriviallyBlocking;
  const AutomatonRecord** mMarkingTestRecords;
  int mNumMarkingTestRecords;
  const AutomatonRecord** mPreMarkingTestRecords;
  int mNumPreMarkingTestRecords;
};


}   /* namespace waters */


#endif  /* !_AutomatonEncoding_h_ */
