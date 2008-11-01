//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   IdentifierToken
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class IdentifierToken extends Token {

  //#########################################################################
  //# Constructors
  IdentifierToken(final int type, final String text)
  {
    super(type, text);
  }


  //#########################################################################
  //# Equals and HashCode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IdentifierToken token = (IdentifierToken) partner;
      return getTokenText().equals(token.getTokenText());
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return 5 * super.hashCode() + getTokenText().hashCode();
  }

}