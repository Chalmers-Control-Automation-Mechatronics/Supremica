//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.h,v 1.3 2006-08-17 05:02:25 robi Exp $
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
#include "waters/analysis/AutomatonEncoding.h"


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
  explicit ControllabilityChecker(jni::ProductDESGlue des,
				  jni::ClassCache* cache);
  virtual ~ControllabilityChecker();

private:
  //##########################################################################
  //# Auxiliary Methods

  //##########################################################################
  //# Data Members
  AutomatonEncoding mEncoding;
  int mNumEventRecords;
  EventRecord** mEventRecords;

};

}   /* namespace waters */

#endif  /* !_ControllabilityChecker_h_ */
