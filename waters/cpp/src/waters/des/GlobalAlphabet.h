//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   GlobalAlphabet
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _GlobalAlphabet_h_
#define _GlobalAlphabet_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "waters/base/CodeIterator.h"
#include <stdint.h>


namespace waters {

//############################################################################
//# class GlobalAlphabet
//############################################################################

class GlobalAlphabet
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit GlobalAlphabet(uint32_t numuncont, uint32_t numcont);
  ~GlobalAlphabet();

  //##########################################################################
  //# Initialisation
  void initEventName(uint32_t code, jstring name);

  //##########################################################################
  //# Simple Access
  uint32_t getNumEvents() const
    {return mNumEvents;};
  bool isUncontrollable(uint32_t code) const
    {return code < mFirstControllable;};
  bool isControllable(uint32_t code) const
    {return code >= mFirstControllable;};
  jstring getEventName(uint32_t code) const
    {return mEventNames[code];};

  //##########################################################################
  //# Iterators
  CodeIterator iterator() const
    {return CodeIterator(0, mNumEvents);};
  CodeIterator uncontrollableIterator() const
    {return CodeIterator(0, mFirstControllable);};
  CodeIterator controllableIterator() const
    {return CodeIterator(mFirstControllable, mNumEvents);};

private:
  //##########################################################################
  //# Data Members
  const uint32_t mFirstControllable;
  const uint32_t mNumEvents;
  jstring* mEventNames;
};

}   /* namespace waters */

#endif  /* !_GlobalAlphabet_h_ */
