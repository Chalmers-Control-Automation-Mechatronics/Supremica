//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   ClassCache
//###########################################################################
//# $Id: ClassCache.h,v 1.4 2006-09-03 06:38:42 robi Exp $
//###########################################################################


#ifndef _ClassCache_h_
#define _ClassCache_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "jni/cache/ClassGlue.h"
#include "waters/base/HashAccessor.h"
#include "waters/base/HashTable.h"


namespace jni {

class ClassInfo;
class ExceptionGlue;
class PreJavaException;


//###########################################################################
//# Class ClassCacheHashAccessor
//###########################################################################

class ClassCacheHashAccessor : public waters::PtrHashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ClassCacheHashAccessor(JNIEnv* env);

  //##########################################################################
  //# Simple Access
  ClassGlue* getClassGlue() const {return mClassGlue;};

  //##########################################################################
  //# Hash Methods
  virtual waters::uint32 hash(const void* key) const;
  virtual bool equals(const void* key1, const void* key2) const;
  virtual const void* getKey(const void* value) const
    {return ((ClassGlue*) value)->getJavaClass();};

private:
  //##########################################################################
  //# Data Members
  JNIEnv* mEnvironment;
  ClassGlue* mClassGlue;
};


//############################################################################
//# class ClassCache
//############################################################################

class ClassCache
{
public:
  //##########################################################################
  //# Constructors & Destructors
  ClassCache(JNIEnv* env);
  ~ClassCache();

  //##########################################################################
  //# Access
  JNIEnv* getEnvironment() const {return mEnvironment;};
  ClassGlue* getClass(waters::uint32 classcode);
  ClassGlue* getClass(jclass javaclass, waters::uint32 classcode);
  ClassGlue* getClass(jclass javaclass, const ClassInfo* info);

  //##########################################################################
  //# Exceptions
  jint throwJavaException(waters::uint32 classcode, const char* msg);
  jint throwJavaException(const PreJavaException& pre);
  jthrowable throwJavaException(const ExceptionGlue& glue);
  
private:
  //##########################################################################
  //# Data Members
  JNIEnv* mEnvironment;
  ClassCacheHashAccessor mAccessor;
  waters::HashTable<jclass,ClassGlue*> mClassMap;
  ClassGlue** mCodeMap;

};

}   /* namespace jni */

#endif  /* !_ClassCache_h_ */
