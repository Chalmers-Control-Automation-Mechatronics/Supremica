//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionUpdateRecord
//###########################################################################
//# $Id: TransitionUpdateRecord.cpp,v 1.1 2006-11-22 21:27:57 robi Exp $
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
    mCommonTargets |= target;
    return false;
  }
}


}  /* namespace waters */
