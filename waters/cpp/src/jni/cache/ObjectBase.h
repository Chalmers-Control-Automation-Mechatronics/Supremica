//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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

#include <stdint.h>


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
  explicit ObjectBase(uint32_t classcode, ClassCache* cache);
  explicit ObjectBase(jobject javaobject,
                      uint32_t classcode,
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
