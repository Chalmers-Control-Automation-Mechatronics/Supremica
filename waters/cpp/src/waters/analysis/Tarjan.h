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
#include <stdint.h>


namespace waters {

class NondeterministicTransitionIterator;
class TransitionRecord;


//############################################################################
//# class TarjanStackFrameNondeterministic
//############################################################################

class TarjanStackFrameNondeterministic
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanStackFrameNondeterministic(int size);
  ~TarjanStackFrameNondeterministic();

  //##########################################################################
  //# Simple Access
  inline uint32_t getFirstStateCode() const {return mFirstStateCode;}
  inline void setFirstStateCode(uint32_t state) {mFirstStateCode = state;}

  //##########################################################################
  //# Iteration
  inline void reset() {mTransitionIteratorEnd = 0;}
  inline bool hasTransitionIterators() const
    {return mTransitionIteratorEnd != 0;}
  uint32_t setupTransitionIterator(const TransitionRecord* trans, uint32_t source);
  bool advanceTransitionIterators(uint32_t* bufferpacked);

private:
  //##########################################################################
  //# Data Members
  uint32_t mFirstStateCode;
  NondeterministicTransitionIterator* mTransitionIterators;
  int mTransitionIteratorEnd;
};


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
  inline uint32_t getStateCode() const {return mStateCode;}
  inline uint32_t getEventCode() const {return mEventCode;}
  inline bool hasNondeterministicTransitionIterators() const
    {return mNondeterministicInfo->hasTransitionIterators();}
  inline uint32_t getFirstNondeterministicSuccessor() const
    {return mNondeterministicInfo->getFirstStateCode();}
  inline void setRoot(bool root) {mIsRoot = root;}
  inline void setStateCode(uint32_t state) {mStateCode = state;}
  inline void setEventCode(uint32_t event) {mEventCode = event;}
  inline void setFirstNondeterministicSuccessor(uint32_t state)
    {mNondeterministicInfo->setFirstStateCode(state);}

  //##########################################################################
  //# Advanced Access
  void reset(uint32_t state);
  void createNondeterministicTransitionIterators(int max);
  inline uint32_t setupNondeterministicTransitionIterator
    (const TransitionRecord* trans, uint32_t source)
    {return mNondeterministicInfo->setupTransitionIterator(trans, source);}
  inline bool advanceNondeterministicTransitionIterators(uint32_t* bufferpacked)
    {return mNondeterministicInfo->advanceTransitionIterators(bufferpacked);}

private:
  //##########################################################################
  //# Data Members
  bool mIsRoot;
  uint32_t mStateCode;
  uint32_t mEventCode;
  TarjanStackFrameNondeterministic* mNondeterministicInfo;
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
  TarjanStackFrame& push(uint32_t state);
  void pop() {mStackPointer--;}
  void clear();

private:
  //##########################################################################
  //# Data Members
  uint32_t mStackPointer;
};


//############################################################################
//# class TarjanStateStack
//############################################################################

class TarjanStateStack : private BlockedArrayList<uint32_t>
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TarjanStateStack() : mStackPointer(0) {};
  ~TarjanStateStack() {};

  //##########################################################################
  //# Access
  bool isEmpty() const {return mStackPointer == 0;}
  uint32_t top() const {return get(mStackPointer);}
  uint32_t pop() {return get(mStackPointer--);}
  void push(uint32_t state);
  void clear();

private:
  //##########################################################################
  //# Data Members
  uint32_t mStackPointer;
};


}   /* namespace waters */

#endif  /* !_Tarjan_h_ */
