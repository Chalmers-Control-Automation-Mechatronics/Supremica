//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   ClassGlue
//###########################################################################
//# $Id: ClassGlue.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _ClassGlue_h_
#define _ClassGlue_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "waters/base/IntTypes.h"

namespace waters {
  class HashAccessor;
}


namespace jni {

class ClassCache;
class ClassInfo;


//############################################################################
//# class ClassGlue
//############################################################################

class ClassGlue
{
public:
  //##########################################################################
  //# Static Class Methods
  static const waters::HashAccessor* getHashAccessor();

  //##########################################################################
  //# Constructors & Destructors
  explicit ClassGlue(const ClassInfo* info, jclass javaclass, JNIEnv* env);
  ~ClassGlue();

  //##########################################################################
  //# Access
  jclass getJavaClass() const {return mJavaClass;};
  jmethodID getMethodID(waters::uint32 methodcode);
  jobject getStaticFinalField(waters::uint32 fieldcode);
  JNIEnv* getEnvironment() const {return mEnvironment;};

private:
  //##########################################################################
  //# Data Members
  const ClassInfo* mClassInfo;
  jclass mJavaClass;
  jmethodID* mMethodTable;
  jobject* mStaticFinalFieldTable;
  JNIEnv* mEnvironment;

  //##########################################################################
  //# Class Constants
  static const waters::HashAccessor* theAccessor;
};

}   /* namespace jni */

#endif  /* !_ClassGlue_h_ */
