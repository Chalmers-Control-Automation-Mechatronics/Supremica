//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectReference
//###########################################################################
//# $Id: ObjectReference.h,v 1.2 2006-08-20 11:02:43 robi Exp $
//###########################################################################


#ifndef _ObjectReference_h_
#define _ObjectReference_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "waters/base/IntTypes.h"


namespace jni {

class ClassCache;
class ClassGlue;


//###########################################################################
//# Class ObjectReference
//###########################################################################

class ObjectReference {
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ObjectReference(waters::uint32 classcode, ClassCache* cache);
  explicit ObjectReference(jobject javaobject,
			   waters::uint32 classcode,
			   ClassCache* cache,
			   bool global = false);
  ~ObjectReference();

  //##########################################################################
  //# Access
  ClassGlue* getClass() const {return mClass;};
  jobject getJavaObject() const {return mJavaObject;};
  jobject returnJavaObject();
  void initJavaObject(jobject javaobject);
  waters::uint32 addReference();
  waters::uint32 removeReference();

private:
  //##########################################################################
  //# Data Members
  ClassGlue* mClass;
  jobject mJavaObject;
  waters::uint32 mRefCount;
};

}   /* namespace jni */

#endif  /* !_ObjectReference_h_ */
