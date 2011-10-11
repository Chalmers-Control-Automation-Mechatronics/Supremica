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

#include <stdint.h>


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
  explicit ObjectReference(uint32_t classcode, ClassCache* cache);
  explicit ObjectReference(jobject javaobject,
			   uint32_t classcode,
			   ClassCache* cache,
			   bool global = false);
  ~ObjectReference();

  //##########################################################################
  //# Access
  ClassGlue* getClass() const {return mClass;};
  jobject getJavaObject() const {return mJavaObject;};
  jobject returnJavaObject();
  void initJavaObject(jobject javaobject);
  uint32_t addReference();
  uint32_t removeReference();

private:
  //##########################################################################
  //# Data Members
  ClassGlue* mClass;
  jobject mJavaObject;
  uint32_t mRefCount;
};

}   /* namespace jni */

#endif  /* !_ObjectReference_h_ */
