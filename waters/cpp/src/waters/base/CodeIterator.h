//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   CodeIterator
//###########################################################################
//# $Id: CodeIterator.h 4707 2009-05-20 22:45:16Z robi $
//###########################################################################


#ifndef _CodeIterator_h_
#define _CodeIterator_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>


namespace waters {

//############################################################################
//# class CodeIterator
//############################################################################

class CodeIterator
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit CodeIterator(uint32_t start, uint32_t stop)
    : mPos(start), mStop(stop) {};

  //##########################################################################
  //# Simple Access
  bool hasNext() const {return mPos < mStop;};
  uint32_t next() {return mPos++;};

private:
  //##########################################################################
  //# Data Members
  uint32_t mPos;
  const uint32_t mStop;
};

}   /* namespace waters */

#endif  /* !_CodeIterator_h_ */
