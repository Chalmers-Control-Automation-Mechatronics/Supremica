//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

#ifndef _TarjanStateSpace_h_
#define _TarjanStateSpace_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "waters/base/BlockedArrayList.h"
#include "waters/analysis/StateSpace.h"


namespace waters {

class AutomatonEncoding;
class TarjanCallBack;
class TarjanStateSpace;


//############################################################################
//# class TarjanControlStackEntry
//############################################################################

class TarjanControlStackEntry
{
public:
  //##########################################################################
  //# Simple Access
  inline uint32_t getNext() const {return mNext;}
  inline uint32_t isClosing() const {return (mIndex & TAG_CLOSING) != 0;}
  inline uint32_t getExpandingState() const {return mIndex;}
  inline uint32_t getClosingCompIndex() const {return mIndex & ~TAG_CLOSING;}
  inline uint32_t getParent() const {return mParent;}

  inline void setNext(uint32_t next) {mNext = next;}
  inline void setExpandingState(uint32_t state, uint32_t parent)
    {mIndex = state; mParent = parent;}
  inline void setClosingCompIndex(uint32_t index)
    {mIndex = index | TAG_CLOSING;}
  inline void setParent(uint32_t parent) {mParent = parent;}

private:
  //##########################################################################
  //# Data Members
  uint32_t mNext;
  uint32_t mIndex;
  uint32_t mParent;

  //##########################################################################
  //# Class Constants
  static const uint32_t TAG_CLOSING = 0x80000000;
};


//############################################################################
//# class TarjanControlStack
//############################################################################

class TarjanControlStack : private BlockedArrayList<TarjanControlStackEntry>
{
public:
  //##########################################################################
  //# Constructors
  explicit TarjanControlStack();

  //##########################################################################
  //# Simple Access
  inline uint32_t getStackSize() const {return mStackSize;}
  inline uint32_t getMaxStackSize() const {return mMaxStackSize;}
  inline uint32_t getPreTopIndex() const {return mPreTopIndex;}
  inline uint32_t getTopIndex() const {return mPreTop->getNext();}
  inline void updateMaxStackSize()
    { if (mStackSize > mMaxStackSize) {mMaxStackSize = mStackSize;} }

  //##########################################################################
  //# Stack Access
  const TarjanControlStackEntry& top() const;
  TarjanControlStackEntry& top();
  void push(uint32_t state, uint32_t parent);
  void pop();
  TarjanControlStackEntry* moveToTop(uint32_t behindIndex, uint32_t parent);

  inline void clear()
    {BlockedArrayList<TarjanControlStackEntry>::clear();}

  //##########################################################################
  //# Debugging
#ifdef DEBUG
  void dump(const TarjanStateSpace* tarjan) const;
#endif /* DEBUG */

private:
  //##########################################################################
  //# Auxiliary Methods
  void allocate();

  //##########################################################################
  //# Data Members
  uint32_t mStackSize;
  uint32_t mMaxStackSize;
  uint32_t mFreeIndex;
  uint32_t mPreTopIndex;
  TarjanControlStackEntry* mPreTop;
  
  //##########################################################################
  //# Class Constants
  static const uint32_t NO_INDEX = UINT32_MAX;
};


//############################################################################
//# class TarjanStateSpace
//############################################################################

class TarjanStateSpace : public StateSpace
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanStateSpace(const AutomatonEncoding* encoding, uint32_t limit);

  //##########################################################################
  //# Simple Access
  inline uint32_t getNumberOfComponents() const {return mNumComponents;}

  //##########################################################################
  //# Overrides
  virtual uint32_t add();

  //##########################################################################
  //# Control Stack
  bool isOpenState(uint32_t state) const;
  bool isClosedState(uint32_t state) const;
  bool isControlStackEmpty() const;
  bool isTopControlStateClosing() const;
  uint32_t getTopControlState() const;
  uint32_t getTopControlStateParent() const;
  void pushRootControlState(uint32_t state);
  void popControlState();

  //##########################################################################
  //# Algorithm
  uint32_t beginStateExpansion();
  void processTransition(uint32_t source, uint32_t target);
  void endStateExpansion();
  void mayBeCloseComponent(TarjanCallBack *callBack);
  uint32_t getCriticalComponentSize() const;

  //##########################################################################
  //# Trace Search
  void setUpTraceSearch(uint32_t numInit);
  uint32_t getTraceStatus(uint32_t state) const {return getLowLink(state);}
  uint32_t& getTraceStatusRef(uint32_t state) {return getLowLinkRef(state);}

  //##########################################################################
  //# Statistics
  virtual void addStatistics
    (const jni::NativeVerificationResultGlue& vresult) const;

  //##########################################################################
  //# Debugging
#ifdef DEBUG
  void dumpControlStack() const;
  void dumpLowLink(uint32_t state) const;
  uint32_t getStateForCompIndex(uint32_t compIndex) const
    {return mComponentStack.get(compIndex);}
#endif /* DEBUG */

private:
  //##########################################################################
  //# Low Link
  uint32_t getLowLink(uint32_t state) const;
  uint32_t& getLowLinkRef(uint32_t state);
  void setLowLink(uint32_t state, uint32_t lowLink);
  void adjustLowLink(uint32_t state, uint32_t lowLink);

  //##########################################################################
  //# Data Members
  TarjanControlStack mControlStack;
  BlockedArrayList<uint32_t> mComponentStack;
  uint32_t mNumComponents;
  uint32_t mCriticalComponentStart;
  uint32_t mMaxComponentStackHeight;

  //##########################################################################
  //# Class Constants
private:
  static const uint32_t LL_OPEN = 0xffffffff;
  static const uint32_t LL_EXPANDING = 0x80000000;
  static const uint32_t LL_CLOSED = 0x7fffffff;

public:
  static const uint32_t TR_INIT = 0xfffffffd;
  static const uint32_t TR_OPEN = 0xfffffffe;
  static const uint32_t TR_CRITICAL = 0xffffffff;
};


//############################################################################
//# abstract class TarjanCallBack
//############################################################################

class TarjanCallBack
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanCallBack(TarjanStateSpace* tarjan) : mTarjan(*tarjan) {}
  virtual ~TarjanCallBack() {}

  //##########################################################################
  //# Simple Access
  inline TarjanStateSpace& getTarjan() const {return mTarjan;}

  //##########################################################################
  //# Interface TarjanCallBack
  virtual bool addStateToComponent(uint32_t state) = 0;

private:
  //##########################################################################
  //# Data Members
  TarjanStateSpace& mTarjan;
};

}   /* namespace waters */

#endif  /* !_TarjanStateSpace_h_ */








