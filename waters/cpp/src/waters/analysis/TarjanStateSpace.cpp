//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>

#include "jni/glue/NativeVerificationResultGlue.h"

#include "waters/analysis/TarjanStateSpace.h"


namespace waters {

//############################################################################
//# class TarjanControlStack
//############################################################################

//############################################################################
//# TarjanControlStack: Constructors & Destructors

TarjanControlStack::
TarjanControlStack()
  : BlockedArrayList<TarjanControlStackEntry>(1024),
    mStackSize(0),
    mMaxStackSize(0),
    mFreeIndex(NO_INDEX)
{
  mPreTopIndex = add();
  mPreTop = &getref(mPreTopIndex);
  mPreTop->setNext(NO_INDEX);
}


//############################################################################
//# TarjanControlStack: Stack Access

const TarjanControlStackEntry& TarjanControlStack::
top()
  const
{
  uint32_t index = mPreTop->getNext();
  return getref(index);
}

TarjanControlStackEntry& TarjanControlStack::
top()
{
  uint32_t index = mPreTop->getNext();
  return getref(index);
}

void TarjanControlStack::
push(uint32_t state, uint32_t parent)
{
  mPreTop->setExpandingState(state, parent);
  allocate();
  mStackSize++;
}

void TarjanControlStack::
pop()
{
  uint32_t topIndex = mPreTop->getNext();
  TarjanControlStackEntry& topEntry = getref(topIndex);
  uint32_t next = topEntry.getNext();
  mPreTop->setNext(next);
  topEntry.setNext(mFreeIndex);
  mFreeIndex = topIndex;
  mStackSize--;
}

TarjanControlStackEntry* TarjanControlStack::
moveToTop(uint32_t behindIndex, uint32_t parent)
{
  uint32_t topIndex = mPreTop->getNext();
  TarjanControlStackEntry& topEntry = getref(topIndex);
  if (behindIndex == mPreTopIndex) {
    topEntry.setParent(parent);
    return 0;
  } else {
    TarjanControlStackEntry& behindEntry = getref(behindIndex);
    uint32_t movedIndex = behindEntry.getNext();
    TarjanControlStackEntry& movedEntry = getref(movedIndex);
    if (movedEntry.getParent() == parent) {
      return 0;
    } else {
      uint32_t beforeIndex = movedEntry.getNext();
      behindEntry.setNext(beforeIndex);
      mPreTop->setNext(movedIndex);
      movedEntry.setNext(topIndex);
      movedEntry.setParent(parent);
      return &getref(beforeIndex);
    }
  }
}


//############################################################################
//# TarjanControlStack: Debugging

#ifdef DEBUG

void TarjanControlStack::
dump(const TarjanStateSpace* tarjan)
  const
{
  std::cerr << "CONTROL STACK pretop=" << mPreTopIndex << std::endl;
  uint32_t index = mPreTop->getNext();
  while (index != NO_INDEX) {
    const TarjanControlStackEntry& entry = getref(index);
    uint32_t state;
    if (entry.isClosing()) {
      std::cerr << "CLO:";
      uint32_t compIndex = entry.getClosingCompIndex();
      state = tarjan->getStateForCompIndex(compIndex);
    } else {
      std::cerr << "EXP:";
      state = entry.getExpandingState();
    }
    std::cerr << state << "[";
    tarjan->dumpLowLink(state);
    std::cerr << "]:" << entry.getParent() << " @ " << index << std::endl;
    index = entry.getNext();
  }
  std::cerr << "CONTROL STACK END" << std::endl;
}

#endif /* DEBUG */


//############################################################################
//# TarjanControlStack: Auxiliary Methods
void TarjanControlStack::
allocate()
{
  uint32_t index;
  if (mFreeIndex == NO_INDEX) {
    index = add();
    mPreTop = &getref(index);
  } else {
    index = mFreeIndex;
    mPreTop = &getref(index);
    mFreeIndex = mPreTop->getNext();
  }
  mPreTop->setNext(mPreTopIndex);
  mPreTopIndex = index;  
}


//############################################################################
//# class TarjanStateSpace
//############################################################################

//############################################################################
//# TarjanStateSpace: Constructors & Destructors

TarjanStateSpace::
TarjanStateSpace(const AutomatonEncoding* encoding, uint32_t limit)
  : StateSpace(encoding, limit, 1),
    mControlStack(),
    mComponentStack(1024),
    mNumComponents(0),
    mCriticalComponentStart(UINT32_MAX),
    mMaxComponentStackHeight(0)
{
}


//############################################################################
//# TarjanStateSpace: Overrides

uint32_t TarjanStateSpace::
add()
{
  uint32_t oldSize = size();
  uint32_t result = StateSpace::add();
  if (result == oldSize) {
    setLowLink(result, LL_OPEN);
  }
  return result;
}


//############################################################################
//# TarjanStateSpace: Control Stack

bool TarjanStateSpace::
isOpenState(uint32_t state)
  const
{
  uint32_t lowLink = getLowLink(state);
  return lowLink == LL_OPEN;
}

bool TarjanStateSpace::
isClosedState(uint32_t state)
  const
{
  uint32_t lowLink = getLowLink(state);
  return lowLink == LL_CLOSED;
}

bool TarjanStateSpace::
isControlStackEmpty()
  const
{
  return mControlStack.getStackSize() == 0;
}

bool TarjanStateSpace::
isTopControlStateClosing()
  const
{
  const TarjanControlStackEntry& entry = mControlStack.top();
  return entry.isClosing();
}

uint32_t TarjanStateSpace::
getTopControlState()
  const
{
  const TarjanControlStackEntry& entry = mControlStack.top();
  if (entry.isClosing()) {
    uint32_t compIndex = entry.getClosingCompIndex();
    return mComponentStack.get(compIndex);
  } else {
    return entry.getExpandingState();
  }
}

uint32_t TarjanStateSpace::
getTopControlStateParent()
  const
{
  const TarjanControlStackEntry& entry = mControlStack.top();
  return entry.getParent();
}

void TarjanStateSpace::
pushRootControlState(uint32_t state)
{
  mControlStack.push(state, state);
}

void TarjanStateSpace::
popControlState()
{
  mControlStack.pop();
}


//############################################################################
//# TarjanStateSpace: Algorithm

uint32_t TarjanStateSpace::
beginStateExpansion()
{
  TarjanControlStackEntry& entry = mControlStack.top();
  uint32_t state = entry.getExpandingState();
  uint32_t lowLink = mComponentStack.size();
  entry.setClosingCompIndex(lowLink);
  mComponentStack.add(state);
  if (mComponentStack.size() > mMaxComponentStackHeight) {
    mMaxComponentStackHeight = mComponentStack.size();
  }
  setLowLink(state, lowLink);
  return state;
}

void TarjanStateSpace::
processTransition(uint32_t source, uint32_t target)
{
  // std::cerr << "  " << source << "->" << target << std::endl;
  uint32_t& ref = getLowLinkRef(target);
  uint32_t lowLink = ref;
  if ((lowLink & LL_EXPANDING) != 0) {
    if (lowLink == LL_OPEN) {
      mControlStack.push(target, source);
      ref = mControlStack.getPreTopIndex() | LL_EXPANDING;
    } else {
      TarjanControlStackEntry& oldTopEntry = mControlStack.top();
      uint32_t behindIndex = lowLink & ~LL_EXPANDING;
      TarjanControlStackEntry* beforeEntry =
        mControlStack.moveToTop(behindIndex, source);
      if (beforeEntry != 0) {
        ref = mControlStack.getPreTopIndex() | LL_EXPANDING;
        if (!beforeEntry->isClosing()) {
          uint32_t beforeState = beforeEntry->getExpandingState();
          setLowLink(beforeState, lowLink);
        }
        if (!oldTopEntry.isClosing()) {
          uint32_t oldTopState = oldTopEntry.getExpandingState();
          uint32_t newTopIndex = mControlStack.getTopIndex();
          setLowLink(oldTopState, newTopIndex | LL_EXPANDING);
        }
      }
    }
  } else {
    if (lowLink != LL_CLOSED) {
      adjustLowLink(source, lowLink);
    }
  }
}

void TarjanStateSpace::
endStateExpansion()
{
  mControlStack.updateMaxStackSize();
}

void TarjanStateSpace::
mayBeCloseComponent(TarjanCallBack *callBack)
{
  TarjanControlStackEntry& entry = mControlStack.top();
  uint32_t compIndex = entry.getClosingCompIndex();
  uint32_t state = mComponentStack.get(compIndex);
  uint32_t lowLink = getLowLink(state);
  if (compIndex == lowLink) {
    uint32_t end = mComponentStack.size();
    if (callBack != 0 && callBack->isCriticalComponent(compIndex, end)) {
      mCriticalComponentStart = compIndex;
    }
    if (mCriticalComponentStart == UINT32_MAX) {
      for (uint32_t pos = compIndex; pos < end; pos++) {
        uint32_t s = mComponentStack.get(pos);
        setLowLink(s, LL_CLOSED);
      }
      mComponentStack.removeLast(end - compIndex);
    }
    mNumComponents++;
    // std::cerr << "component " << mNumComponents << " "
    //           << (critical ? "CRITICAL" : "ok")
    //           << " " << callBack->getCriticalComponentSize() << std::endl;
  } else {
    uint32_t parent = entry.getParent();
    adjustLowLink(parent, lowLink);
  }
}

uint32_t TarjanStateSpace::
getCriticalComponentSize()
  const
{
  if (mCriticalComponentStart == UINT32_MAX) {
    return 0;
  } else {
    return mComponentStack.size() - mCriticalComponentStart;
  }
}


//############################################################################
//# TarjanStateSpace: Trace Search

void TarjanStateSpace::
setUpTraceSearch(uint32_t numInit)
{
  mControlStack.clear();
  uint32_t numStates = size();
  for (uint32_t s = 0; s < numStates; s++) {
    uint32_t& ref = getTraceStatusRef(s);
    ref = s < numInit ? TR_INIT : TR_OPEN;
  }
  uint32_t end = mComponentStack.size();
  for (uint32_t pos = mCriticalComponentStart; pos < end; pos++) {
    uint32_t s = mComponentStack.get(pos);
    setLowLink(s, TR_CRITICAL); // TraceStatus and LowLink are the same
  }
  mComponentStack.clear();
}

void TarjanStateSpace::
setUpLoopClosingSearch(uint32_t entryState)
{
  uint32_t numStates = size();
  for (uint32_t s = 0; s < numStates; s++) {
    uint32_t& ref = getTraceStatusRef(s);
    if (s == entryState) {
      ref = TR_CRITICAL;
    } else if (ref == TR_CRITICAL) {
      ref = TR_OPEN;
    } else {
      ref = TR_INIT;
    }
  }
}


//############################################################################
//# TarjanStateSpace: Statistics

void TarjanStateSpace::
addStatistics(const jni::NativeVerificationResultGlue& vresult)
  const
{
  StateSpace::addStatistics(vresult);
  vresult.setTarjanComponentCount(mNumComponents);
  vresult.setTarjanControlStackHeight(mControlStack.getMaxStackSize());
  vresult.setTarjanComponentStackHeight(mMaxComponentStackHeight);
}


//############################################################################
//# TarjanStateSpace: Debugging

#ifdef DEBUG

void TarjanStateSpace::
dumpControlStack()
  const
{
  mControlStack.dump(this);
}

void TarjanStateSpace::
dumpLowLink(uint32_t state)
  const
{
  uint32_t lowLink = getLowLink(state);
  if ((lowLink & LL_EXPANDING) != 0) {
    std::cerr << "EXP";
    if (lowLink != LL_OPEN) {
      std::cerr << (lowLink & ~LL_EXPANDING);
    }
  } else {
    std::cerr << "CLO";
    if (lowLink != LL_CLOSED) {
      std::cerr << lowLink;
    }
  }
}

#endif /* DEBUG */


//############################################################################
//# TarjanStateSpace: Low Link

uint32_t TarjanStateSpace::
getLowLink(uint32_t state)
  const
{
  int offset = getSignificantTupleSize();
  const uint32_t* tuple = get(state);
  return tuple[offset];
}

uint32_t& TarjanStateSpace::
getLowLinkRef(uint32_t state)
{
  int offset = getSignificantTupleSize();
  uint32_t* tuple = get(state);
  return tuple[offset];
}

void TarjanStateSpace::
setLowLink(uint32_t state, uint32_t lowLink)
{
  int offset = getSignificantTupleSize();
  uint32_t* tuple = get(state);
  tuple[offset] = lowLink;
}

void TarjanStateSpace::
adjustLowLink(uint32_t state, uint32_t lowLink)
{
  int offset = getSignificantTupleSize();
  uint32_t* tuple = get(state);
  if (lowLink < tuple[offset]) {
    tuple[offset] = lowLink;
  }
}


}  /* namespace waters */
