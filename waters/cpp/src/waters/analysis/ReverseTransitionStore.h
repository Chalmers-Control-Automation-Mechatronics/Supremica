//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ReverseTransitionStore
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _ReverseTransitionStore_h_
#define _ReverseTransitionStore_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/ArrayList.h"
#include <stdint.h>


namespace waters {


//############################################################################
//# class ReverseTransitionStore
//############################################################################

class ReverseTransitionStore
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ReverseTransitionStore(uint32_t limit);
  virtual ~ReverseTransitionStore();

  //##########################################################################
  //# Simple Access
  inline uint32_t getNumberOfTransitions() const {return mNumTransitions;}
  inline uint32_t getTransitionLimit() const {return mTransitionLimit;}

  //##########################################################################
  //# Access
  void addTransition(uint32_t source, uint32_t target);
  uint32_t iterator(uint32_t target) const;
  uint32_t hasNext(uint32_t iterator) const;
  uint32_t next(uint32_t& iterator) const;

  //##########################################################################
  //# Debug Output
#ifdef DEBUG
  void dump(uint32_t numstates) const;
#endif /* DEBUG */

private:
  //##########################################################################
  //# Data Members
  uint32_t mTransitionLimit;
  uint32_t mNumTransitions;
  uint32_t mNextLocalIndex;
  uint32_t mNextGlobalIndex;
  uint32_t* mCurrentNodeBlock;
  ArrayList<uint32_t*> mHeadBlocks;
  ArrayList<uint32_t*> mNodeBlocks;

  //##########################################################################
  //# Class Constants
  static const uint32_t INIT_BLOCKS = 256;
  static const uint32_t BLOCK_SHIFT = 10;
  static const uint32_t BLOCK_SIZE = 1 << BLOCK_SHIFT;
  static const uint32_t BLOCK_MASK = BLOCK_SIZE - 1;

  static const uint32_t NODE_SIZE = 4;
  static const uint32_t NODE_MASK = NODE_SIZE - 1;

  static const uint32_t TAG_DATA = 0x80000000;
};


}   /* namespace waters */

#endif  /* !_ReverseTransitionStore_h_ */
