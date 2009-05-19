//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   ObjectBase
//###########################################################################
//# $Id: ObjectBase.h,v 1.6 2006-08-21 05:41:39 robi Exp $
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
class ObjectReference;


//############################################################################
//# class ObjectBase
//############################################################################

class ObjectBase
{
public:
  //##########################################################################
  //# Access
  bool isNull() const {return mObjectReference == 0;}
  bool isSameObject(const ObjectBase& other, const ClassCache* cache) const;
  jobject getJavaObject() const;
  jobject returnJavaObject() const;
  ClassGlue* getClass() const;

  //##########################################################################
  //# Error Handling
  void checkNonNull() const;

protected:
  //##########################################################################
  //# Constructors, Destructors & Co.
  explicit ObjectBase(waters::uint32 classcode, ClassCache* cache);
  explicit ObjectBase(jobject javaobject,
                      waters::uint32 classcode,
                      ClassCache* cache,
		      bool global = false);
  ObjectBase(const ObjectBase& partner);
  ~ObjectBase();
  ObjectBase& operator= (const ObjectBase& partner);

  //##########################################################################
  //# Protected Access
  void initJavaObject(jobject javaobject);

private:
  //##########################################################################
  //# Data Members
  ObjectReference* mObjectReference;
};

}   /* namespace jni */

#endif  /* !_ObjectBase_h_ */
