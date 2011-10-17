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

#include <stdint.h>

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
			     uint32_t code = UINT32_MAX);

  //##########################################################################
  //# Simple Access
  inline uint32_t getEventCode() const {return mEventCode;}
  inline void setEventCode(uint32_t code) {mEventCode = code;}
  inline uint32_t getNumberOfAutomata() const {return mNumAutomata;}
  inline uint32_t getNumberOfPlants() const {return mNumPlants;}
  inline bool isGloballyDisabled() const {return mIsGloballyDisabled;}
  inline bool isOnlySelfloops() const {return mIsOnlySelfloops;}
  inline bool isPlantOnly() const {return mNumAutomata == mNumPlants;}
  inline bool isSpecOnly() const {return mNumPlants == 0;}
  bool isSkippable() const;

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const EventRecord* partner) const;
  int compareTo(const NarrowEventRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);

  //##########################################################################
  //# Setup
  void resetLocalTransitions();
  void countLocalTransition(bool selfloop);
  void mergeLocalToGlobal(bool isplant, uint32_t numstates);
  bool isLocallySelflooped(uint32_t numstates) const;

private:
  //##########################################################################
  //# Data Members
  uint32_t mEventCode;
  uint32_t mNumAutomata;
  uint32_t mNumPlants;
  bool mIsOnlySelfloops;
  bool mIsOnlySelfloopsDisabledInSpec;
  bool mIsGloballyDisabled;
  uint32_t mNumLocalTransitions;
  bool mIsOnlyLocalSelfloops;
};

}   /* namespace waters */

#endif  /* !_NarrowEventRecord_h_ */
