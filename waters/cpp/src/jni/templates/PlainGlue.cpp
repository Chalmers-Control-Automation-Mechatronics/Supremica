//# -*- indent-tabs-mode: nil -*-
$-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.templates
//# CLASS:   Template for a plain glue class implementation file
//###########################################################################
//# $Id: PlainGlue.cpp,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

$+
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.glue
//# CLASS:   $CPPCLASSNAME$
//###########################################################################
//# Automatically generated---do not edit.
//# Source: $INPUTFILE$
//###########################################################################


#ifdef __GNUG__
#pragma implementation
#endif

$IF-HASCONSTRUCTORS
#include "jni/cache/ClassCache.h"
$ELSEIF-HASGLUERESULTS
#include "jni/cache/ClassCache.h"
$ELSEIF-ENUM
#include "jni/cache/ClassCache.h"
$ENDIF
#include "jni/cache/ClassGlue.h"
#include "jni/glue/Glue.h"
#include "jni/glue/$CPPCLASSNAME.h"
$FOREACH-INCLUDEDGLUE
$IF-ENUM$ELSE
#include "jni/glue/$CPPCLASSNAME.h"
$ENDIF
$ENDFOR

namespace jni {


//###########################################################################
//# Class $CPPCLASSNAME$
//###########################################################################

$IF-ENUM
//############################################################################
//# $CPPCLASSNAME: Enumeration Type Conversion

$CLASSNAME$ $CPPCLASSNAME::
toEnum(jobject javaobject, ClassCache* cache)
{
  ClassGlue* cls = cache->getClass(CLASS_$CLASSNAME);
  JNIEnv* env = cls->getEnvironment();
  jmethodID mid = cls->getMethodID(METHOD_Object_equals);
  for (waters::uint32 fieldcode = 0; fieldcode < $NUMFIELDS; fieldcode++) {
    jobject item = cls->getStaticFinalField(fieldcode);
    jboolean eq = env->CallBooleanMethod(javaobject, mid, item);
    if (eq != JNI_FALSE) {
      return ($CLASSNAME) fieldcode;
    }
  }
  cache->throwJavaException(CLASS_IllegalArgumentException,
                            "Unknown object for enumeration $CLASSNAME!");
  return ($CLASSNAME) -1;
}


jobject $CPPCLASSNAME::
toJavaObject($CLASSNAME$ item, ClassCache* cache)
{
  waters::uint32 fieldcode = (waters::uint32) item;
  ClassGlue* cls = cache->getClass(CLASS_$CLASSNAME);
  return cls->getStaticFinalField(fieldcode);
}


$ENDIF
//###########################################################################
//# $CPPCLASSNAME: Constructors

$IF-HASSUBCLASSES
$CPPCLASSNAME::
$CPPCLASSNAME(waters::uint32 classcode, ClassCache* cache)
  : $IF-HASBASECLASS $CPPBASECLASSNAME $ELSE ObjectBase$ENDIF$=
      (classcode, cache)
{
}


$CPPCLASSNAME::
$CPPCLASSNAME(jobject javaobject, waters::uint32 classcode, ClassCache* cache)
  : $IF-HASBASECLASS $CPPBASECLASSNAME $ELSE ObjectBase$ENDIF$=
      (javaobject, classcode, cache)
{
}


$ENDIF
$CPPCLASSNAME::
$CPPCLASSNAME(jobject javaobject, ClassCache* cache)
  : $IF-HASBASECLASS $CPPBASECLASSNAME $ELSE ObjectBase$ENDIF$=
      (javaobject, CLASS_$CLASSNAME, cache)
{
}


$IF-ENUM
$CPPCLASSNAME::
$CPPCLASSNAME($CLASSNAME$ item, ClassCache* cache)
  : $IF-HASBASECLASS $CPPBASECLASSNAME $ELSE ObjectBase$ENDIF$=
      (CLASS_$CLASSNAME, cache)
{
  waters::uint32 fieldcode = (waters::uint32) item;
  ClassGlue* cls = getClass();
  jobject javaobject = cls->getStaticFinalField(fieldcode);
  initJavaObject(javaobject);
}


$ENDIF
$FOREACH-CONSTRUCTOR
$CPPCLASSNAME::
$CPPCLASSNAME($FOREACH-ARG$=
              $IF-ENUM$=
                $JAVATYPENAME$=
              $ELSEIF-GLUE$=
                const $GLUETYPENAME*$=
              $ELSE$=
		$CPPTYPENAME$=
              $ENDIF$ arg_$ARGNAME,
$CSPC       $ $ENDFOR$=
              ClassCache* cache)
  : $IF-HASBASECLASS $CPPBASECLASSNAME $ELSE ObjectBase$ENDIF$=
      (CLASS_$CLASSNAME, cache)
{
  ClassGlue* cls = getClass();
  JNIEnv* env = cls->getEnvironment();
  jmethodID mid = cls->getMethodID(METHOD_$CLASSNAME_$METHODCODENAME);
  jclass javaclass = cls->getJavaClass();
$FOREACH-ARG
$IF-GLUE
$IF-ENUM
  jobject obj_$ARGNAME = $GLUETYPENAME::toJavaObject(arg_$ARGNAME, cache);
$ELSE
  jobject obj_$ARGNAME = arg_$ARGNAME->getJavaObject();
$ENDIF
$ENDIF
$ENDFOR
  jobject javaobject = env->NewObject(javaclass, mid$=
    $FOREACH-ARG, $IF-GLUE obj$ELSE arg$ENDIF _$ARGNAME $ENDFOR);
  if (jthrowable exception = env->ExceptionOccurred()) {
    throw exception;
  }
  initJavaObject(javaobject);
}


$ENDFOR
$IF-HASPLAINMETHODS
//############################################################################
//# $CPPCLASSNAME: Java Methods

$FOREACH-PLAINMETHOD
$CPPTYPENAME$ $CPPCLASSNAME::
$METHODNAME($FOREACH-ARG$=
            $IF-GLUE const $ENDIF$=
              $GLUETYPENAME $IF-GLUE*$ENDIF$ arg_$ARGNAME $IF-HASNEXT,
$MSPC     $ $ENDIF $ENDFOR)
  const
{
  ClassGlue* cls = getClass();
  JNIEnv* env = cls->getEnvironment();
  jmethodID mid = cls->getMethodID(METHOD_$CLASSNAME_$METHODCODENAME);
  jobject javaobject = getJavaObject();
$FOREACH-ARG
$IF-GLUE
  jobject obj_$ARGNAME = arg_$ARGNAME->getJavaObject();
$ENDIF
$ENDFOR
  $IF-NONVOID $JNITYPENAME$ result = $IF-STRING(jstring) $ENDIF $ENDIF $=
    env->$JNICALLNAME(javaobject, mid$=
      $FOREACH-ARG, $IF-GLUE obj$ELSE arg$ENDIF _$ARGNAME $ENDFOR);
  if (jthrowable exception = env->ExceptionOccurred()) {
    throw exception;
  }
$IF-NONVOID
  return result$IF-BOOLEAN$ != JNI_FALSE$ENDIF;
$ENDIF
}


$IF-GLUE
$IF-ENUM $JAVATYPENAME $ELSE $GLUETYPENAME $ENDIF$ $=
$CPPCLASSNAME::
$METHODNAME Glue($FOREACH-ARG$=
                 $IF-GLUE const $ENDIF$=
                   $GLUETYPENAME $IF-GLUE*$ENDIF$ arg_$ARGNAME,
$MSPC      $     $ENDFOR ClassCache* cache)
  const
{
  jobject result = $METHODNAME($FOREACH-ARG arg_$ARGNAME $COMMASP $ENDFOR);
$IF-ENUM
  return $GLUETYPENAME::toEnum(result, cache);
$ELSE
  return $GLUETYPENAME(result, cache);
$ENDIF
}  
  

$ENDIF
$ENDFOR
$ENDIF
}  /* namespace jni */
