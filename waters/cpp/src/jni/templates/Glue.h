//# This may look like C code, but it really is -*- C++ -*-
$-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.templates
//# CLASS:   Template for Glue.h header file
//###########################################################################
//# $Id: Glue.h,v 1.1 2005-02-18 01:30:10 robi Exp $
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

#include "waters/base/IntTypes.h"


namespace jni {

class ClassInfo;
  

//###########################################################################
//# Class Codes and Names
//###########################################################################

$FOREACH-CLASS
$IF-REF
const waters::uint32 CLASS_$CLASSNAME$ = $INDEX;
$ENDIF
$ENDFOR

const waters::uint32 CLASS_COUNT = $COUNT;

extern const ClassInfo CLASSINFO[];


//###########################################################################
//# Method Codes
//###########################################################################

$FOREACH-CLASS
$IF-HASMETHODS
$FOREACH-METHOD
const waters::uint32 METHOD_$CLASSNAME_$METHODCODENAME$ = $METHODNO;
$ENDFOR

$ENDIF
$ENDFOR
}   /* namespace jni */

#endif  /* !_Glue_h_ */
