//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
