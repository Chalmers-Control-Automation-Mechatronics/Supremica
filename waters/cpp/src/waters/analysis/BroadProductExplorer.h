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

protected:
  //##########################################################################
  //# Shared Auxiliary Methods
  virtual void setup();
  virtual void teardown();
  virtual bool expandSafetyState
    (const uint32_t* sourcetuple, const uint32_t* sourcepacked);
  virtual bool expandNonblockingReachabilityState
    (uint32_t source, const uint32_t* sourcetuple, const uint32_t* sourcepacked);
  virtual void expandNonblockingCoreachabilityState
    (const uint32_t* targettuple, const uint32_t* targetpacked);
  virtual void setupReverseTransitionRelations();
  virtual void expandTraceState
    (const uint32_t* targettuple, const uint32_t* targetpacked);
  virtual const EventRecord* findEvent
    (const uint32_t* sourcetuple, const uint32_t* sourcepacked,
     const uint32_t* targetpacked);
  virtual void storeNondeterministicTargets
    (const uint32_t* sourcetuple, const uint32_t* targettuple,
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
