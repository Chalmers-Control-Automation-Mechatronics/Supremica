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
#include "jni/glue/EventGlue.h"
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
  mAutomaton->setInitialStates(0, nextinit); 
}


AutomatonStateMap::
AutomatonStateMap(jni::ClassCache* cache,
                  AutomatonRecord* aut,
                  const jni::EventGlue& marking)
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
  int cat;
  uint32 catindex[CAT_COUNT];
  for (cat = 0; cat < CAT_COUNT; cat++) {
    catindex[cat] = 0;
  }
  const jni::IteratorGlue iter1 = states.iteratorGlue(cache);
  while (iter1.hasNext()) {
    jobject javaobject = iter1.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, marking, cache);
    catindex[cat++];
  }
  uint32 start = 0;
  for (cat = 0; cat < CAT_COUNT; cat++) {
    uint32 next = start + catindex[cat];
    catindex[cat] = start;
    start = next;
  }
  const jni::IteratorGlue iter2 = states.iteratorGlue(cache);
  while (iter2.hasNext()) {
    jobject javaobject = iter2.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, marking, cache);
    const uint32 code = catindex[cat]++;
    new (&mStateArray[code]) StateRecord(state, code, cache);
    mStateMap->add(&mStateArray[code]);
  }
  mAutomaton->setInitialStates(catindex[0], catindex[2]);
  mAutomaton->setMarkedStates(catindex[1]); 
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


//############################################################################
//# AutomatonStateMap: Auxiliary Methods

int AutomatonStateMap::
getCategory(const jni::StateGlue& state,
            const jni::EventGlue& marking,
            jni::ClassCache* cache)
{
  int init = state.isInitial() ? 0 : 1;
  jni::CollectionGlue props = state.getPropositionsGlue(cache);
  int marked = props.contains(&marking) ? 0 : 3;
  return init ^ marked;
}


}  /* namespace waters */
