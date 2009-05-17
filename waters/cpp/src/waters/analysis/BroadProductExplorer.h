//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadProductExplorer
//###########################################################################
//# $Id: BroadProductExplorer.h,v 1.5 2007-04-18 03:45:53 robi Exp $
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
#include "waters/base/IntTypes.h"
#include "waters/analysis/ProductExplorer.h"


namespace jni {
  class ClassCache;
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
  explicit BroadProductExplorer(jni::ProductDESGlue des,
				jni::KindTranslatorGlue translator,
				jni::ClassCache* cache);
  virtual ~BroadProductExplorer();

protected:
  //##########################################################################
  //# Auxiliary Methods
  virtual void setup();
  virtual void teardown();
  virtual void storeInitialStates();
  virtual bool expandState(const uint32* currenttuple,
			   const uint32* currentpacked);
  virtual const jni::EventGlue& getTraceEvent();
  virtual void setupReverseTransitionRelations();
  virtual void expandTraceState(const uint32* targettuple,
				const uint32* targetpacked);

private:
  //##########################################################################
  //# Data Members
  int mNumEventRecords;
  BroadEventRecord** mEventRecords;
  BroadEventRecord** mReversedEventRecords;
  int mNumNondetInitialStates;
  NondeterministicTransitionIterator* mNondeterministicTransitionIterators;
  const BroadEventRecord* mTraceEvent;
  uint32 mTraceLimit;
};

}   /* namespace waters */

#endif  /* !_BroadProductExplorer_h_ */
