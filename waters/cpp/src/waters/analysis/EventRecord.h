//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.h,v 1.1 2006-08-15 03:08:53 robi Exp $
//###########################################################################


#ifndef _EventRecord_h_
#define _EventRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class EventGlue;
}


namespace waters {

class TransitionRecord;


//############################################################################
//# class EventRecord
//############################################################################

class EventRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventRecord(const jni::EventGlue& event, jni::ClassCache* cache);

private:
  //##########################################################################
  //# Auxiliary Methods

  //##########################################################################
  //# Data Members
  const jni::EventGlue& mJavaEvent;
  bool mIsControllable;
  TransitionRecord* mTransitionRecords;
};

}   /* namespace waters */

#endif  /* !_EventRecord_h_ */
