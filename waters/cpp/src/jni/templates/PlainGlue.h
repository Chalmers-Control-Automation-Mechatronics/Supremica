//# This may look like C code, but it really is -*- C++ -*-
$-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.templates
//# CLASS:   Template for a plain glue class header file
//###########################################################################
//# $Id: PlainGlue.h,v 1.5 2006-08-21 05:41:39 robi Exp $
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


#ifndef _$CPPCLASSNAME_h_
#define _$CPPCLASSNAME_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

$IF-HASBASECLASS
#include "jni/glue/$CPPBASECLASSNAME.h"
$ELSE
#include "jni/cache/ObjectBase.h"
$ENDIF
$FOREACH-INCLUDEDGLUE
$IF-ENUM
#include "jni/glue/$CPPCLASSNAME.h"
$ENDIF
$ENDFOR


namespace jni {

class ClassCache;
$FOREACH-INCLUDEDGLUE
$IF-ENUM$ELSE
class $CPPCLASSNAME;
$ENDIF
$ENDFOR


$IF-ENUM
//############################################################################
//# enum $CLASSNAME$
//############################################################################

enum $CLASSNAME$ {
$FOREACH-FIELD
  $CLASSNAME_$FIELDNAME$ = $INDEX$COMMA$
$ENDFOR
};


$ENDIF
//############################################################################
//# class $CPPCLASSNAME$
//############################################################################

class $CPPCLASSNAME$ : $=
  public $IF-HASBASECLASS $CPPBASECLASSNAME $ELSE ObjectBase$ENDIF$
{
public:
$IF-ENUM
  //##########################################################################
  //# Enumeration Type Conversion
  static $CLASSNAME$ toEnum(jobject javaobject, ClassCache* cache);
  static jobject toJavaObject($CLASSNAME$ item, ClassCache* cache);

$ENDIF
  //##########################################################################
  //# Constructors
  explicit $CPPCLASSNAME(jobject javaobject,
           $CSPC       $ ClassCache* cache,
           $CSPC       $ bool global = false);
  $CPPCLASSNAME(const $CPPCLASSNAME& partner);
$IF-ENUM
  explicit $CPPCLASSNAME($CLASSNAME$ item, ClassCache* cache);
$ENDIF
$FOREACH-CONSTRUCTOR
  explicit $CPPCLASSNAME($FOREACH-ARG$=
                         $IF-ENUM$=
                           $JAVATYPENAME$=
                         $ELSEIF-GLUE$=
                           const $GLUETYPENAME*$=
                         $ELSE$=
                           $CPPTYPENAME$=
                         $ENDIF$ $ARGNAME,
           $CSPC       $ $ENDFOR$=
                         ClassCache* cache);
$ENDFOR
$IF-HASVIRTUALMETHODS

  //##########################################################################
  //# Java Member Functions
$FOREACH-VIRTUALMETHOD
  $CPPTYPENAME$ $METHODNAME($FOREACH-ARG$=
                            $IF-GLUE const $ENDIF$=
                            $GLUETYPENAME$IF-GLUE*$ENDIF$ $ARGNAME $IF-HASNEXT,
  $TSPC       $ $MSPC     $ $ENDIF $ENDFOR) const;
$IF-GLUE
  $IF-ENUM $JAVATYPENAME $ELSE $GLUETYPENAME $ENDIF$ $=
  $METHODNAME Glue($FOREACH-ARG$=
                                  $IF-GLUE const $ENDIF$=
                                    $GLUETYPENAME $IF-GLUE*$ENDIF$ $ARGNAME,
  $^IF-ENUM $^JSPC $^ELSE $^GSPC $^ENDIF$=
               $ $MSPC      $     $ENDFOR$=
                                  ClassCache* cache) const;
$ENDIF
$ENDFOR
$ENDIF
$IF-HASSTATICMETHODS

  //##########################################################################
  //# Java Static Functions
$FOREACH-STATICMETHOD
  static $CPPTYPENAME$ $METHODNAME($FOREACH-ARG$=
				   $IF-GLUE const $ENDIF$=
				   $GLUETYPENAME$IF-GLUE*$ENDIF$ $ARGNAME,
         $TSPC       $ $MSPC     $ $ENDFOR$=
                                   ClassCache* cache);
$IF-GLUE
  static $IF-ENUM $JAVATYPENAME $ELSE $GLUETYPENAME $ENDIF$ $=
  $METHODNAME Glue($FOREACH-ARG$=
                                  $IF-GLUE const $ENDIF$=
                                    $GLUETYPENAME $IF-GLUE*$ENDIF$ $ARGNAME,
  $^IF-ENUM $^JSPC $^ELSE $^GSPC $^ENDIF$=
                      $ $MSPC      $     $ENDFOR$=
                                  ClassCache* cache);
$ENDIF
$ENDFOR
$ENDIF
$IF-HASSUBCLASSES

protected:
  //##########################################################################
  //# Protected Constructors
  explicit $CPPCLASSNAME(waters::uint32 classcode, ClassCache* cache);
  explicit $CPPCLASSNAME(jobject javaobject,
           $CSPC       $ waters::uint32 classcode,
           $CSPC       $ ClassCache* cache,
           $CSPC       $ bool global = false);
$ENDIF
};

}   /* namespace jni */

#endif  /* !_$CPPCLASSNAME_h_ */
