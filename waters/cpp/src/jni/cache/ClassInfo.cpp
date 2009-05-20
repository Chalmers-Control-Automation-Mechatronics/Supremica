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
ClassInfo(waters::uint32 code,
          const char* name,
          const ClassInfo* baseclass,
          waters::uint32 nummethods,
          waters::uint32 numfields,
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

waters::uint32 ClassInfo::
getFirstCode()
  const
{
  return mBaseClass == 0 ? 0 : mBaseClass->mNumMethods;
}


const MethodInfo* ClassInfo::
getMethodInfo(waters::uint32 methodcode)
  const
{
  waters::uint32 firstcode = getFirstCode();
  if (methodcode < firstcode) {
    return mBaseClass->getMethodInfo(methodcode);
  } else {
    return &mMethodInfo[methodcode - firstcode];
  }
}


const MethodInfo* ClassInfo::
getFieldInfo(waters::uint32 fieldcode)
  const
{
  int firstcode = getFirstCode();
  return &mMethodInfo[mNumMethods - firstcode + fieldcode];
}


}  /* namespace jni */
