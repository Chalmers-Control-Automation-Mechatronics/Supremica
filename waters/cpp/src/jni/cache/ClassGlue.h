//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   ClassGlue
//###########################################################################
//# $Id$
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

#include <stdint.h>


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
  jmethodID getMethodID(uint32_t methodcode);
  jmethodID getStaticMethodID(uint32_t methodcode);
  jobject getStaticFinalField(uint32_t fieldcode);
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
