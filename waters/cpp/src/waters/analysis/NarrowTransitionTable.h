//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowTransitionTable
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _NarrowTransitionTable_h_
#define _NarrowTransitionTable_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/HashTable.h"
#include "waters/base/IntTypes.h"


namespace jni {
  class ClassCache;
  class EventGlue;
}


namespace waters {

class AutomatonRecord;
class NarrowEventRecord;


//############################################################################
//# class NarrowTransitionTable
//############################################################################

class NarrowTransitionTable
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit NarrowTransitionTable(AutomatonRecord* aut);
  ~NarrowTransitionTable();

  //##########################################################################
  //# Simple Access
  inline const AutomatonRecord* getAutomaton() const {return mAutomaton;}

private:
  //##########################################################################
  //# Auxiliary Methods
  void setup(jni::ClassCache* cache,
	     const HashTable<const jni::EventGlue*,NarrowEventRecord*>&
	       eventmap);

  //##########################################################################
  //# Data Members
  AutomatonRecord* mAutomaton;
  uint32* mStateTable;
  uint32* mBuffers;

  //##########################################################################
  //# Class Constants
  static const uint32 TAG_NONDET = 0x10000000;
};


}   /* namespace waters */

#endif  /* !_NarrowTransitionTable_h_ */
