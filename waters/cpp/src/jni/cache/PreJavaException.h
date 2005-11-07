//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   PreJavaException
//###########################################################################
//# $Id: PreJavaException.h,v 1.1 2005-11-07 23:45:47 robi Exp $
//###########################################################################


#ifndef _PreJavaException_h_
#define _PreJavaException_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"


namespace jni {


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
  explicit PreJavaException(waters::uint32 classcode, const char* msg);
  explicit PreJavaException(waters::uint32 classcode,
			    const char* msg,
			    bool staticString);
  PreJavaException(const PreJavaException& partner);
  ~PreJavaException();
  PreJavaException& operator= (const PreJavaException& partner);

  //#########################################################################
  //# Simple Access
  const waters::uint32 getClassCode() const {return mClassCode;}
  const char* getMessage() const {return mMessage;}

private:
  //#########################################################################
  //# Auxiliary Methods
  void initMessage(const char* msg, bool staticString);

  //#########################################################################
  //# Data Members
  waters::uint32 mClassCode;
  bool mStaticString;
  char* mMessage;
};

}   /* namespace jni */

#endif  /* !_PreJavaException_h_ */
