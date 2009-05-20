//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashAccessor
//###########################################################################
//# $Id$
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
  explicit HashAccessor() {}
  virtual ~HashAccessor() {}

  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const = 0;
  virtual bool equals(const void* key1, const void* key2) const = 0;
  virtual const void* getKey(const void* value) const = 0;

  //##########################################################################
  //# Tagging and Detagging
  virtual void* getDefaultValue() const = 0;
  virtual bool isLink(const void* item) const = 0;
  virtual void* detagLink(const void* item) const = 0;
  virtual void* entagLink(const void* item) const = 0;
};


//############################################################################
//# class PtrHashAccessor
//############################################################################

class PtrHashAccessor : public HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit PtrHashAccessor() {}

  //##########################################################################
  //# Tagging and Detagging
  virtual void* getDefaultValue() const {return 0;}
  virtual bool isLink(const void* item) const {return ((int) item & TAG) != 0;}
  virtual void* detagLink(const void* item) const
    {return (void*) ((int) item & TAG ? (int) item & ~TAG : 0);}
  virtual void* entagLink(const void* item) const
    {return (void*) ((int) item | TAG);}

private:
  //##########################################################################
  //# Class Constants
  static const int TAG = 1;
};


//############################################################################
//# class IntHashAccessor
//############################################################################

class IntHashAccessor : public HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit IntHashAccessor() {}

  //##########################################################################
  //# Tagging and Detagging
  virtual void* getDefaultValue() const {return (void*) UNDEF_UINT32;}
  virtual bool isLink(const void* item) const {return ((int) item & TAG) != 0;}
  virtual void* detagLink(const void* item) const
    {return (void*) ((int) item & TAG ? (int) item << 1 : 0);}
  virtual void* entagLink(const void* item) const
    {return (void*) (((int) item >> 1) | TAG);}

private:
  //##########################################################################
  //# Class Constants
  static const int TAG = 0x80000000;
};


//############################################################################
//# Some Hash Functions
//############################################################################

uint32 hashInt(uint32 key);

uint32 hashInt(int key);

uint32 hashInt(const void* key);

uint32 hashIntArray(const uint32* key, const int len);

uint32 hashIntArray(const uint32* key, const int len, const uint32 mask0);

uint32 hashString(const char* key);


}   /* namespace waters */

#endif  /* !_HashAccessor_h_ */
