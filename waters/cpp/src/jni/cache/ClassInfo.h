//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   ClassInfo
//###########################################################################
//# $Id: ClassInfo.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _ClassInfo_h_
#define _ClassInfo_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/cache/MethodInfo.h"
#include <stdint.h>


namespace jni {


//###########################################################################
//# Class ClassInfo
//###########################################################################

class ClassInfo {
public:
  //#########################################################################
  //# Constructors
  explicit ClassInfo(uint32_t code,
                     const char* name, 
                     const ClassInfo* baseclass,
                     uint32_t nummethods,
                     uint32_t numfields,
                     const MethodInfo* methods);

  //#########################################################################
  //# Simple Access
  const uint32_t getClassCode() const {return mCode;};
  const char* getName() const {return mName;};
  uint32_t getFirstCode() const;
  uint32_t getNumMethods() const {return mNumMethods;};
  const MethodInfo* getMethodInfo(uint32_t methodcode) const;
  const uint32_t getNumFields() const {return mNumFields;};
  const MethodInfo* getFieldInfo(uint32_t fieldcode) const;

private:
  //#########################################################################
  //# Data Members
  uint32_t mCode;
  const char* mName;
  const ClassInfo* mBaseClass;
  uint32_t mNumMethods;
  uint32_t mNumFields;
  const MethodInfo* mMethodInfo;
};

}   /* namespace jni */

#endif  /* !_ClassInfo_h_ */
