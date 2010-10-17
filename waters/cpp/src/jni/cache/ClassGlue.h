//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   ClassGlue
//###########################################################################
//# $Id: ClassGlue.h,v 1.2 2005-11-05 09:47:15 robi Exp $
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
//# class ClassKey
//############################################################################

class ClassKey
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ClassKey(const ClassInfo* info, jclass javaclass) :
    mClassInfo(info), mJavaClass(javaclass) {}

  //##########################################################################
  //# Simple Access
  inline const ClassInfo* getClassInfo() const {return mClassInfo;};
  inline jclass getJavaClass() const {return mJavaClass;};

private:
  //##########################################################################
  //# Data Members
  const ClassInfo* mClassInfo;
  jclass mJavaClass;
};


//############################################################################
//# class ClassGlue
//############################################################################

class ClassGlue
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ClassGlue(const ClassInfo* info, jclass javaclass, JNIEnv* env);
  ~ClassGlue();

  //##########################################################################
  //# Access
  const ClassKey* getClassKey() const {return &mKey;};
  jclass getJavaClass() const {return mKey.getJavaClass();};
  jmethodID getMethodID(waters::uint32 methodcode);
  jmethodID getStaticMethodID(waters::uint32 methodcode);
  jobject getStaticFinalField(waters::uint32 fieldcode);
  JNIEnv* getEnvironment() const {return mEnvironment;};

private:
  //##########################################################################
  //# Data Members
  ClassKey mKey;
  jmethodID* mMethodTable;
  jobject* mStaticFinalFieldTable;
  JNIEnv* mEnvironment;
};

}   /* namespace jni */

#endif  /* !_ClassGlue_h_ */
