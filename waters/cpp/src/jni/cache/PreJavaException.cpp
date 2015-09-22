//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <string.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreJavaException.h"


namespace jni {


//###########################################################################
//# Class PreJavaException
//###########################################################################

//###########################################################################
//# PreJavaException: Constructors, Destructors & Co.

PreJavaException::
PreJavaException()
  : mClassCode(UINT32_MAX)
{
  initMessage(0,true);
}

PreJavaException::
PreJavaException(uint32_t classcode)
  : mClassCode(classcode)
{
  initMessage(0, true);
}

PreJavaException::
PreJavaException(uint32_t classcode,
                 const char* msg,
                 bool staticString)
  : mClassCode(classcode)
{
  initMessage(msg, staticString);
}

PreJavaException::
PreJavaException(const PreJavaException& partner)
  : mClassCode(partner.mClassCode)
{
  initMessage(partner.mMessage, partner.mStaticString);
}

PreJavaException::
~PreJavaException()
{
  if (!mStaticString) {
    delete [] mMessage;
  }
}

PreJavaException& PreJavaException::
operator=(const PreJavaException& partner)
{
  if (this != &partner) {
    if (!mStaticString) {
      delete [] mMessage;
    }
    mClassCode = partner.mClassCode;
    initMessage(partner.mMessage, partner.mStaticString);
  }
  return *this;
}


//###########################################################################
//# PreJavaException: Throwing Exceptions

jint PreJavaException::
throwJavaException(ClassCache& cache)
  const
{
  return cache.throwJavaException(mClassCode, mMessage);
}


//###########################################################################
//# PreJavaException: Auxiliary Methods

void PreJavaException::
initMessage(const char* msg, bool staticString)
{
  if ( (mStaticString = staticString) ) {
    mMessage = (char*) msg;
  } else {
    int len = strlen(msg);
    mMessage = new char[len + 1];
    strcpy(mMessage, msg);
  }
}


}  /* namespace jni */








