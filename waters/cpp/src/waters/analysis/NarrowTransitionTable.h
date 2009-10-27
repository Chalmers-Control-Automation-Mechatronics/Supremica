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
#include "waters/base/IntTypes.h"


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
     const HashTable<const jni::EventGlue*, NarrowEventRecord*>& eventmap);
  ~NarrowTransitionTable();

  //##########################################################################
  //# Simple Access
  inline uint32 getAutomatonIndex() const {return mAutomatonIndex;}
  inline bool isPlant() const {return mIsPlant;}
  inline const AutomatonRecord* getAutomaton() const {return mAutomaton;}

  //##########################################################################
  //# Iteration
  inline uint32 iterator(uint32 state) const {return mStateTable[state];}
  inline uint32 hasNext(uint32 iterator) const
    {return mBuffers[iterator] != UNDEF_UINT32;}
  inline uint32 next(uint32 iterator) const {return iterator + 2;}
  inline uint32 getEvent(uint32 iterator) const {return mBuffers[iterator];}
  inline uint32 getRawSuccessors(uint32 iterator) const
    {return mBuffers[iterator + 1];}
  inline uint32 getRawNondetSuccessor(uint32 offset) const
    {return mBuffers[offset];}

  //##########################################################################
  //# Setup
  void reverse(const NarrowEventRecord* const* events);

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump(uint32 a, const NarrowEventRecord* const* events) const;
#endif /* DEBUG */

  //##########################################################################
  //# Class Constants
  static const uint32 TAG_END_OF_LIST = 0x80000000;

private:
  //##########################################################################
  //# Data Members
  AutomatonRecord* mAutomaton;
  uint32 mAutomatonIndex;
  bool mIsPlant;
  uint32 mNumStates;
  uint32 mNumTransitions;
  uint32* mStateTable;
  uint32* mBuffers;
};


}   /* namespace waters */

#endif  /* !_NarrowTransitionTable_h_ */
