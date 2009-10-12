//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowEventRecord
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _NarrowEventRecord_h_
#define _NarrowEventRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/EventGlue.h"

#include "waters/base/IntTypes.h"

#include "waters/analysis/EventRecord.h"


namespace waters {


//############################################################################
//# class NarrowEventRecord
//############################################################################

class NarrowEventRecord : public EventRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowEventRecord(jni::EventGlue event,
			     bool controllable,
			     uint32 code = UNDEF_UINT32);

  //##########################################################################
  //# Simple Access
  inline uint32 getEventCode() const {return mEventCode;}
  inline void setEventCode(uint32 code) {mEventCode = code;}
  inline uint32 getNumberOfAutomata() const {return mNumAutomata;}
  inline uint32 getNumberOfPlants() const {return mNumPlants;}
  inline bool isGloballyDisabled() const {return mIsGloballyDisabled;}
  inline bool isOnlySelfloops() const {return mIsOnlySelfloops;}

  //##########################################################################
  //# Setup
  void resetLocalTransitions();
  void countLocalTransition(bool selfloop);
  void mergeLocalToGlobal(bool isplant, uint32 numstates);
  bool isLocallySelflooped(uint32 numstates);

  //##########################################################################
  //# Trace Computation
  virtual void storeNondeterministicTargets(const uint32* sourcetuple,
					    const uint32* targettuple,
					    const jni::MapGlue& map) const;

private:
  //##########################################################################
  //# Data Members
  uint32 mEventCode;
  uint32 mNumAutomata;
  uint32 mNumPlants;
  bool mIsOnlySelfloops;
  bool mIsGloballyDisabled;
  uint32 mNumLocalTransitions;
  bool mIsOnlyLocalSelfloops;
};

}   /* namespace waters */

#endif  /* !_NarrowEventRecord_h_ */
