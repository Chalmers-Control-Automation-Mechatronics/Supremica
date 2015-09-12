//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   WordSize
//###########################################################################
//# $Id$
//###########################################################################

#ifndef __WORDSIZE

// Check windows
#if _WIN32 || _WIN64
  #if _WIN64
    #define __WORDSIZE 64
  #else
    #define __WORDSIZE 32
  #endif
#endif

// Check GCC
#ifdef __GNUG__
  #if __x86_64__ || __ppc64__
    #define __WORDSIZE 64
  #else
    #define __WORDSIZE 32
  #endif
#endif

#ifndef __WORDSIZE
  #error "Could not determine __WORDSIZE !"
#endif

#endif  /* !__WORDSIZE */