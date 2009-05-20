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
#include "jni/glue/SafetyTraceGlue.h"
#include "waters/base/HashTable.h"
#include "waters/base/IntTypes.h"
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
  explicit BroadProductExplorer(const jni::ProductDESGlue& des,
				const jni::KindTranslatorGlue& translator,
				const jni::EventGlue& marking,
				jni::ClassCache* cache);
  virtual ~BroadProductExplorer();

protected:
  //##########################################################################
  //# Shared Auxiliary Methods
  virtual void setup(bool safety);
  virtual void teardown();
  virtual bool expandSafetyState
    (const uint32* sourcetuple, const uint32* sourcepacked);
  virtual bool expandNonblockingReachabilityState
    (uint32 source, const uint32* sourcetuple, const uint32* sourcepacked);
  virtual void expandNonblockingCoreachabilityState
    (const uint32* targettuple, const uint32* targetpacked,
     uint32 stackpos, int ndindex);
  virtual const jni::EventGlue& getTraceEvent();
  virtual void setupReverseTransitionRelations();
  virtual void expandTraceState
    (const uint32* targettuple,	const uint32* targetpacked);

  virtual int getMinimumNondeterministicTransitionIterators() const;
  virtual int allocateNondeterministicTransitionIterators(int factor = 1);

private:
  //##########################################################################
  //# Private Auxiliary Methods
  void setupSafety();
  void setupNonblocking();
  void setupEventMap
    (HashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void setupTransitions
    (AutomatonRecord* aut,
     const jni::AutomatonGlue& autglue,
     const HashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);
  void setupCompactEventList
    (bool safety,
     const HashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap);

  //##########################################################################
  //# Data Members
  int mNumEventRecords;
  BroadEventRecord** mEventRecords;
  BroadEventRecord** mReversedEventRecords;
  int mMaxUpdates;
  int mNumNondeterministicTransitionsIterators;
  NondeterministicTransitionIterator* mNondeterministicTransitionIterators;
  const BroadEventRecord* mTraceEvent;
  uint32 mTraceLimit;
};

}   /* namespace waters */

#endif  /* !_BroadProductExplorer_h_ */
