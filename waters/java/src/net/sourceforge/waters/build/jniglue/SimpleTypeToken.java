//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   SimpleTypeToken
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class SimpleTypeToken extends Token {

  //#########################################################################
  //# Constructors
  SimpleTypeToken(final SimpleTypeGlue glue)
  {
    super(TokenTable.C_SIMPLETYPE, glue.getName());
    mTypeGlue = glue;
  }


  //#########################################################################
  //# Simple Access
  SimpleTypeGlue getTypeGlue()
  {
    return mTypeGlue;
  }


  //#########################################################################
  //# Data Members
  private final SimpleTypeGlue mTypeGlue;

}
