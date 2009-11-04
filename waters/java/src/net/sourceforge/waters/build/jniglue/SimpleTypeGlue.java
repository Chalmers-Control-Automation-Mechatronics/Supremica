//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   SimpleTypeGlue
//###########################################################################
//# $Id$
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
  @SuppressWarnings("unused")
  private static final int C_OBJECT = 5;
  private static final int C_STRING = 6;

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

  void appendTypeSignature(final StringBuffer buffer)
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
