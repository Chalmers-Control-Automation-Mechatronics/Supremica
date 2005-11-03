//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledIntValue
//###########################################################################
//# $Id: CompiledIntValue.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.IntValue;


class CompiledIntValue implements IntValue
{

  //#########################################################################
  //# Constructors
  CompiledIntValue(final int value)
  {
    mValue = value;
  }

  CompiledIntValue(final boolean value)
  {
    this(value ? 1 : 0);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.IntValue
  public int getValue()
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
      final CompiledIntValue value = (CompiledIntValue) partner;
      return mValue == value.mValue;
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Data Members
  private final int mValue;

}