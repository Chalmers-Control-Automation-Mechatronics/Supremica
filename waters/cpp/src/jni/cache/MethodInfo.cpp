//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   MethodInfo
//###########################################################################
//# $Id: MethodInfo.cpp 4707 2009-05-20 22:45:16Z robi $
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
MethodInfo(uint32_t code,
           const char* name, 
           const char* signature)
{
  mCode = code;
  mName = name;
  mSignature = signature;
}

}  /* namespace jni */
