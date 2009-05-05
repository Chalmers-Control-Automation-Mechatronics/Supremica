//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionUpdateRecord
//###########################################################################
//# $Id: TransitionUpdateRecord.h,v 1.2 2006-12-01 02:06:30 robi Exp $
//###########################################################################


#ifndef _TransitionUpdateRecord_h_
#define _TransitionUpdateRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"


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
  inline uint32 getWordIndex() const {return mWordIndex;}
  inline uint32 getKeptMask() const {return mKeptMask;}
  inline uint32 getCommonMask() const {return mCommonMask;}
  inline uint32 getCommonTargets() const {return mCommonTargets;}
  inline TransitionRecord* getTransitionRecords() const
    {return mTransitionRecords;}

  //##########################################################################
  //# Set up
  bool addTransition(TransitionRecord* trans);

private:
  //##########################################################################
  //# Data Members
  uint32 mWordIndex;
  uint32 mKeptMask;
  uint32 mCommonMask;
  uint32 mCommonTargets;
  TransitionRecord* mTransitionRecords;
};


}   /* namespace waters */

#endif  /* !_TransitionUpdateRecord_h_ */
