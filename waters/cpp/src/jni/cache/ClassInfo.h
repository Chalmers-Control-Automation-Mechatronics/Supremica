//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

#ifndef _ClassInfo_h_
#define _ClassInfo_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "jni/cache/MethodInfo.h"
#include <stdint.h>


namespace jni {


//###########################################################################
//# Class ClassInfo
//###########################################################################

class ClassInfo {
public:
  //#########################################################################
  //# Constructors
  explicit ClassInfo(uint32_t code,
                     const char* name, 
                     const ClassInfo* baseclass,
                     uint32_t nummethods,
                     uint32_t numfields,
                     const MethodInfo* methods);

  //#########################################################################
  //# Simple Access
  const uint32_t getClassCode() const {return mCode;};
  const char* getName() const {return mName;};
  uint32_t getFirstCode() const;
  uint32_t getNumMethods() const {return mNumMethods;};
  const MethodInfo* getMethodInfo(uint32_t methodcode) const;
  const uint32_t getNumFields() const {return mNumFields;};
  const MethodInfo* getFieldInfo(uint32_t fieldcode) const;

private:
  //#########################################################################
  //# Data Members
  uint32_t mCode;
  const char* mName;
  const ClassInfo* mBaseClass;
  uint32_t mNumMethods;
  uint32_t mNumFields;
  const MethodInfo* mMethodInfo;
};

}   /* namespace jni */

#endif  /* !_ClassInfo_h_ */
