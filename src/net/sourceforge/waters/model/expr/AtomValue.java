//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   AtomValue
//###########################################################################
//# $Id: AtomValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


public class AtomValue implements Value
{

  //#########################################################################
  //# Constructors
  public AtomValue(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Getters
  String getName()
  {
    return mName;
  }


  //#########################################################################
  //# Equals and Hashcode
  public String toString()
  {
    return mName;
  }

  public int hashCode()
  {
    return mName.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final AtomValue value = (AtomValue) partner;
      return mName.equals(value.mName);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
