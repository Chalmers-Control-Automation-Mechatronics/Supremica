//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.build.jniglue;

import java.util.Set;


class SimpleTypeGlue extends TypeGlue {

  //#########################################################################
  //# Class Constants
  private static final int C_VOID = 0;
  private static final int C_BOOLEAN = 1;
  private static final int C_CHAR = 2;
  private static final int C_DOUBLE = 3;
  private static final int C_INT = 4;
  private static final int C_LONG = 5;
  @SuppressWarnings("unused")
  private static final int C_OBJECT = 6;
  private static final int C_STRING = 7;

  static final SimpleTypeGlue TYPE_VOID =
    new SimpleTypeGlue(C_VOID, "void", "void", "void",
		       "V", "Void", void.class);
  static final SimpleTypeGlue TYPE_BOOLEAN =
    new SimpleTypeGlue(C_BOOLEAN, "boolean", "bool", "jboolean",
		       "Z", "Boolean", boolean.class);
  static final SimpleTypeGlue TYPE_CHAR =
    new SimpleTypeGlue(C_CHAR, "char", "char", "jchar",
		       "C", "Char", char.class);
  static final SimpleTypeGlue TYPE_DOUBLE =
    new SimpleTypeGlue(C_DOUBLE, "double", "double", "jdouble",
		       "D", "Double", double.class);
  static final SimpleTypeGlue TYPE_INT =
    new SimpleTypeGlue(C_INT, "int", "int", "jint",
               "I", "Int", int.class);
  static final SimpleTypeGlue TYPE_LONG =
    new SimpleTypeGlue(C_LONG, "long", "jlong", "jlong",
                       "J", "Long", long.class);
  static final SimpleTypeGlue TYPE_STRING =
    new SimpleTypeGlue(C_STRING, "String", "jstring", "jstring",
		       "Ljava/lang/String;", "Object", String.class);


  //#########################################################################
  //# Constructors
  SimpleTypeGlue(final int code,
		 final String name,
		 final String cppname,
		 final String jniname,
		 final String signature,
		 final String jnicall,
		 final Class<?> javaclass)
  {
    mCode = code;
    mName = name;
    mCppName = cppname;
    mJNIName = jniname;
    mSignature = signature;
    mJNICallPart = jnicall;
    mJavaClass = javaclass;
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final SimpleTypeGlue type = (SimpleTypeGlue) partner;
      return mCode == type.mCode;
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mCode;
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final TypeGlue partner)
  {
    if (partner instanceof SimpleTypeGlue) {
      final SimpleTypeGlue simpletype = (SimpleTypeGlue) partner;
      return mCode - simpletype.mCode;
    } else {
      return -1;
    }
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  String getCppTypeName()
  {
    return mCppName;
  }

  String getJNITypeName()
  {
    return mJNIName;
  }

  String getGlueTypeName()
  {
    return getCppTypeName();
  }

  void appendTypeSignature(final StringBuilder buffer)
  {
    buffer.append(mSignature);
  }

  String getJNICallPart()
  {
    return mJNICallPart;
  }

  Class<?> getJavaClass()
  {
    return mJavaClass;
  }


  //#########################################################################
  //# Type Flags
  boolean isVoid()
  {
    return mCode == C_VOID;
  }

  boolean isBoolean()
  {
    return mCode == C_BOOLEAN;
  }

  boolean isString()
  {
    return mCode == C_STRING;
  }

  boolean isGlue()
  {
    return false;
  }

  boolean isEnum()
  {
    return false;
  }


  //#########################################################################
  //# Calculating Dependencies
  void collectUsedGlue(final Set<ClassGlue> used)
  {
  }


  //#########################################################################
  //# Data Members
  private final int mCode;
  private final String mName;
  private final String mCppName;
  private final String mJNIName;
  private final String mSignature;
  private final String mJNICallPart;
  private final Class<?> mJavaClass;

}
