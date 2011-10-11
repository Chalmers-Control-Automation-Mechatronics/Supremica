//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionUpdateRecord
//###########################################################################
//# $Id: TransitionUpdateRecord.cpp 4707 2009-05-20 22:45:16Z robi $
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
  : mKeptMask(UINT32_MAX),
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
  const uint32_t mask = aut->getBitMask();
  mKeptMask &= ~mask;
  const uint32_t target = trans->getCommonTarget();
  if (target == UINT32_MAX) {
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
