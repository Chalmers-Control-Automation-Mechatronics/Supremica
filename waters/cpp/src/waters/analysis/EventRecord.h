//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventRecord
//###########################################################################
//# $Id: EventRecord.h,v 1.3 2006-08-17 10:15:12 robi Exp $
//###########################################################################


#ifndef _EventRecord_h_
#define _EventRecord_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"

namespace jni {
  class ClassCache;
  class EventGlue;
}


namespace waters {

class HashAccessor;
class TransitionRecord;


//###########################################################################
//# Class EventRecordHashAccessor
//###########################################################################

class EventRecordHashAccessor : public HashAccessor
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
  virtual void* getDefaultValue() const {return 0;}  
};



//############################################################################
//# class EventRecord
//############################################################################

class EventRecord
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventRecord(jni::EventGlue event,
		       bool controllable,
		       jni::ClassCache* cache);

  //##########################################################################
  //# Simple Access
  bool isControllable() const {return mIsControllable;}
  const jni::EventGlue& getJavaEvent() const {return mJavaEvent;}

  //##########################################################################
  //# Comparing and Hashing
  int compareTo(const EventRecord* partner) const;
  static int compare(const void* elem1, const void* elem2);
  static const HashAccessor* getHashAccessor() {return &theHashAccessor;}

private:
  //##########################################################################
  //# Data Members
  jni::EventGlue mJavaEvent;
  bool mIsControllable;
  TransitionRecord* mTransitionRecords;

  //##########################################################################
  //# Class Variables
  static const EventRecordHashAccessor theHashAccessor;
};

}   /* namespace waters */

#endif  /* !_EventRecord_h_ */
