//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   PreAnalysisConfigurationException
//###########################################################################
//# $Id$
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
