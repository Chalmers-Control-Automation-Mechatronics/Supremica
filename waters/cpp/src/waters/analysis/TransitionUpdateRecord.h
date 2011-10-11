//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionUpdateRecord
//###########################################################################
//# $Id: TransitionUpdateRecord.h 4707 2009-05-20 22:45:16Z robi $
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
