//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   <basic data types>
//###########################################################################
//# $Id: IntTypes.cpp,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "waters/base/IntTypes.h"


namespace waters {

//############################################################################
//# Elementary Arithmetic
//############################################################################

int log2(uint32 x)
{
  int result = 0;
  if (x > 1) {
    x--;
    do {
      x >>= 1;
      result++;
    } while (x);
  }
  return result;
}

}  /* namespace waters */
