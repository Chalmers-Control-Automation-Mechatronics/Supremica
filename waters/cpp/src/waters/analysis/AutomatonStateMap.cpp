//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonStateMap
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/SetGlue.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/AutomatonStateMap.h"
#include "waters/analysis/StateRecord.h"


namespace waters {


//############################################################################
//# class AutomatonStateMap
//############################################################################

//############################################################################
//# AutomatonStateMap: Constructors & Destructors

AutomatonStateMap::
AutomatonStateMap(jni::ClassCache* cache, AutomatonRecord* aut)
  : mAutomaton(aut),
    mStateArray(0),
    mStateMap(0)
{
  const HashAccessor* accessor = StateRecord::getHashAccessor();
  const uint32 numstates =  mAutomaton->getNumberOfStates();
  mStateArray = (StateRecord*) new char[numstates * sizeof(StateRecord)];
  mStateMap =
    new HashTable<const jni::StateGlue*,StateRecord*>(accessor, numstates);
  const jni::AutomatonGlue& autglue = mAutomaton->getJavaAutomaton();
  const jni::SetGlue states = autglue.getStatesGlue(cache);
  const jni::IteratorGlue iter = states.iteratorGlue(cache);
  uint32 nextinit = 0;
  uint32 nextnoninit = numstates - 1;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::StateGlue state(javaobject, cache);
    uint32 code;
    if (state.isInitial()) {
      code = nextinit++;
    } else {
      code = nextnoninit--;
    }
    new (&mStateArray[code]) StateRecord(state, code, cache);
    mStateMap->add(&mStateArray[code]);
  }
  mAutomaton->setNumberOfInitialStates(nextinit); 
}


AutomatonStateMap::
~AutomatonStateMap()
{
  if (mStateArray != 0) {
    const uint32 numstates = mAutomaton->getNumberOfStates();
    for (uint32 code = 0; code < numstates; code++) {
      mStateArray[code].~StateRecord();
    }
    delete [] (char*) mStateArray;
    delete mStateMap;
  }
}


}  /* namespace waters */
