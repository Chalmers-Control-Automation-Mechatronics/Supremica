//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledAtomValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.old;

import net.sourceforge.waters.model.expr.AtomValue;


class CompiledAtomValue implements AtomValue
{

  //#########################################################################
  //# Constructors
  CompiledAtomValue(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.AtomValue
  public String getName()
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
      final CompiledAtomValue value = (CompiledAtomValue) partner;
      return mName.equals(value.mName);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
