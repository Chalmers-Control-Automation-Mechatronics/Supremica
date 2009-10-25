//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _EventRecord_h_
#define _EventRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/glue/EventGlue.h"
#include "waters/base/HashAccessor.h"
#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class JavaString;
}


namespace waters {


//###########################################################################
//# Class EventRecordHashAccessor
//###########################################################################

class EventRecordHashAccessor : public PtrHashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventRecordHashAccessor() {};
  friend class EventRecord;

public:
  //##########################################################################
  //# Hash Methods
  virtual uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const;
};



//############################################################################
//# class EventRecord
//############################################################################

class EventRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventRecord(jni::EventGlue event, bool controllable);
  virtual ~EventRecord() {}

  //##########################################################################
  //# Simple Access
  inline bool isControllable() const {return mIsControllable;}
  inline void setControllable(bool controllable)
    {mIsControllable = controllable;}
  const jni::EventGlue& getJavaEvent() const {return mJavaEvent;}
  jni::JavaString getName() const;

  //##########################################################################
  //# Comparing and Hashing
  virtual int compareTo(const EventRecord* partner) const;
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}
  static int compare(const void* elem1, const void* elem2);

private:
  //##########################################################################
  //# Data Members
  jni::EventGlue mJavaEvent;
  bool mIsControllable;

  //##########################################################################
  //# Class Variables
  static const EventRecordHashAccessor theHashAccessor;
};

}   /* namespace waters */

#endif  /* !_EventRecord_h_ */
