//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassModifierToken
//###########################################################################
//# $Id: ClassModifierToken.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class ClassModifierToken extends Token {

  //#########################################################################
  //# Constructors
  ClassModifierToken(final int type,
		     final String text,
		     final ClassModifier mod)
  {
    super(type, text);
    mClassModifier = mod;
  }


  //#########################################################################
  //# Simple Access
  ClassModifier getClassModifier()
  {
    return mClassModifier;
  }


  //#########################################################################
  //# Data Members
  private final ClassModifier mClassModifier;

}