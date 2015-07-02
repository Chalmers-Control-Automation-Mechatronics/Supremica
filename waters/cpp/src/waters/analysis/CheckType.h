//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   CheckType
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _CheckType_h_
#define _CheckType_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif


//############################################################################
//# enumeration CheckType
//############################################################################

namespace waters {

enum CheckType {
  CHECK_TYPE_SAFETY,
  CHECK_TYPE_NONBLOCKING
};

}   /* namespace waters */


#endif  /* !_CheckType_h_ */
