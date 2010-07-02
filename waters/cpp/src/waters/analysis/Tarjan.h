//# This may look like C code, but it really is -*- C++ -*-
//############################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   Tarjan
//############################################################################
//# $Id$
//############################################################################


#ifndef _Tarjan_h_
#define _Tarjan_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/BlockedArrayList.h"
#include "waters/base/IntTypes.h"


namespace waters {

class NondeterministicTransitionIterator;
class TransitionRecord;


//############################################################################
//# class TarjanStackFrame
//############################################################################

class TarjanStackFrame
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanStackFrame();
  ~TarjanStackFrame();

  //##########################################################################
  //# Simple Access
  inline bool isRoot() const {return mIsRoot;}
  inline uint32 getStateCode() const {return mStateCode;}
  inline uint32 getEventCode() const {return mEventCode;}
  inline bool hasNondeterministicTransitionIterators() const
    {return mNondeterministicTransitionIteratorEnd != 0;}
  inline void setRoot(bool root) {mIsRoot = root;}
  inline void setStateCode(uint32 state) {mStateCode = state;}
  inline void setEventCode(uint32 event) {mEventCode = event;}

  //##########################################################################
  //# Advanced Access
  void reset(uint32 state);
  void createNondeterministicTransitionIterators(int max);
  uint32 setupNondeterministicTransitionIterator(const TransitionRecord* trans,
						 uint32 source);
  bool advanceNondeterministicTransitionIterators(uint32* bufferpacked);

private:
  //##########################################################################
  //# Data Members
  bool mIsRoot;
  uint32 mStateCode;
  uint32 mEventCode;
  NondeterministicTransitionIterator* mNondeterministicTransitionIterators;
  int mNondeterministicTransitionIteratorEnd;
};


//############################################################################
//# class TarjanControlStack
//############################################################################

class TarjanControlStack : private BlockedArrayList<TarjanStackFrame>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanControlStack() : mStackPointer(0) {};
  ~TarjanControlStack() {};

  //##########################################################################
  //# Access
  bool isEmpty() const {return mStackPointer == 0;}
  TarjanStackFrame& top() const {return getref(mStackPointer);}
  TarjanStackFrame& push(uint32 state);
  void pop() {mStackPointer--;}
  void clear();

private:
  //##########################################################################
  //# Data Members
  uint32 mStackPointer;
};


//############################################################################
//# class TarjanStateStack
//############################################################################

class TarjanStateStack : private BlockedArrayList<uint32>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanStateStack() : mStackPointer(0) {};
  ~TarjanStateStack() {};

  //##########################################################################
  //# Access
  bool isEmpty() const {return mStackPointer == 0;}
  uint32 top() const {return get(mStackPointer);}
  uint32 pop() {return get(mStackPointer--);}
  void push(uint32 state);
  void clear();

private:
  //##########################################################################
  //# Data Members
  uint32 mStackPointer;
};


}   /* namespace waters */

#endif  /* !_Tarjan_h_ */
