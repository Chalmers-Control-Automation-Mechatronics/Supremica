//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Import/Export
//# PACKAGE: net.sourceforge.waters.external.susyna
//# CLASS:   SusynaToken
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.susyna;


/**
 * @author Robi Malik
 */

class SusynaToken
{

  //#######################################################################
  //# Factory Methods
  static SusynaToken getToken(final Type type)
  {
    return type.getToken();
  }


  //#######################################################################
  //# Constructors
  SusynaToken(final Type type, final String text)
  {
    mTokenType = type;
    mText = text;
  }

  SusynaToken(final Type type, final StringBuffer buffer)
  {
    this(type, buffer.toString());
  }


  //#######################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    if (mTokenType.getToken() == this) {
      return mTokenType.toString();
    } else {
      return mText;
    }
  }


  //#######################################################################
  //# Simple Access
  Type getTokenType()
  {
    return mTokenType;
  }

  String getText()
  {
    return mText;
  }

  int getValue()
  {
    return Integer.parseInt(mText);
  }


  //#########################################################################
  //# Inner Enumeration Type
  static enum Type
  {
    OPENBR("("),
    CLOSEBR(")"),
    COMMA(","),
    EQUALS("="),
    IDENTIFIER,
    INTCONST,
    HEADER,
    EOF("");

    //#######################################################################
    //# Constructors
    private Type()
    {
      mToken = null;
    }

    private Type(final String text)
    {
      mToken = new SusynaToken(this, text);
    }

    //#######################################################################
    //# Simple Access
    private SusynaToken getToken()
    {
      return mToken;
    }

    //#######################################################################
    //# Data Members
    private SusynaToken mToken;
  }


  //#######################################################################
  //# Data Members
  private final Type mTokenType;
  private final String mText;

}
