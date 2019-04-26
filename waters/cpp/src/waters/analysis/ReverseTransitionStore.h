//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
  uint32_t getFirstPredecessor(uint32_t target) const;

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
