//# This may look like C code, but it really is -*- C++ -*-
$-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.templates
//# CLASS:   Template for Glue.h header file
//###########################################################################
//# $Id: Glue.h 4707 2009-05-20 22:45:16Z robi $
//###########################################################################

$+
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.glue
//# CLASS:   <Class and Method Codes>
//###########################################################################
//# Automatically generated---do not edit.
//# Source: $INPUTFILE$
//###########################################################################

#ifndef _Glue_h_
#define _Glue_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>


namespace jni {

class ClassInfo;
  

//###########################################################################
//# Class Codes and Names
//###########################################################################

$FOREACH-CLASS
$IF-REF
const uint32_t CLASS_$CLASSNAME$ = $INDEX;
$ENDIF
$ENDFOR

const uint32_t CLASS_COUNT = $COUNT;

extern const ClassInfo CLASSINFO[];


//###########################################################################
//# Method Codes
//###########################################################################

$FOREACH-CLASS
$IF-HASMETHODS
$FOREACH-METHOD
const uint32_t METHOD_$CLASSNAME_$METHODCODENAME$ = $METHODNO;
$ENDFOR

$ENDIF
$ENDFOR
}   /* namespace jni */

#endif  /* !_Glue_h_ */
