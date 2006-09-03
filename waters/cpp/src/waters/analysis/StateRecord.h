//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   StateRecord
//###########################################################################
//# $Id: StateRecord.h,v 1.3 2006-09-03 06:38:42 robi Exp $
//###########################################################################


#ifndef _StateRecord_h_
#define _StateRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/StateGlue.h"
#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class JavaString;
}


namespace waters {

class HashAccessor;


//###########################################################################
//# Class StateRecordHashAccessor
//###########################################################################

class StateRecordHashAccessor : public PtrHashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit StateRecordHashAccessor() {};
  friend class StateRecord;

public:
  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const;
};



//############################################################################
//# class StateRecord
//############################################################################

class StateRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit StateRecord(jni::StateGlue state,
		       uint32 code,
		       jni::ClassCache* cache);

  //##########################################################################
  //# Simple Access
  uint32 getStateCode() const {return mStateCode;}
  const jni::StateGlue& getJavaState() const {return mJavaState;}
  jni::JavaString getName() const;

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const StateRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}

private:
  //##########################################################################
  //# Data Members
  jni::StateGlue mJavaState;
  int mStateCode;

  //##########################################################################
  //# Class Variables
  static const StateRecordHashAccessor theHashAccessor;
};

}   /* namespace waters */

#endif  /* !_StateRecord_h_ */
