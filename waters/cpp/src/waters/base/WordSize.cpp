//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   WordSize
//###########################################################################
//# $Id$
//###########################################################################


#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>

#include <jni.h>

#include "waters/base/WordSize.h"
#include "waters/javah/Invocations.h"


JNIEXPORT jboolean JNICALL
Java_net_sourceforge_waters_cpp_analysis_WordSizeTest_nativeWordSizeTest
  (JNIEnv *env, jclass /* none */)
{
  if (__WORDSIZE == 8 * sizeof(void*)) {
    return JNI_TRUE;
  } else {
    std::cerr << "__WORDSIZE = " << __WORDSIZE << " bits ("
	      << (__WORDSIZE / 8) << " bytes)" << std::endl;
    std::cerr << "sizeof(void*) = " << (8 * sizeof(void*)) << " bits ("
	      << sizeof(void*) << " bytes)" << std::endl;
    return JNI_FALSE;
  }
}

