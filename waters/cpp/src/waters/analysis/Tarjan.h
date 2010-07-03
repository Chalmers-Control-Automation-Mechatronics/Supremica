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
  inline uint32 getFirstStateCode() const {return mFirstStateCode;}
  inline void setFirstStateCode(uint32 state) {mFirstStateCode = state;}

  //##########################################################################
  //# Iteration
  inline void reset() {mTransitionIteratorEnd = 0;}
  inline bool hasTransitionIterators() const
    {return mTransitionIteratorEnd != 0;}
  uint32 setupTransitionIterator(const TransitionRecord* trans, uint32 source);
  bool advanceTransitionIterators(uint32* bufferpacked);

private:
  //##########################################################################
  //# Data Members
  uint32 mFirstStateCode;
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
  inline uint32 getStateCode() const {return mStateCode;}
  inline uint32 getEventCode() const {return mEventCode;}
  inline bool hasNondeterministicTransitionIterators() const
    {return mNondeterministicInfo->hasTransitionIterators();}
  inline uint32 getFirstNondeterministicSuccessor() const
    {return mNondeterministicInfo->getFirstStateCode();}
  inline void setRoot(bool root) {mIsRoot = root;}
  inline void setStateCode(uint32 state) {mStateCode = state;}
  inline void setEventCode(uint32 event) {mEventCode = event;}
  inline void setFirstNondeterministicSuccessor(uint32 state)
    {mNondeterministicInfo->setFirstStateCode(state);}

  //##########################################################################
  //# Advanced Access
  void reset(uint32 state);
  void createNondeterministicTransitionIterators(int max);
  inline uint32 setupNondeterministicTransitionIterator
    (const TransitionRecord* trans, uint32 source)
    {return mNondeterministicInfo->setupTransitionIterator(trans, source);}
  inline bool advanceNondeterministicTransitionIterators(uint32* bufferpacked)
    {return mNondeterministicInfo->advanceTransitionIterators(bufferpacked);}

private:
  //##########################################################################
  //# Data Members
  bool mIsRoot;
  uint32 mStateCode;
  uint32 mEventCode;
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
