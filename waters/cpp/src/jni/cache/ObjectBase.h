//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   ObjectBase
//###########################################################################
//# $Id: ObjectBase.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _ObjectBase_h_
#define _ObjectBase_h_

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


//############################################################################
//# class ObjectBase
//############################################################################

class ObjectBase
{
public:
  //##########################################################################
  //# Destructor
  ~ObjectBase();

  //##########################################################################
  //# Access
  jobject getJavaObject() const {return mJavaObject;};
  jobject returnJavaObject();
  ClassGlue* getClass() const {return mClass;};

protected:
  //##########################################################################
  //# Protected Constructors
  explicit ObjectBase(waters::uint32 classcode, ClassCache* cache);
  explicit ObjectBase(jobject javaobject,
                      waters::uint32 classcode,
                      ClassCache* cache);

  //##########################################################################
  //# Access
  void initJavaObject(jobject javaobject) {mJavaObject = javaobject;};

private:
  //##########################################################################
  //# Data Members
  ClassGlue* mClass;
  jobject mJavaObject;

};

}   /* namespace jni */

#endif  /* !_ObjectBase_h_ */
