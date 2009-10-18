//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowEventRecord
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "waters/analysis/NarrowEventRecord.h"


namespace waters {

//############################################################################
//# class NarrowEventRecord
//############################################################################

//############################################################################
//# NarrowEventRecord: Constructors & Destructors

NarrowEventRecord::
NarrowEventRecord(jni::EventGlue event, bool controllable, uint32 code)
  : EventRecord(event, controllable),
    mEventCode(code),
    mNumAutomata(0),
    mIsOnlySelfloops(true),
    mIsOnlySelfloopsDisabledInSpec(false),
    mIsGloballyDisabled(false)
{
}


//############################################################################
//# NarrowEventRecord: Simple Access

bool NarrowEventRecord::
isSkippable()
  const
{
  return
    mIsGloballyDisabled ||
    mIsOnlySelfloops && !mIsOnlySelfloopsDisabledInSpec;
}


//############################################################################
//# NarrowEventRecord: Comparing and Hashing

int NarrowEventRecord::
compareTo(const EventRecord* partner)
  const
{
  const NarrowEventRecord* narrow =
    dynamic_cast<const NarrowEventRecord*>(partner);
  if (narrow == 0) {
    return EventRecord::compareTo(partner);
  } else {
    return compareTo(narrow);
  }
}

int NarrowEventRecord::
compareTo(const NarrowEventRecord* partner)
  const
{
  const bool onlyspec1 = mNumPlants == 0;
  const bool onlyspec2 = partner->mNumPlants == 0;
  if (onlyspec1 != onlyspec2) {
    return onlyspec1 ? 1 : -1;
  }
  const bool cont1 = isControllable();
  const bool cont2 = partner->isControllable();
  if (cont1 != cont2) {
    return cont1 ? -1 : 1;
  }
  return EventRecord::compareTo(partner);
}

int NarrowEventRecord::
compare(const void* elem1, const void* elem2)
{
  const NarrowEventRecord* val1 = *((const NarrowEventRecord**) elem1);
  const NarrowEventRecord* val2 = *((const NarrowEventRecord**) elem2);
  return val1->compareTo(val2);
}


//############################################################################
//# NarrowEventRecord: Setup

void NarrowEventRecord::
resetLocalTransitions()
{
  mNumLocalTransitions = 0;
  mIsOnlyLocalSelfloops = true;
}

void NarrowEventRecord::
countLocalTransition(bool selfloop)
{
  mNumLocalTransitions++;
  mIsOnlyLocalSelfloops &= selfloop;
}

void NarrowEventRecord::
mergeLocalToGlobal(bool isplant, uint32 numstates)
{
  mIsOnlySelfloops &= mIsOnlyLocalSelfloops;
  if (mIsOnlySelfloops && !isplant) {
    mIsOnlySelfloopsDisabledInSpec |= mNumLocalTransitions < numstates;
  }
  if (mNumLocalTransitions == 0) {
    mIsGloballyDisabled = isplant || isControllable();
  }
  if (!mIsOnlyLocalSelfloops || mNumLocalTransitions < numstates) {
    mNumAutomata++;
    if (isplant) {
      mNumPlants++;
    }
  }
}

bool NarrowEventRecord::
isLocallySelflooped(uint32 numstates)
  const
{
  return mIsOnlyLocalSelfloops && mNumLocalTransitions == numstates;
}


}  /* namespace waters */
