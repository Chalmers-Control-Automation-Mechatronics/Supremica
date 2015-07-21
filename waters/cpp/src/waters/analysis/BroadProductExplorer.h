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

#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "waters/base/HashTable.h"
#include <stdint.h>
#include "waters/analysis/ProductExplorer.h"


namespace jni {
  class AutomatonGlue;
  class ClassCache;
  class EventGlue;
  class ListGlue;
  class ProductDESGlue;
  class VerificationResultGlue;
}


namespace waters {

class BroadEventRecord;
class NondeterministicTransitionIterator;


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
  //# Shared Auxiliary Methods
  virtual void setup();
  virtual void teardown();

  //##########################################################################
  //# State Expansion Procedures
  virtual bool isLocalDumpState(const uint32_t* tuple) const;
  virtual void setupReverseTransitionRelations();
  virtual bool expandSafetyState
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandForwardAgain
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandReverse
    (uint32_t target, const uint32_t* targetTuple,
     const uint32_t* targetPacked, TransitionCallBack callBack = 0);
  virtual void expandTraceState
    (uint32_t target, const uint32_t* targetTuple,
     const uint32_t* targetPacked, uint32_t level);
  virtual const EventRecord* findEvent
    (const uint32_t* sourceTuple, const uint32_t* sourcePacked,
     const uint32_t* targetPacked);
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
  BroadEventRecord** mEventRecords;
  int mNumReversedEventRecords;
  BroadEventRecord** mReversedEventRecords;
  int mMaxNondeterministicUpdates;
  NondeterministicTransitionIterator* mNondeterministicTransitionIterators;
  // List of pairs (automaton number, state number), terminated by UINT32_MAX;
  // or NULL.
  uint32_t* mDumpStates;
  uint32_t mTraceLimit;
};

}   /* namespace waters */

#endif  /* !_BroadProductExplorer_h_ */
