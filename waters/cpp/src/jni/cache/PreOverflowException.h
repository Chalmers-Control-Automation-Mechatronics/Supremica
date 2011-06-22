//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   PreOverflowException
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _PreOverflowException_h_
#define _PreOverflowException_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "jni/cache/PreJavaException.h"
#include "jni/glue/OverflowKindGlue.h"


namespace jni {

class ClassCache;


//###########################################################################
//# Class PreOverflowException
//###########################################################################
//# A data structure containing the information for an overflow exception to
//# be thrown from native code. This is thrown as a C++ exception to be
//# converted to a proper OverflowException for Java as soon as the environment
//# becomes available.
//###########################################################################

class PreOverflowException : public PreJavaException
{
public:
  //#########################################################################
  //# Constructors, Destructors & Co.
  explicit PreOverflowException(OverflowKind kind, int limit);
  PreOverflowException(const PreOverflowException& partner);
  PreOverflowException& operator=(const PreOverflowException& partner);

  //#########################################################################
  //# Simple Access
  OverflowKind getKind() const {return mKind;}
  int getLimit() const {return mLimit;}

  //#########################################################################
  //# Throwing Exceptions
  virtual jint throwJavaException(ClassCache& cache) const;

private:
  //#########################################################################
  //# Data Members
  OverflowKind mKind;
  int mLimit;
};

}   /* namespace jni */

#endif  /* !_PreOverflowException_h_ */
