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
			     uint32 code);

  //##########################################################################
  //# Simple Access
  inline uint32 getEventCode() const {return mEventCode;}

private:
  //##########################################################################
  //# Data Members
  uint32 mEventCode;
};

}   /* namespace waters */

#endif  /* !_NarrowEventRecord_h_ */
