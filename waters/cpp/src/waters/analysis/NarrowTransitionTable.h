//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowTransitionTable
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _NarrowTransitionTable_h_
#define _NarrowTransitionTable_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/HashTable.h"
#include <stdint.h>


namespace jni {
  class ClassCache;
  class EventGlue;
}


namespace waters {

class AutomatonRecord;
class NarrowEventRecord;
class NarrowPreTransitionTable;


//############################################################################
//# class NarrowTransitionTable
//############################################################################

class NarrowTransitionTable
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowTransitionTable
    (const NarrowPreTransitionTable* pre,
     jni::ClassCache* cache,
     const PtrHashTable<const jni::EventGlue*, NarrowEventRecord*>& eventmap);
  explicit NarrowTransitionTable(const NarrowTransitionTable* reverse,
				 const NarrowEventRecord* const* events);
  ~NarrowTransitionTable();

  //##########################################################################
  //# Simple Access
  inline uint32_t getAutomatonIndex() const {return mAutomatonIndex;}
  inline bool isPlant() const {return mIsPlant;}
  inline const AutomatonRecord* getAutomaton() const {return mAutomaton;}

  //##########################################################################
  //# Iteration
  inline uint32_t iterator(uint32_t state) const {return mStateTable[state];}
  inline uint32_t hasNext(uint32_t iterator) const
    {return mBuffers[iterator] != UINT32_MAX;}
  inline uint32_t next(uint32_t iterator) const {return iterator + 2;}
  inline uint32_t getEvent(uint32_t iterator) const {return mBuffers[iterator];}
  inline uint32_t getRawSuccessors(uint32_t iterator) const
    {return mBuffers[iterator + 1];}
  inline uint32_t getRawNondetSuccessor(uint32_t offset) const
    {return mBuffers[offset];}

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump(uint32_t a, const NarrowEventRecord* const* events) const;
#endif /* DEBUG */

  //##########################################################################
  //# Class Constants
  static const uint32_t TAG_END_OF_LIST = 0x80000000;

  //##########################################################################
  //# Data Members
  AutomatonRecord* mAutomaton;
  uint32_t mAutomatonIndex;
  bool mIsPlant;
  uint32_t mNumStates;
  uint32_t mNumTransitions;
  uint32_t* mStateTable;
  uint32_t* mBuffers;
};


}   /* namespace waters */

#endif  /* !_NarrowTransitionTable_h_ */
