//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TarjanStateSpace
//###########################################################################
//# $Id$
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
//# class TarjanStateSpace
//############################################################################

//############################################################################
//# TarjanStateSpace: Constructors & Destructors

TarjanStateSpace::
TarjanStateSpace(const AutomatonEncoding* encoding, uint32_t limit)
  : StateSpace(encoding, limit, 1),
    mControlStack(2048),
    mComponentStack(1024),
    mNumComponents(0),
    mNumRedundantControlStackEntries(0),
    mMaxControlStackHeight(0),
    mMaxComponentStackHeight(0),
    mNumGarbageCollections(0)
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
  return mControlStack.size() == 0;
}

bool TarjanStateSpace::
isTopControlStateClosing()
  const
{
  uint32_t top = mControlStack.size() - 2;
  uint32_t value = mControlStack.get(top);
  return (value & TAG_CLOSING) != 0;
}

bool TarjanStateSpace::
isTopControlStateOpen()
  const
{
  uint32_t top = mControlStack.size() - 2;
  uint32_t value = mControlStack.get(top);
  if ((value & TAG_CLOSING) == 0) {
    uint32_t lowLink = getLowLink(value);
    return lowLink == LL_OPEN;
  } else {
    return false;
  }
}

uint32_t TarjanStateSpace::
getTopControlState()
  const
{
  uint32_t top = mControlStack.size() - 2;
  uint32_t value = mControlStack.get(top);
  if ((value & TAG_CLOSING) == 0) {
    return value;
  } else {
    return mComponentStack.get(value & ~TAG_CLOSING);
  }
}

uint32_t TarjanStateSpace::
getTopControlStateParent()
  const
{
  uint32_t top = mControlStack.size() - 1;
  return mControlStack.get(top);
}

void TarjanStateSpace::
pushControlState(uint32_t state, uint32_t parent)
{
  mControlStack.add(state);
  mControlStack.add(parent);
}

void TarjanStateSpace::
popControlState()
{
  mControlStack.removeLast(2);
}

void TarjanStateSpace::
popRedundantControlState()
{
  popControlState();
  mNumRedundantControlStackEntries--;
}


//############################################################################
//# TarjanStateSpace: Algorithm

uint32_t TarjanStateSpace::
beginStateExpansion()
{
  uint32_t top = mControlStack.size() - 2;
  uint32_t& ref = mControlStack.getref(top);
  uint32_t state = ref;
  uint32_t lowLink = mComponentStack.size();
  ref = lowLink | TAG_CLOSING;
  mComponentStack.add(state);
  if (mComponentStack.size() > mMaxComponentStackHeight) {
    mMaxComponentStackHeight = mComponentStack.size();
  }
  uint32_t* tuple = get(state);
  int offset = getSignificantTupleSize();
  tuple[offset] = lowLink;
  mNumStatesAtBegin = size();
  mControlStackSizeAtBegin = mControlStack.size();
  return state;
}

void TarjanStateSpace::
processTransition(uint32_t source, uint32_t target)
{
  uint32_t& lowLink = getLowLinkRef(target);
  // std::cerr << "  " << source << "->" << target << std::endl;
  if ((lowLink & TAG_GC) == 0) {
    switch (lowLink) {
    case LL_OPEN:
      pushControlState(target, source);
      lowLink |= TAG_GC;
      if (target < mNumStatesAtBegin) {
        mNumRedundantControlStackEntries++;
      }
      break;
    case LL_CLOSED:
      break; // skip
    default:
      adjustLowLink(source, lowLink);
      break;
    }
  }
}

void TarjanStateSpace::
endStateExpansion()
{
  for (uint32_t pos = mControlStackSizeAtBegin;
       pos < mControlStack.size(); pos += 2) {
    uint32_t state = mControlStack.get(pos);
    uint32_t& lowLink = getLowLinkRef(state);
    lowLink &= ~TAG_GC;
  }
  if (mControlStack.size() > mMaxComponentStackHeight) {
    mMaxControlStackHeight = mControlStack.size();
  }
  garbageCollect();
}

void TarjanStateSpace::
mayBeCloseComponent(TarjanCallBack *callBack)
{
  uint32_t top = mControlStack.size() - 2;
  uint32_t index = mControlStack.get(top) & ~TAG_CLOSING;
  uint32_t state = mComponentStack.get(index);
  uint32_t lowLink = getLowLink(state);
  if (index == lowLink) {
    uint32_t comp = LL_CLOSED;
    uint32_t end = mComponentStack.size();
    if (callBack != 0) {
      comp = TR_CRITICAL;
      for (uint32_t pos = index; pos < end; pos++) {
        uint32_t s = mComponentStack.get(pos);
        if (!callBack->addStateToComponent(s)) {
          comp = LL_CLOSED;
          break;
        }
      }
      if (comp == TR_CRITICAL) {
        callBack->setCriticalComponentSize(end - index);
      }
    }
    for (uint32_t pos = index; pos < end; pos++) {
      uint32_t s = mComponentStack.get(pos);
      setLowLink(s, comp);
    }
    mComponentStack.removeLast(end - index);
    mNumComponents++;
    // std::cerr << "component " << mNumComponents << " "
    //           << (comp == TR_CRITICAL ? "CRITICAL" : "ok")
    //           << " " << callBack->getCriticalComponentSize() << std::endl;
  } else {
    uint32_t parent = mControlStack.get(top + 1);
    adjustLowLink(parent, lowLink);
  }
}


//############################################################################
//# TarjanStateSpace: Trace Search

void TarjanStateSpace::
setUpTraceSearch(uint32_t numInit)
{
  mControlStack.clear();
  mComponentStack.clear();
  uint32_t numStates = size();
  for (uint32_t s = 0; s < numStates; s++) {
    uint32_t& ref = getTraceStatusRef(s);
    if (ref != TR_CRITICAL) {
      ref = s < numInit ? TR_INIT : TR_OPEN;
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
  vresult.setTarjanControlStackHeight(mMaxControlStackHeight >> 1);
  vresult.setTarjanComponentStackHeight(mMaxComponentStackHeight);
  vresult.setTarjanGarbageCollections(mNumGarbageCollections);
}


//############################################################################
//# TarjanStateSpace: Debugging

void TarjanStateSpace::
dumpControlStack()
{
  for (uint32_t pos = 0; pos < mControlStack.size(); pos += 2) {
    uint32_t key = mControlStack.get(pos);
    uint32_t state;
    if ((key & TAG_CLOSING) == 0) {
      std::cerr << "EXP:";
      state = key;
    } else {
      std::cerr << "CLO:";
      uint32_t comp = key & ~TAG_CLOSING;
      state = mComponentStack.get(comp);
    }
    std::cerr << state << "[";
    uint32_t lowLink = getLowLink(state);
    switch (lowLink) {
    case LL_OPEN:
      std::cerr << "OPEN";
      break;
    case LL_CLOSED:
      std::cerr << "CLOSED";
      break;
    default:
      std::cerr << lowLink;
      break;
    }
    uint32_t parent = mControlStack.get(pos + 1);
    std::cerr << "]:" << parent << std::endl;
  }
}


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


//############################################################################
//# TarjanStateSpace: Garbage Collection

void TarjanStateSpace::
garbageCollect()
{
  uint32_t stackSize = mControlStack.size();
  if (stackSize > GC_MINIMUM &&
      mNumRedundantControlStackEntries * GC_THRESHOLD >= stackSize) {
    uint32_t numStates = size();
    uint32_t start = stackSize;
    for (uint32_t pos = start; pos > 0;) {
      pos -= 2;
      uint32_t& ref = mControlStack.getref(pos);
      bool closing = (ref & TAG_CLOSING) != 0;
      uint32_t state = closing ? mComponentStack.get(ref & ~TAG_CLOSING) : ref;
      uint32_t& lowLink = getLowLinkRef(state);
      if (lowLink == LL_OPEN) {
        lowLink |= TAG_GC;
      } else if (!closing) {
        ref = numStates;
        start = pos;
      }
    }
    for (uint32_t pos = 0; pos < start; pos += 2) {
      uint32_t value = mControlStack.get(pos);
      bool closing = (value & TAG_CLOSING) != 0;
      uint32_t state = 
        closing ? mComponentStack.get(value & ~TAG_CLOSING) : value;
      uint32_t& lowLink = getLowLinkRef(state);
      lowLink &= ~TAG_GC;
    }
    uint32_t wpos = start;
    for (uint32_t rpos = start + 2; rpos < stackSize; rpos += 2) {
      uint32_t& ref = mControlStack.getref(rpos);
      bool closing = (ref & TAG_CLOSING) != 0;
      uint32_t state = closing ? mComponentStack.get(ref & ~TAG_CLOSING) : ref;
      uint32_t& lowLink = getLowLinkRef(state);
      lowLink &= ~TAG_GC;
      if (ref != numStates) {
        uint64_t& wref = (uint64_t&) mControlStack.getref(wpos);
        uint64_t& rref = (uint64_t&) ref;
        wref = rref; // copy two consecutive 32-bit words
        wpos += 2;
      }
   }
    mControlStack.removeLast(stackSize - wpos);
    mNumRedundantControlStackEntries = 0;
    mNumGarbageCollections++;
    // std::cerr << "GC #" << mNumGarbageCollections
    //           << " before: " << stackSize
    //           << " after: " << mControlStack.size() << std::endl;
  }
}


}  /* namespace waters */
