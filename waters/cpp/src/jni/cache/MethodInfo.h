//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   MethodInfo
//###########################################################################
//# $Id: MethodInfo.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _MethodInfo_h_
#define _MethodInfo_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/base/IntTypes.h"


namespace jni {

//###########################################################################
//# Class MethodInfo
//###########################################################################

class MethodInfo {
public:
  //#########################################################################
  //# Constructors
  explicit MethodInfo(waters::uint32 code,
                      const char* name, 
                      const char* signature);

  //#########################################################################
  //# Simple Access
  const char* getName() const {return mName;};
  const char* getSignature() const {return mSignature;};

private:
  //#########################################################################
  //# Data Members
  waters::uint32 mCode;
  const char* mName;
  const char* mSignature;
};

}   /* namespace jni */

#endif  /* !_MethodInfo_h_ */
