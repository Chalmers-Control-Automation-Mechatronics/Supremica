//# This may look like C code, but it really is -*- C++ -*-
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

#ifndef _TransitionUpdateRecord_h_
#define _TransitionUpdateRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>


namespace waters {

class TransitionRecord;


//############################################################################
//# class TransitionUpdateRecord
//############################################################################

class TransitionUpdateRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TransitionUpdateRecord();
  ~TransitionUpdateRecord();

  //##########################################################################
  //# Simple Access
  inline uint32_t getWordIndex() const {return mWordIndex;}
  inline uint32_t getKeptMask() const {return mKeptMask;}
  inline uint32_t getCommonMask() const {return mCommonMask;}
  inline uint32_t getCommonTargets() const {return mCommonTargets;}
  inline TransitionRecord* getTransitionRecords() const
    {return mTransitionRecords;}

  //##########################################################################
  //# Set up
  bool addTransition(TransitionRecord* trans);

private:
  //##########################################################################
  //# Data Members
  uint32_t mWordIndex;
  uint32_t mKeptMask;
  uint32_t mCommonMask;
  uint32_t mCommonTargets;
  TransitionRecord* mTransitionRecords;
};


}   /* namespace waters */

#endif  /* !_TransitionUpdateRecord_h_ */
