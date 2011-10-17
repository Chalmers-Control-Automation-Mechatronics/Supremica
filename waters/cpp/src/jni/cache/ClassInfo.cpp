//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.base
//# CLASS:   ClassInfo
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassInfo.h"


namespace jni {

//###########################################################################
//# Class ClassInfo
//###########################################################################

//###########################################################################
//# ClassInfo: Constructors

ClassInfo::
ClassInfo(uint32_t code,
          const char* name,
          const ClassInfo* baseclass,
          uint32_t nummethods,
          uint32_t numfields,
          const MethodInfo* methods)
{
  mCode = code;
  mName = name;
  mBaseClass = baseclass;
  mNumMethods = nummethods;
  mNumFields = numfields;
  mMethodInfo = methods;
}

//###########################################################################
//# ClassInfo: Simple Access

uint32_t ClassInfo::
getFirstCode()
  const
{
  return mBaseClass == 0 ? 0 : mBaseClass->mNumMethods;
}


const MethodInfo* ClassInfo::
getMethodInfo(uint32_t methodcode)
  const
{
  uint32_t firstcode = getFirstCode();
  if (methodcode < firstcode) {
    return mBaseClass->getMethodInfo(methodcode);
  } else {
    return &mMethodInfo[methodcode - firstcode];
  }
}


const MethodInfo* ClassInfo::
getFieldInfo(uint32_t fieldcode)
  const
{
  int firstcode = getFirstCode();
  return &mMethodInfo[mNumMethods - firstcode + fieldcode];
}


}  /* namespace jni */
