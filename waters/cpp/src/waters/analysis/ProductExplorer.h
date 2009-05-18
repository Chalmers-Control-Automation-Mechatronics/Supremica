//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ProductExplorer
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _ProductExplorer_h_
#define _ProductExplorer_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <ctime>

#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/SafetyTraceGlue.h"
#include "waters/base/ArrayList.h"
#include "waters/base/IntTypes.h"
#include "waters/analysis/AutomatonEncoding.h"


namespace jni {
  class ClassCache;
  class ListGlue;
  class ProductDESGlue;
  class VerificationResultGlue;
}


namespace waters {

class StateSpace;


//############################################################################
//# exception SearchAbort
//############################################################################

class SearchAbort {
public:
  //##########################################################################
  //# Constructors & Destructors
  SearchAbort() {}
  ~SearchAbort() {}
};


//############################################################################
//# class BroadProductExplorer
//############################################################################

class ProductExplorer
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ProductExplorer(jni::ProductDESGlue des,
			   jni::KindTranslatorGlue translator,
			   jni::ClassCache* cache);
  virtual ~ProductExplorer();

public:
  //##########################################################################
  //# Invocation
  virtual bool runSafetyCheck();
  virtual jni::SafetyTraceGlue getSafetyCounterExample
    (const jni::ProductDESProxyFactoryGlue& factory) const;
  virtual void addStatistics(const jni::VerificationResultGlue& vresult) const;

  //##########################################################################
  //# Parameters
  void setStateLimit(uint32 limit) {mStateLimit = limit;}

protected:
  //##########################################################################
  //# Auxiliary Methods
  virtual void setup();
  virtual void teardown();
  virtual bool doSafetySearch();
  virtual void computeCounterExample(const jni::ListGlue& list);
  virtual void storeInitialStates(bool initzero);
  virtual bool expandState(const uint32* currenttuple,
			   const uint32* currentpacked) = 0;
  virtual const jni::EventGlue& getTraceEvent() = 0;
  virtual void setupReverseTransitionRelations() = 0;
  virtual void expandTraceState(const uint32* targettuple,
				const uint32* targetpacked) = 0;
  
  bool checkTraceState();

  //##########################################################################
  //# Simple Access
  inline jni::ClassCache* getCache() {return mCache;}
  inline jni::ProductDESGlue& getModel() {return mModel;}
  inline jni::KindTranslatorGlue& getKindTranslator() {return mKindTranslator;}
  inline AutomatonEncoding& getAutomatonEncoding() {return *mEncoding;}
  inline StateSpace& getStateSpace() {return *mStateSpace;}
  inline void setTrivial() {mIsTrivial = true;}
  inline int getNumberOfAutomata() const {return mNumAutomata;}
  inline uint32 getNumberOfStates() const {return mNumStates;}
  inline uint32 incNumberOfStates() {return mNumStates++;}

private:
  //##########################################################################
  //# Data Members
  jni::ClassCache* mCache;
  jni::ProductDESGlue mModel;
  jni::KindTranslatorGlue mKindTranslator;
  uint32 mStateLimit;
  AutomatonEncoding* mEncoding;
  StateSpace* mStateSpace;
  ArrayList<uint32>* mDepthMap;
  bool mIsTrivial;
  int mNumAutomata;
  uint32 mNumStates;
  jni::ListGlue* mTraceList;
  uint32 mTraceState;
  uint32 mTraceLimit;
  clock_t mStartTime;
  clock_t mTraceStartTime;
  clock_t mStopTime;
};

}   /* namespace waters */

#endif  /* !_ProductExplorer_h_ */
