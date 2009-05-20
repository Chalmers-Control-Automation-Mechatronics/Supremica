//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   MethodInfo
//###########################################################################
//# $Id$
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
