//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowEventRecord
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "waters/analysis/NarrowEventRecord.h"


namespace waters {

//############################################################################
//# class NarrowEventRecord
//############################################################################

//############################################################################
//# NarrowEventRecord: Constructors & Destructors

NarrowEventRecord::
NarrowEventRecord(jni::EventGlue event, bool controllable, uint32 code)
  : EventRecord(event, controllable),
    mEventCode(code)
{
}



}  /* namespace waters */
