//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   JavaString
//###########################################################################
//# $Id: JavaString.cpp,v 1.1 2006-09-03 06:38:42 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "jni/cache/JavaString.h"


namespace jni {


//###########################################################################
//# Class JavaString
//###########################################################################

//###########################################################################
//# JavaString: Constructors, Destructors & Co.

JavaString::
JavaString(JNIEnv* env, const jstring javastring) :
  mEnvironment(env),
  mJavaString(javastring),
  mChars(0)
{
}


JavaString::
JavaString(const JavaString& partner) :
  mEnvironment(partner.mEnvironment),
  mJavaString(partner.mJavaString),
  mChars(0)
{
}

JavaString::
~JavaString()
{
  if (mChars) {
    mEnvironment->ReleaseStringUTFChars(mJavaString, mChars);
  }
}

JavaString& JavaString::
operator=(const JavaString& partner)
{
  if (this != &partner) {
    if (mChars) {
      mEnvironment->ReleaseStringUTFChars(mJavaString, mChars);
    }
    mEnvironment = partner.mEnvironment;
    mJavaString = partner.mJavaString;
    mChars = 0;
  }
  return *this;
}


//###########################################################################
//# JavaString: Auxiliary Methods

int JavaString::
length()
  const
{
  return mEnvironment->GetStringUTFLength(mJavaString);
}

JavaString::
operator const char* ()
  const
{
  if (mChars == 0) {
    mChars = mEnvironment->GetStringUTFChars(mJavaString, 0);
  }
  return mChars;    
}


}  /* namespace jni */
