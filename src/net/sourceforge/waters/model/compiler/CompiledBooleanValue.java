//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledBooleanValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.BooleanValue;


class CompiledBooleanValue implements BooleanValue
{

  //#########################################################################
  //# Constructors
  CompiledBooleanValue(final boolean value)
  {
    mValue = value;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.BooleanValue
  public boolean getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return Boolean.toString(mValue);
  }

  public int hashCode()
  {
    return mValue ? 1 : 0;
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final CompiledBooleanValue value = (CompiledBooleanValue) partner;
      return mValue == value.mValue;
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Data Members
  private final boolean mValue;

}