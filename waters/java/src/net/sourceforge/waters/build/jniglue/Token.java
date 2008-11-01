//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   Token
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class Token {

  //#########################################################################
  //# Constructors
  Token(final int type, final String text)
  {
    mTokenType = type;
    mTokenText = text;
  }


  //#########################################################################
  //# Equals and HashCode
  public boolean equals(final Object partner)
  {
    if (partner != null && partner.getClass() == getClass()) {
      final Token token = (Token) partner;
      return mTokenType == token.mTokenType;
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mTokenType;
  }


  //#########################################################################
  //# Simple Access
  int getTokenType()
  {
    return mTokenType;
  }

  String getTokenText()
  {
    return mTokenText;
  }


  //#########################################################################
  //# Data Members
  private final int mTokenType;
  private final String mTokenText;

}