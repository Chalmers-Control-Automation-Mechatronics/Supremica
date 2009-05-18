//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   StateRecord
//###########################################################################
//# $Id: StateRecord.cpp,v 1.2 2006-09-03 06:38:42 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/JavaString.h"

#include "waters/analysis/StateRecord.h"


namespace waters {

//############################################################################
//# class StateRecordHashAccessor
//############################################################################

//############################################################################
//# StateRecordHashAccessor: Hash Methods

uint32 StateRecordHashAccessor::
hash(const void* key)
  const
{
  const jni::StateGlue* state = (const jni::StateGlue*) key;
  return (uint32) state->hashCode();
}


bool StateRecordHashAccessor::
equals(const void* key1, const void* key2)
  const
{
  const jni::StateGlue* state1 = (const jni::StateGlue*) key1;
  const jni::StateGlue* state2 = (const jni::StateGlue*) key2;
  return state1->equals(state2);
}


const void* StateRecordHashAccessor::
getKey(const void* value)
  const
{
  const StateRecord* record = (const StateRecord*) value;
  return &record->getJavaState();
}


//############################################################################
//# class StateRecord
//############################################################################

//############################################################################
//# StateRecord: Class Variables

const StateRecordHashAccessor StateRecord::theHashAccessor;


//############################################################################
//# StateRecord: Constructors & Destructors

StateRecord::
StateRecord(const jni::StateGlue& state,
            uint32 code,
            jni::ClassCache* /* cache */)
  : mJavaState(state),
    mStateCode(code)
{
}


//############################################################################
//# StateRecord: Simple Access

jni::JavaString StateRecord::
getName()
  const
{
  const jni::ClassGlue* cls = mJavaState.getClass();
  JNIEnv* env = cls->getEnvironment();
  jstring jname = mJavaState.getName();
  return jni::JavaString(env, jname);
}


//############################################################################
//# StateRecord: Comparing

int StateRecord::
compareTo(const StateRecord* partner)
  const
{
  return mJavaState.compareTo(&partner->mJavaState);
}

int StateRecord::
compare(const void* elem1, const void* elem2)
{
  const StateRecord* val1 = *((const StateRecord**) elem1);
  const StateRecord* val2 = *((const StateRecord**) elem2);
  return val1->compareTo(val2);
}


}  /* namespace waters */
