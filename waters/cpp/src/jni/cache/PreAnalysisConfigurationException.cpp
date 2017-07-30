//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

#include <string.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreAnalysisConfigurationException.h"
#include "jni/glue/Glue.h"
#include "jni/glue/AnalysisConfigurationExceptionGlue.h"


namespace jni {


//###########################################################################
//# Class PreAnalysisConfigurationException
//###########################################################################

//###########################################################################
//# PreAnalysisConfigurationException: Constructors, Destructors & Co.

PreAnalysisConfigurationException::
PreAnalysisConfigurationException(ConflictCheckMode mode)
  : PreJavaException(CLASS_AnalysisConfigurationException)
{
  char buffer[64];
  sprintf(buffer, "Unknown conflict check mode 0x%x!", mode);
  initMessage(buffer, false);
}

PreAnalysisConfigurationException::
PreAnalysisConfigurationException(waters::CheckType type)
  : PreJavaException(CLASS_AnalysisConfigurationException)
{
  char buffer[64];
  sprintf(buffer, "Unknown check type 0x%x!", type);
  initMessage(buffer, false);
}

PreAnalysisConfigurationException::
PreAnalysisConfigurationException(const char* description, int value)
  : PreJavaException(CLASS_AnalysisConfigurationException)
{
  char buffer[strlen(description) + 16];
  sprintf(buffer, "Unknown %s 0x%x!", description, value);
  initMessage(buffer, false);  
}


}  /* namespace jni */
