//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   CodeIterator
//###########################################################################
//# $Id: CodeIterator.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _CodeIterator_h_
#define _CodeIterator_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"


namespace waters {

//############################################################################
//# class CodeIterator
//############################################################################

class CodeIterator
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit CodeIterator(uint32 start, uint32 stop)
    : mPos(start), mStop(stop) {};

  //##########################################################################
  //# Simple Access
  bool hasNext() const {return mPos < mStop;};
  uint32 next() {return mPos++;};

private:
  //##########################################################################
  //# Data Members
  uint32 mPos;
  const uint32 mStop;
};

}   /* namespace waters */

#endif  /* !_CodeIterator_h_ */
