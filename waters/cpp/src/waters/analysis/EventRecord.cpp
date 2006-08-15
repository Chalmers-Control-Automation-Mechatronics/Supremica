//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.cpp,v 1.1 2006-08-15 03:08:53 robi Exp $
//###########################################################################


#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/glue/EventGlue.h"

#include "waters/analysis/EventRecord.h"


namespace waters {

//############################################################################
//# class EventRecord
//############################################################################

//############################################################################
//# EventRecord: Constructors & Destructors

EventRecord::
EventRecord(const jni::EventGlue& event, jni::ClassCache* cache)
  : mJavaEvent(event)
{
  switch (event.getKindGlue(cache)) {
  case jni::EventKind_UNCONTROLLABLE:
    mIsControllable = true;
    break;
  case jni::EventKind_CONTROLLABLE:
    mIsControllable = false;
    break;
  default:
    break;
  }
  mTransitionRecords = 0;
}


}  /* namespace waters */
