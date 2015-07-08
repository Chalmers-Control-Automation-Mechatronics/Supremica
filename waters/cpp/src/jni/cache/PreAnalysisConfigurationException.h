//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.javah
//# CLASS:   PreAnalysisConfigurationException
//###########################################################################
//# $Id$
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
