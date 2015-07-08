//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   PreJavaException
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _PreJavaException_h_
#define _PreJavaException_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include <stdint.h>


namespace jni {

class ClassCache;


//###########################################################################
//# Class PreJavaException
//###########################################################################
//# A data structure containing the information for a Java esception to be
//# thrown from native code. This is thrown as a C++ exception to be
//# converted to a proper Java exception as soon as the environment
//# becomes available.
//###########################################################################

class PreJavaException {
public:
  //#########################################################################
  //# Constructors, Destructors & Co.
  explicit PreJavaException();
  explicit PreJavaException(uint32_t classcode);
  explicit PreJavaException(uint32_t classcode,
			    const char* msg,
			    bool staticString = false);
  PreJavaException(const PreJavaException& partner);
  virtual ~PreJavaException();
  PreJavaException& operator=(const PreJavaException& partner);

  //#########################################################################
  //# Simple Access
  const uint32_t getClassCode() const {return mClassCode;}
  const char* getMessage() const {return mMessage;}

  //#########################################################################
  //# Throwing Exceptions
  virtual jint throwJavaException(ClassCache& cache) const;

protected:
  //#########################################################################
  //# Auxiliary Methods
  void initMessage(const char* msg, bool staticString);

private:
  //#########################################################################
  //# Data Members
  uint32_t mClassCode;
  bool mStaticString;
  char* mMessage;
};

}   /* namespace jni */

#endif  /* !_PreJavaException_h_ */
