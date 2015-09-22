//# This may look like C code, but it really is -*- C++ -*-
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

#ifndef _PreAnalysisConfigurationException_h_
#define _PreAnalysisConfigurationException_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "jni/cache/PreJavaException.h"
#include "jni/glue/ConflictCheckModeGlue.h"
#include "waters/analysis/CheckType.h"


namespace jni {

class ClassCache;


//###########################################################################
//# Class PreAnalysisConfigurationException
//###########################################################################
//# A data structure containing the information for an analysis configuration
//# exception to be thrown from native code. This is thrown as a C++ exception
//# to be converted to a proper AnalysisConfigurationException for Java as
//# soon as the environment becomes available.
//###########################################################################

class PreAnalysisConfigurationException : public PreJavaException
{
public:
  //#########################################################################
  //# Constructors, Destructors & Co.
  explicit PreAnalysisConfigurationException(ConflictCheckMode mode);
  explicit PreAnalysisConfigurationException(waters::CheckType type);
  PreAnalysisConfigurationException(const char* description, int value);
};

}   /* namespace jni */

#endif  /* !_PreAnalysisConfigurationException_h_ */








