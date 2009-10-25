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

#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESGlue.h"
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
     const jni::EventGlue& marking,
     jni::ClassCache* cache);
  virtual ~NarrowProductExplorer();

protected:
  //##########################################################################
  //# Shared Auxiliary Methods
  virtual void setup();
  virtual void teardown();
  virtual bool expandSafetyState
    (const uint32* sourcetuple, const uint32* sourcepacked);
  virtual bool expandNonblockingReachabilityState
    (uint32 source, const uint32* sourcetuple, const uint32* sourcepacked);
  virtual void expandNonblockingCoreachabilityState
    (const uint32* targettuple, const uint32* targetpacked);
  virtual void setupReverseTransitionRelations();
  virtual void expandTraceState
    (const uint32* targettuple,	const uint32* targetpacked);
  virtual void storeNondeterministicTargets
    (const uint32* sourcetuple, const uint32* targettuple,
     const jni::MapGlue& statemap);

private:
  //##########################################################################
  //# Private Auxiliary Methods
  void addSuccessorStates(const uint32* sourcetuple, uint32 autcount);

  //##########################################################################
  //# Data Members
  uint32 mNumEventRecords;
  uint32 mFirstSpecOnlyUncontrollable;
  uint32 mNumPlants;
  NarrowEventRecord** mEventRecords;
  NarrowTransitionTable* mTransitionTables;
  uint32* mIterator;
  uint32* mNondetIterator;
  uint32* mCurrentAutomata;
  uint32* mTargetTuple;
};

}   /* namespace waters */

#endif  /* !_NarrowProductExplorer_h_ */
