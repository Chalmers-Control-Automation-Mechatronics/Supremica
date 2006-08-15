//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.h,v 1.1 2006-08-15 03:08:53 robi Exp $
//###########################################################################


#ifndef _ControllabilityChecker_h_
#define _ControllabilityChecker_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class ProductDESGlue;
}


namespace waters {

class EventRecord;


//############################################################################
//# class ControllabilityChecker
//############################################################################

class ControllabilityChecker
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ControllabilityChecker(const jni::ProductDESGlue& des,
				  jni::ClassCache* cache);
  ~ControllabilityChecker();

private:
  //##########################################################################
  //# Auxiliary Methods

  //##########################################################################
  //# Data Members
  int mNumEventRecords;
  EventRecord** mEventRecords;

};

}   /* namespace waters */

#endif  /* !_ControllabilityChecker_h_ */
