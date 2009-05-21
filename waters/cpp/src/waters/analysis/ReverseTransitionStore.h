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
#include "waters/base/IntTypes.h"


namespace waters {


//############################################################################
//# class ReverseTransitionStore
//############################################################################

class ReverseTransitionStore
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ReverseTransitionStore(uint32 limit);
  virtual ~ReverseTransitionStore();

  //##########################################################################
  //# Simple Access
  inline uint32 getNumberOfTransitions() const {return mNumTransitions;}
  inline uint32 getTransitionLimit() const {return mTransitionLimit;}

  //##########################################################################
  //# Access
  void addTransition(uint32 source, uint32 target);
  uint32 iterator(uint32 target) const;
  uint32 hasNext(uint32 iterator) const;
  uint32 next(uint32& iterator) const;

private:
  //##########################################################################
  //# Data Members
  uint32 mTransitionLimit;
  uint32 mNumTransitions;
  uint32 mNextLocalIndex;
  uint32 mNextGlobalIndex;
  uint32* mCurrentNodeBlock;
  ArrayList<uint32*> mHeadBlocks;
  ArrayList<uint32*> mNodeBlocks;

  //##########################################################################
  //# Class Constants
  static const uint32 INITBLOCKS = 256;
  static const uint32 BLOCKSHIFT = 10;
  static const uint32 BLOCKSIZE = 1 << BLOCKSHIFT;
  static const uint32 BLOCKMASK = BLOCKSIZE - 1;
  static const uint32 TAG_DATA = 0x80000000;
};


}   /* namespace waters */

#endif  /* !_ReverseTransitionStore_h_ */
