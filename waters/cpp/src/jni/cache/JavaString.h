//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   JavaString
//###########################################################################
//# $Id: JavaString.h,v 1.1 2006-09-03 06:38:42 robi Exp $
//###########################################################################


#ifndef _JavaString_h_
#define _JavaString_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>


namespace jni {


//###########################################################################
//# Class JavaString
//###########################################################################
//# A simple wrapper for the JNI's jstring that allows easy access to
//# the string contents combined with transparent deletion of the buffer
//# when the object loses its scope. 
//###########################################################################

class JavaString {
public:
  //#########################################################################
  //# Constructors, Destructors & Co.
  explicit JavaString(JNIEnv* env, jstring javastring);
  JavaString(const JavaString& partner);
  ~JavaString();
  JavaString& operator=(const JavaString& partner);

  //#########################################################################
  //# Simple Access
  jstring getJavaString() const {return mJavaString;}
  int length() const;
  operator const char*() const;

  //#########################################################################
  //# Modifications
  JavaString& operator+=(const char* utf);

private:
  //#########################################################################
  //# Data Members
  JNIEnv* mEnvironment;
  jstring mJavaString;
  mutable const char* mChars;
};

}   /* namespace jni */

#endif  /* !_JavaString_h_ */
