//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   GlobalAlphabet
//###########################################################################
//# $Id: GlobalAlphabet.cpp,v 1.1 2005-02-18 01:30:10 robi Exp $
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
GlobalAlphabet(uint32 numprops, uint32 numuncont, uint32 numcont)
  : mFirstUncontrollable(numprops),
    mFirstControllable(mFirstUncontrollable + numuncont),
    mNumEvents(mFirstControllable + numcont)
{
  mEventNames = new jstring[mNumEvents];
  for (uint32 i = 0; i < mNumEvents; i++) {
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
initEventName(uint32 code, jstring name)
{
  mEventNames[code] = name;
}


}   /* namespace waters */
