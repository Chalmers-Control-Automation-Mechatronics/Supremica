//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadProductExplorer
//###########################################################################
//# $Id$
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
			   const BroadEventRecord* event);
  virtual bool handleState(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   const BroadEventRecord* event);
  inline bool handleTransition(uint32_t source,
			       const BroadEventRecord* event,
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
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, BroadExpandHandler& handler);
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardSafety
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardAgain
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
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
  void setupSafety();
  void setupNonblocking();
  void setupEventMap
    (PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void setupTransitions
    (AutomatonRecord* aut,
     const jni::AutomatonGlue& autglue,
     const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void setupCompactEventList
    (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);

  //##########################################################################
  //# Data Members
  int mNumEventRecords;
  ArrayList<BroadEventRecord*> mEventRecords;
  ArrayList<BroadEventRecord*> mReversedEventRecords;
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
