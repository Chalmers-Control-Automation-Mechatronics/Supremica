//# -*- indent-tabs-mode: nil -*-
$-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.templates
//# CLASS:   Template for Glue.cpp header file
//###########################################################################
//# $Id$
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

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassInfo.h"
#include "jni/cache/MethodInfo.h"
#include "jni/glue/Glue.h"


namespace jni {

//###########################################################################
//# Method Names
//###########################################################################

const char* JAVANAME__init = "<init>";
$FOREACH-PLAINMETHODNAME
const char* JAVANAME_$METHODNAME$ = "$METHODNAME";
$ENDFOR

$FOREACH-TYPESIGNATURE
const char* TYPESIG_$TYPESIGCODE$ = "$TYPESIGNATURE";
$ENDFOR


//###########################################################################
//# Method Information
//###########################################################################

$FOREACH-CLASS
$IF-HASMEMBERS
const MethodInfo METHODINFO_$CLASSNAME[] = {
$FOREACH-METHOD
  MethodInfo(METHOD_$CLASSNAME_$METHODCODENAME,
             JAVANAME_$IF-ISCONSTRUCTOR _init$ELSE $METHODNAME $ENDIF,
             TYPESIG_$TYPESIGCODE)$=
  $IF-HASFIELDS,$ELSE$COMMA$ENDIF$
$ENDFOR
$FOREACH-FIELD
  MethodInfo($INDEX,
             JAVANAME_$FIELDNAME,
             TYPESIG_$TYPESIGCODE)$COMMA$
$ENDFOR
};

$ENDIF
$ENDFOR

//###########################################################################
//# Class Information
//###########################################################################

const ClassInfo CLASSINFO[] = {
$FOREACH-CLASS
$IF-REF
  ClassInfo(CLASS_$CLASSNAME,
            "$FULLCLASSNAME",
            $IF-HASBASECLASS &CLASSINFO[CLASS_$BASECLASSNAME] $ELSE 0$ENDIF,
            $NUMMETHODS,
            $NUMFIELDS,
            $IF-HASMEMBERS METHODINFO_$CLASSNAME $ELSE 0$ENDIF)$COMMA$
$ENDIF
$ENDFOR
};

}  /* namespace jni */
