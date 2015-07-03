//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TarjanStateSpace
//###########################################################################
//# $Id$
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
  bool isTopControlStateOpen() const;
  uint32_t getTopControlState() const;
  uint32_t getTopControlStateParent() const;
  inline void pushControlState(uint32_t state) {pushControlState(state, state);}
  void pushControlState(uint32_t state, uint32_t parent);
  void popControlState();
  void popRedundantControlState();

  //##########################################################################
  //# Algorithm
  uint32_t beginStateExpansion();
  void processTransition(uint32_t source, uint32_t target);
  void endStateExpansion();
  bool mayBeCloseComponent(TarjanCallBack *callBack);

  //##########################################################################
  //# Trace Search
  void setUpTraceSearch(uint32_t numInit);
  uint32_t getTraceStatus(uint32_t state) const {return getLowLink(state);}
  uint32_t& getTraceStatusRef(uint32_t state) {return getLowLinkRef(state);}

  //##########################################################################
  //# Debugging
  void dumpControlStack();

private:
  //##########################################################################
  //# Low Link
  uint32_t getLowLink(uint32_t state) const;
  uint32_t& getLowLinkRef(uint32_t state);
  void setLowLink(uint32_t state, uint32_t lowLink);
  void adjustLowLink(uint32_t state, uint32_t lowLink);

  //##########################################################################
  //# Garbage Collection
  void garbageCollect();

  //##########################################################################
  //# Data Members
  uint32_t mNumComponents;
  BlockedArrayList<uint32_t> mControlStack;
  BlockedArrayList<uint32_t> mComponentStack;
  uint32_t mNumStatesAtBegin;
  uint32_t mControlStackSizeAtBegin;
  uint32_t mNumRedundantControlStackEntries;

  //##########################################################################
  //# Class Constants
private:
  static const uint32_t GC_MINIMUM = 1024;
  static const uint32_t GC_THRESHOLD = 4;
  static const uint32_t TAG_GC = 0x80000000;
  static const uint32_t TAG_CLOSING = 0x80000000;
  static const uint32_t LL_OPEN = 0x7ffffffe;
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
  explicit TarjanCallBack(TarjanStateSpace* tarjan) :
    mTarjan(*tarjan), mCriticalComponentSize(0) {}
  virtual ~TarjanCallBack() {}

  //##########################################################################
  //# Simple Access
  inline TarjanStateSpace& getTarjan() const {return mTarjan;}
  inline uint32_t getCriticalComponentSize() const
    {return mCriticalComponentSize;}
  inline void setCriticalComponentSize(uint32_t size)
    {mCriticalComponentSize = size;}

  //##########################################################################
  //# Interface TarjanCallBack
  virtual bool addStateToComponent(uint32_t state) = 0;

private:
  //##########################################################################
  //# Data Members
  TarjanStateSpace& mTarjan;
  uint32_t mCriticalComponentSize;
};

}   /* namespace waters */

#endif  /* !_TarjanStateSpace_h_ */
