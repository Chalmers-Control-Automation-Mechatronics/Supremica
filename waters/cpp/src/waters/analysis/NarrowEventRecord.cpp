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
    mNumAutomata(0)
{
}


//############################################################################
//# NarrowEventRecord: Simple Access

void NarrowEventRecord::
resetLocalTransitions()
{
  mNumLocalTransitions = 0;
  mIsOnlyLocalSelfloops = true;
}

void NarrowEventRecord::
countLocalTransition(bool selfloop)
{
  mNumLocalTransitions;
  mIsOnlyLocalSelfloops &= selfloop;
}

void NarrowEventRecord::
mergeLocalToGlobal(bool isplant, uint32 numstates)
{
  mIsOnlySelfloops &= mIsOnlyLocalSelfloops;
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
{
  return mIsOnlyLocalSelfloops && mNumLocalTransitions == numstates;
}


}  /* namespace waters */
