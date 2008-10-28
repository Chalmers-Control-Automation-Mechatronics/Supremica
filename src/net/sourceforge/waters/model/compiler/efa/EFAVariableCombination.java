//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCombination
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;

import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class EFAVariableCombination {

  //#########################################################################
  //# Constructor
  EFAVariableCombination(final ProxyAccessorMap<IdentifierProxy> identifiers,
			 final ProxyAccessorMap<UnaryExpressionProxy> primed)
  {
    final int numident = identifiers.size();
    final int numprimed = primed.size();
    mContents = new ProxyAccessorHashMapByContents<SimpleExpressionProxy>
      (numident + numprimed);
    mContents.addAll(identifiers);
    mContents.addAll(primed);
  }


  //#########################################################################
  //# Simple Access
  Collection<SimpleExpressionProxy> getContents()
  {
    return mContents.values();
  }


  //#########################################################################
  //# Equals & Hashcode
  public boolean equals(final Object other)
  {
    if (other.getClass() == getClass()) {
      final EFAVariableCombination combination =
	(EFAVariableCombination) other;
      return mContents.equalsByAccessorEquality(combination.mContents);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mContents.hashCodeByAccessorEquality();
  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorMap<SimpleExpressionProxy> mContents;

}
