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
#include "waters/base/IntTypes.h"


namespace jni {


//###########################################################################
//# Class ClassInfo
//###########################################################################

class ClassInfo {
public:
  //#########################################################################
  //# Constructors
  explicit ClassInfo(waters::uint32 code,
                     const char* name, 
                     const ClassInfo* baseclass,
                     waters::uint32 nummethods,
                     waters::uint32 numfields,
                     const MethodInfo* methods);

  //#########################################################################
  //# Simple Access
  const waters::uint32 getClassCode() const {return mCode;};
  const char* getName() const {return mName;};
  waters::uint32 getFirstCode() const;
  waters::uint32 getNumMethods() const {return mNumMethods;};
  const MethodInfo* getMethodInfo(waters::uint32 methodcode) const;
  const waters::uint32 getNumFields() const {return mNumFields;};
  const MethodInfo* getFieldInfo(waters::uint32 fieldcode) const;

private:
  //#########################################################################
  //# Data Members
  waters::uint32 mCode;
  const char* mName;
  const ClassInfo* mBaseClass;
  waters::uint32 mNumMethods;
  waters::uint32 mNumFields;
  const MethodInfo* mMethodInfo;
};

}   /* namespace jni */

#endif  /* !_ClassInfo_h_ */
