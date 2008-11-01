//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateProcessorKey
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class TemplateProcessorKey {

  //#########################################################################
  //# Constructors
  TemplateProcessorKey(final int kind, final String name)
  {
    mKind = kind;
    mName = name;
  }


  //#########################################################################
  //# Equals and HashCode
  public boolean equals(final Object partner)
  {
    if (partner != null && partner.getClass() == getClass()) {
      final TemplateProcessorKey key = (TemplateProcessorKey) partner;
      return mKind == key.mKind && mName.equals(key.mName);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mKind + 5 * mName.hashCode();
  }


  //#########################################################################
  //# Data Members
  private final int mKind;
  private final String mName;


  //#########################################################################
  //# Class Constants
  static final int K_VARIABLE = 0;
  static final int K_FOREACH = 1;
  static final int K_CONDITIONAL = 2;

}