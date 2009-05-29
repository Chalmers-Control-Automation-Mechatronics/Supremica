//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   ExplorerMode
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _ExplorerMode_h_
#define _ExplorerMode_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif


//############################################################################
//# enumeration ExplorerMode
//############################################################################

namespace waters {

enum ExplorerMode {
  EXPLORER_MODE_SAFETY,
  EXPLORER_MODE_NONBLOCKING
};

}   /* namespace waters */


#endif  /* !_ExplorerMode_h_ */
