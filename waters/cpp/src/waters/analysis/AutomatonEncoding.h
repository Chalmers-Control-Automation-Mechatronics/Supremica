//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
#include <stdint.h>

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
  virtual uint64_t hash(intptr_t key) const;
  virtual bool equals(intptr_t key1, intptr_t key2) const;
  virtual intptr_t getKey(intptr_t value) const;
};



//############################################################################
//# class AutomatonRecord
//############################################################################

class AutomatonRecord : public Int32PtrHashAccessor
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
  inline uint32_t getNumberOfStates() const {return mNumStates;}
  inline uint32_t getFirstInitialState1() const {return mFirstInitialState1;}
  inline uint32_t getEndOfInitialStates1() const {return mEndInitialStates1;}
  inline uint32_t getFirstInitialState2() const {return mFirstInitialState2;}
  inline uint32_t getEndOfInitialStates2() const {return mEndInitialStates2;}
  uint32_t getNumberOfInitialStates() const;
  inline uint32_t getFirstMarkedState() const {return mFirstMarkedState;}
  inline uint32_t getNumberOfMarkedStates() const
    {return mNumStates - mFirstMarkedState;}
  inline bool isMarkedState(uint32_t code) const
    {return code >= mFirstMarkedState;}
  inline bool isAllMarked() const {return mFirstMarkedState == 0;}
  inline uint32_t getNumberOfPreMarkedStates() const
    {return mEndPreMarkedStates - mFirstPreMarkedState;}
  inline bool isPreMarkedState(uint32_t code) const
    {return code >= mFirstPreMarkedState && code < mEndPreMarkedStates;}
  inline bool isAllPreMarked() const
    {return mFirstPreMarkedState == 0 && mEndPreMarkedStates == mNumStates;}
  inline int getNumberOfBits() const {return mNumBits;}
  inline int getAutomatonIndex() const {return mAutomatonIndex;}
  inline int getWordIndex() const {return mWordIndex;}
  inline int getShift() const {return mShift;}
  inline int getBitMask() const {return mBitMask;}
  jni::JavaString getName() const;
  jni::JavaString getStateName(uint32_t code) const;
  const jni::StateGlue& getJavaState(uint32_t code) const;
  inline int getNumberOfDumpStates() const
    {return mDumpStates == 0 ? 0 : mDumpStates[0];}
  inline int getDumpState(int i) const {return mDumpStates[i + 1];}

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const AutomatonRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);
  int compareToByMarking(const AutomatonRecord* partner) const;
  static int compareByMarking(const void* elem1, const void* elem2);
  int compareToByPreMarking(const AutomatonRecord* partner) const;
  static int compareByPreMarking(const void* elem1, const void* elem2);
  static inline const AutomatonRecordHashAccessor* getHashAccessor()
    {return &theHashAccessor;}

  //##########################################################################
  //# Setting up
  inline void setAutomatonIndex(int index) {mAutomatonIndex = index;}
  void allocate(int wordindex, int shift);
  Int32PtrHashTable<const jni::StateGlue*,uint32_t>* createStateMap();
  void deleteStateMap
    (Int32PtrHashTable<const jni::StateGlue*,uint32_t>* statemap);
  uint32_t setupDumpStates(const bool* dumpStatus);

  //##########################################################################
  //# Hash Methods (for states!!!)
  virtual uint64_t hash(intptr_t key) const;
  virtual bool equals(intptr_t key1, intptr_t key2) const;
  virtual intptr_t getKey(int32_t value) const;

private:
  //##########################################################################
  //# Auxiliary Methods
  void initNonMarking(jni::ClassCache* cache, bool allmarked);
  void initMarking(const jni::EventGlue& marking,
		   uint32_t& firstmarkedref,
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
  uint32_t mNumStates;
  int mNumBits;
  int mAutomatonIndex;
  int mWordIndex;
  int mShift;
  uint32_t mBitMask;
  uint32_t mFirstInitialState1;
  uint32_t mEndInitialStates1;
  uint32_t mFirstInitialState2;
  uint32_t mEndInitialStates2;
  uint32_t mFirstMarkedState;
  uint32_t mFirstPreMarkedState;
  uint32_t mEndPreMarkedStates;
  // Number of dump states followed by state numbers, or NULL.
  uint32_t* mDumpStates;

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
  inline int getEncodingSize() const {return mEncodingSize;}
  inline int getNumberOfTagBits() const {return mNumTags;}
  inline int getNumberOfAutomata() const {return mNumRecords;}
  inline AutomatonRecord* getRecord(int index) const
    {return mAutomatonRecords[index];}
  uint32_t getInverseTagMask() const;
  bool hasSpecs() const;
  int getNumberOfNondeterministicInitialAutomata() const;
  int getNumberOfEncodedBits() const;

  //##########################################################################
  //# Encoding and Decoding
  void encode(const uint32_t* decoded, uint32_t* encoded) const;
  void decode(const uint32_t* encoded, uint32_t* decoded) const;
  uint32_t get(const uint32_t* encoded, int index) const;
  void set(uint32_t* encoded, int index, uint32_t code) const;
  void shift(uint32_t* decoded) const;

  //##########################################################################
  //# Marking
  bool isTriviallyNonblocking() const {return mIsTriviallyNonblocking;}
  bool isTriviallyBlocking() const {return mIsTriviallyBlocking;}
  bool isMarkedStateTuplePacked(const uint32_t* encoded) const;
  bool isMarkedStateTuple(const uint32_t* decoded) const;
  bool isPreMarkedStateTuplePacked(const uint32_t* encoded) const;
  bool isPreMarkedStateTuple(const uint32_t* decoded) const;

  //##########################################################################
  //# Masking
  void initMask(uint32_t* mask) const;
  void addToMask(uint32_t* mask, int index) const;
  bool equals(const uint32_t* encoded1,
	      const uint32_t* encoded2,
	      const uint32_t* nmask) const;

  //##########################################################################
  //# Tagging
  inline bool hasTag(const uint32_t* encoded, const uint32_t tag) const
    {return (encoded[0] & tag) != 0;}
  inline void setTag(uint32_t* encoded, const uint32_t tag) const
    {encoded[0] |= tag;}
  inline void clearTag(uint32_t* encoded, const uint32_t tag) const
    {encoded[0] &= ~tag;}

  //##########################################################################
  //# Trace Computation
  void storeNondeterministicInitialStates
    (const uint32_t* tuple, const jni::MapGlue& statemap) const;

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump() const;
  void dumpEncodedState(const uint32_t* encoded) const;
  void dumpDecodedState(const uint32_t* decoded) const;
#endif /* DEBUG */

  //##########################################################################
  //# Public Class Constants
  static const uint32_t TAG0 = 0x00000001;
  static const uint32_t TAG1 = 0x00000002;
  static const uint32_t TAG2 = 0x00000004;
  static const uint32_t TAG3 = 0x00000008;

private:
  //##########################################################################
  //# Auxiliary Methods
  void setupMarkingTest();

  //##########################################################################
  //# Data Members
  AutomatonRecord** mAutomatonRecords;
  int mEncodingSize;
  int mNumTags;
  int mNumRecords;
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
