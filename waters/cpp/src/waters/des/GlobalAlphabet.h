//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   GlobalAlphabet
//###########################################################################
//# $Id: GlobalAlphabet.h,v 1.1 2005-02-18 01:30:10 robi Exp $
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
#include "waters/base/IntTypes.h"


namespace waters {

//############################################################################
//# class GlobalAlphabet
//############################################################################

class GlobalAlphabet
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit GlobalAlphabet(uint32 numprops, uint32 numuncont, uint32 numcont);
  ~GlobalAlphabet();

  //##########################################################################
  //# Initialisation
  void initEventName(uint32 code, jstring name);

  //##########################################################################
  //# Simple Access
  uint32 getNumEvents() const
    {return mNumEvents;};
  bool isProposition(uint32 code) const
    {return code < mFirstUncontrollable;};
  bool isUncontrollable(uint32 code) const
    {return code >= mFirstUncontrollable && code < mFirstControllable;};
  bool isControllable(uint32 code) const
    {return code >= mFirstControllable;};
  jstring getEventName(uint32 code) const
    {return mEventNames[code];};

  //##########################################################################
  //# Iterators
  CodeIterator iterator() const
    {return CodeIterator(0, mNumEvents);};
  CodeIterator propositionIterator() const
    {return CodeIterator(0, mFirstUncontrollable);};
  CodeIterator uncontrollableIterator() const
    {return CodeIterator(mFirstUncontrollable, mFirstControllable);};
  CodeIterator controllableIterator() const
    {return CodeIterator(mFirstControllable, mNumEvents);};

private:
  //##########################################################################
  //# Data Members
  const uint32 mFirstUncontrollable;
  const uint32 mFirstControllable;
  const uint32 mNumEvents;
  jstring* mEventNames;
};

}   /* namespace waters */

#endif  /* !_GlobalAlphabet_h_ */
