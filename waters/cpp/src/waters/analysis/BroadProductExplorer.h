//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

#ifndef _BroadProductExplorer_h_
#define _BroadProductExplorer_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/ArrayList.h"
#include "waters/base/HashTable.h"
#include "waters/analysis/ProductExplorer.h"


namespace jni {
  class AutomatonGlue;
}


namespace waters {

class BroadEventRecord;
class BroadProductExplorer;
class NondeterministicTransitionIterator;


//############################################################################
//# class BroadExpandHandler
//############################################################################

class BroadExpandHandler
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BroadExpandHandler(BroadProductExplorer& explorer,
			      TransitionCallBack callBack = 0);
  virtual ~BroadExpandHandler() {}

  //##########################################################################
  //# Callbacks
  virtual bool handleEvent(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   BroadEventRecord* event);
  virtual bool handleState(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   BroadEventRecord* event);
  inline bool handleTransition(uint32_t source,
			       BroadEventRecord* event,
			       uint32_t target);

protected:
  //##########################################################################
  //# Simple Access
  BroadProductExplorer& getExplorer() const {return mExplorer;}

private:
  //##########################################################################
  //# Data Members
  BroadProductExplorer& mExplorer;
  TransitionCallBack mTransitionCallBack;
  int mNumberOfWords;
  uint32_t* mNonDetBufferPacked;
};


//############################################################################
//# class BroadProductExplorer
//############################################################################

class BroadProductExplorer : public ProductExplorer
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BroadProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
				const jni::ProductDESGlue& des,
				const jni::KindTranslatorGlue& translator,
				const jni::EventGlue& premarking,
				const jni::EventGlue& marking,
				jni::ClassCache* cache);
  virtual ~BroadProductExplorer();

  //##########################################################################
  //# Overrides for ProductExplorer
  virtual void addStatistics
    (const jni::NativeVerificationResultGlue& vresult) const;

protected:
  //##########################################################################
  //# Simple Access
  inline ArrayList<BroadEventRecord*>& getForwardEventRecords()
    {return mEventRecords;}
  inline ArrayList<BroadEventRecord*>& getBackwardEventRecords()
    {return mReversedEventRecords;}

  //##########################################################################
  //# Shared Auxiliary Methods
  virtual void setup();
  virtual void teardown();

  //##########################################################################
  //# State Expansion Procedures
  virtual bool isLocalDumpState(const uint32_t* tuple) const;
  virtual void setupReverseTransitionRelations();
  virtual void removeUncontrollableEvents();
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, BroadExpandHandler& handler);
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardSafety
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardDeadlock
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardAgain
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardAgainIncludingSelfloops
    (uint32_t source, uint32_t* sourceTuple,
     TransitionCallBack callBack = 0);
  virtual bool expandReverse
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, BroadExpandHandler& handler);
  virtual bool expandReverse
    (uint32_t target, const uint32_t* targetTuple,
     const uint32_t* targetPacked, TransitionCallBack callBack = 0);
  virtual void expandTraceState
    (uint32_t target, const uint32_t* targetTuple,
     const uint32_t* targetPacked, uint32_t level);
  virtual const AutomatonRecord* findDisablingAutomaton
    (const uint32_t* sourceTuple, const BroadEventRecord* event) const;
  virtual const EventRecord* findEvent
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, const uint32_t* targetPacked);
  virtual void storeNondeterministicTargets
    (const uint32_t* sourceTuple, const uint32_t* targetTuple,
     const jni::MapGlue& map);

private:
  //##########################################################################
  //# Private Auxiliary Methods
  void setupSafety
    (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventMap);
  uint32_t setupNonblocking
    (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventMap);
  void setupLoop
    (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventMap);
  void setupEventMap
    (PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventMap);
  void setupTransitions
    (AutomatonRecord* aut,
     const jni::AutomatonGlue& autglue,
     const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void checkForUnusedEvent
    (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void setupCompactEventList
    (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void setupDumpStates(uint32_t numDump);

  //##########################################################################
  //# Data Members
  ArrayList<BroadEventRecord*> mEventRecords;
  ArrayList<BroadEventRecord*> mReversedEventRecords;
  uint32_t mTotalNumberOfEvents;
  int mMaxNondeterministicUpdates;
  NondeterministicTransitionIterator* mNondeterministicTransitionIterators;
  // List of pairs (automaton number, state number), terminated by UINT32_MAX;
  // or NULL.
  uint32_t* mDumpStates;
  uint32_t mTraceLimit;

  //##########################################################################
  //# Friends
  friend class BroadExpandHandler;
};

}   /* namespace waters */

#endif  /* !_BroadProductExplorer_h_ */
