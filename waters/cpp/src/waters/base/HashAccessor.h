//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashAccessor
//###########################################################################
//# $Id: HashAccessor.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _HashAccessor_h_
#define _HashAccessor_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"


namespace waters {

//############################################################################
//# class HashAccessor
//############################################################################

class HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit HashAccessor() {};
  virtual ~HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const = 0;
  virtual bool equals(const void* key1, const void* key2) const = 0;
  virtual void* getKey(const void* value) const = 0;
  virtual void* getDefaultValue() const = 0;
};


//############################################################################
//# Some Hash Functions
//############################################################################

uint32 hashInt(uint32 key);
uint32 hashInt(const void* key);

uint32 hashString(const char* key);

}   /* namespace waters */

#endif  /* !_HashAccessor_h_ */
