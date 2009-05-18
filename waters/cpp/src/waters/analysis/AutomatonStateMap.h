//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonStateMap
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _AutomatonStateMap_h_
#define _AutomatonStateMap_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/StateGlue.h"

#include "waters/analysis/StateRecord.h"
#include "waters/base/HashTable.h"
#include "waters/base/IntTypes.h"


namespace jni {
  class ClassCache;
  class EventGlue;
}


namespace waters {

class AutomatonRecord;


//############################################################################
//# class AutomatonStateMap
//############################################################################

class AutomatonStateMap {
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit AutomatonStateMap(jni::ClassCache* cache, AutomatonRecord* aut);
  explicit AutomatonStateMap(jni::ClassCache* cache,
			     AutomatonRecord* aut,
			     const jni::EventGlue& marking);
  ~AutomatonStateMap();

  //##########################################################################
  //# Simple Access
  inline AutomatonRecord* getAutomaton() const {return mAutomaton;}
  inline StateRecord* getState(uint32 code) const {return &mStateArray[code];}
  inline const jni::StateGlue& getJavaState(uint32 code) const
    {return mStateArray[code].getJavaState();}
  inline StateRecord* getState(const jni::StateGlue& state) const
    {return mStateMap->get(&state);}
  inline uint32 getStateCode(const jni::StateGlue& state) const
    {return getState(state)->getStateCode();}

private:
  //##########################################################################
  //# Auxiliary Methods
  static int getCategory(const jni::StateGlue& state,
			 const jni::EventGlue& marking,
			 jni::ClassCache* cache);

  //##########################################################################
  //# Data Members
  AutomatonRecord* mAutomaton;
  StateRecord* mStateArray;
  HashTable<const jni::StateGlue*,StateRecord*>* mStateMap;

  //##########################################################################
  //# Class Constants
  static const int CAT_NORMAL = 0;
  static const int CAT_INIT = 1;
  static const int CAT_MARKED = 2;
  static const int CAT_INIT_MARKED = 3;
  static const int CAT_COUNT = 4;
};


}   /* namespace waters */

#endif  /* !_AutomatonStateMap_h_ */
