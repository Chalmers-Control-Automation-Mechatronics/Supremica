//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   MethodInfo
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _MethodInfo_h_
#define _MethodInfo_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>


namespace jni {

//###########################################################################
//# Class MethodInfo
//###########################################################################

class MethodInfo {
public:
  //#########################################################################
  //# Constructors
  explicit MethodInfo(uint32_t code,
                      const char* name, 
                      const char* signature);

  //#########################################################################
  //# Simple Access
  const char* getName() const {return mName;};
  const char* getSignature() const {return mSignature;};

private:
  //#########################################################################
  //# Data Members
  uint32_t mCode;
  const char* mName;
  const char* mSignature;
};

}   /* namespace jni */

#endif  /* !_MethodInfo_h_ */
