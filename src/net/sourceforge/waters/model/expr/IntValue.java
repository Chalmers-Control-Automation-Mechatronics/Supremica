//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IntValue
//###########################################################################
//# $Id: IntValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


public class IntValue implements Value
{

  //#########################################################################
  //# Constructors
  public IntValue(final int value)
  {
    mValue = value;
  }


  //#########################################################################
  //# Getters
  int getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return Integer.toString(mValue);
  }

  public int hashCode()
  {
    return mValue;
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final IntValue value = (IntValue) partner;
      return mValue == value.mValue;
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Data Members
  private final int mValue;

}