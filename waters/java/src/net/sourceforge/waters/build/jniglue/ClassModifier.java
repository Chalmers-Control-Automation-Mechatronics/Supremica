//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassModifier
//###########################################################################
//# $Id: ClassModifier.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class ClassModifier {

  //#########################################################################
  //# Constructors
  private ClassModifier(final int modtype)
  {
    mModifierType = modtype;
  }


  //#########################################################################
  //# Simple Access
  boolean includesGlueHeaders()
  {
    return mModifierType >= T_REF;
  }

  boolean includesFullImplementation()
  {
    return mModifierType >= T_GLUE;
  }


  //#########################################################################
  //# Data Members
  private final int mModifierType;


  //#########################################################################
  //# Class Constants
  private static final int T_ARG = 0;
  private static final int T_REF = 1;
  private static final int T_GLUE = 2;  

  static final ClassModifier M_ARG = new ClassModifier(T_ARG);
  static final ClassModifier M_REF = new ClassModifier(T_REF);
  static final ClassModifier M_GLUE = new ClassModifier(T_GLUE);

}