//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionUpdateRecord
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/analysis/TransitionUpdateRecord.h"


namespace waters {

//############################################################################
//# class TransitionUpdateRecord
//############################################################################

//############################################################################
//# TransitionUpdateRecord: Constructors & Destructors

TransitionUpdateRecord::
TransitionUpdateRecord()
  : mKeptMask(UNDEF_UINT32),
    mCommonMask(0),
    mCommonTargets(0),
    mTransitionRecords(0)
{
}

TransitionUpdateRecord::
~TransitionUpdateRecord()
{
}


//############################################################################
//# TransitionRecord: Set up

bool TransitionUpdateRecord::
addTransition(TransitionRecord* trans)
{
  const AutomatonRecord* aut = trans->getAutomaton();
  const uint32 mask = aut->getBitMask();
  mKeptMask &= ~mask;
  const uint32 target = trans->getCommonTarget();
  if (target == UNDEF_UINT32) {
    trans->setNextInUpdate(mTransitionRecords);
    mTransitionRecords = trans;
    return true;
  } else {
    mCommonMask |= mask;
    mCommonTargets |= target;
    return false;
  }
}


}  /* namespace waters */
