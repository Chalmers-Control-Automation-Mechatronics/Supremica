//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   <basic data types>
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _IntTypes_h_
#define _IntTypes_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif


namespace waters {

//############################################################################
//# Integer Data Types
//############################################################################

typedef unsigned int uint32;

#define UNDEF_UINT32 ((waters::uint32) -1)
#define UNDEF_INT32 0x7fffffff

//############################################################################
//# Elementary Arithmetic
//############################################################################

int log2(uint32 x);

inline uint32 tablesize(uint32 x)
{
  return 1 << log2(x);
}

inline uint32 bitmask(uint32 x)
{
  return tablesize(x) - 1;
}

}  /* namespace waters */

#endif  /* !_IntTypes_h_ */
