//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   GlobalAlphabet
//###########################################################################
//# $Id: GlobalAlphabet.cpp 4707 2009-05-20 22:45:16Z robi $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "waters/des/GlobalAlphabet.h"

namespace waters {


//############################################################################
//# class GlobalAlphabet
//############################################################################

//###########################################################################
//# GlobalAlphabet: Constructors & Destructors

GlobalAlphabet::
GlobalAlphabet(uint32_t numuncont, uint32_t numcont)
  : mFirstControllable(numuncont),
    mNumEvents(numuncont + numcont)
{
  mEventNames = new jstring[mNumEvents];
  for (uint32_t i = 0; i < mNumEvents; i++) {
    mEventNames[i] = 0;
  }
}


GlobalAlphabet::
~GlobalAlphabet()
{
  delete [] mEventNames;
}


//############################################################################
//# GlobalAlphabet: Initialisation

void GlobalAlphabet::
initEventName(uint32_t code, jstring name)
{
  mEventNames[code] = name;
}


}   /* namespace waters */
