//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   MethodInfo
//###########################################################################
//# $Id: MethodInfo.cpp,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/MethodInfo.h"


namespace jni {

//###########################################################################
//# Class MethodInfo
//###########################################################################

MethodInfo::
MethodInfo(waters::uint32 code,
           const char* name, 
           const char* signature)
{
  mCode = code;
  mName = name;
  mSignature = signature;
}

}  /* namespace jni */
