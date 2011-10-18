//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowProductExplorer
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _NarrowProductExplorer_h_
#define _NarrowProductExplorer_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESGlue.h"

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

class NarrowEventRecord;
class NarrowTransitionTable;
class NondeterministicTransitionIterator;


//############################################################################
//# class NarrowProductExplorer
//############################################################################

class NarrowProductExplorer : public ProductExplorer
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowProductExplorer
    (const jni::ProductDESProxyFactoryGlue& factory,
     const jni::ProductDESGlue& des,
     const jni::KindTranslatorGlue& translator,
     const jni::EventGlue& premarking,
     const jni::EventGlue& marking,
     jni::ClassCache* cache);
  virtual ~NarrowProductExplorer();

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
    (const uint32_t* targettuple,	const uint32_t* targetpacked);
  virtual void storeNondeterministicTargets
    (const uint32_t* sourcetuple, const uint32_t* targettuple,
     const jni::MapGlue& statemap);

private:
  //##########################################################################
  //# Private Auxiliary Methods
  void addSuccessorStates(const uint32_t* sourcetuple, uint32_t autcount);

  //##########################################################################
  //# Data Members
  uint32_t mNumEventRecords;
  uint32_t mFirstSpecOnlyUncontrollable;
  uint32_t mNumPlants;
  NarrowEventRecord** mEventRecords;
  NarrowTransitionTable* mTransitionTables;
  NarrowTransitionTable* mNonReversedTransitionTables;
  uint32_t* mIterator;
  uint32_t* mNondetIterator;
  uint32_t* mCurrentAutomata;
  uint32_t* mTargetTuple;
};

}   /* namespace waters */

#endif  /* !_NarrowProductExplorer_h_ */
