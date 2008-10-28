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
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class EFAVariableCombination {

  //#########################################################################
  //# Creation
  static EFAVariableCombination create
    (final ProxyAccessorMap<SimpleExpressionProxy> contents)
  {
    switch (contents.size()) {
    case 0:
    case 1:
      return null;
    case 2:
      final Iterator<SimpleExpressionProxy> iter =
        contents.values().iterator();
      final SimpleExpressionProxy first = iter.next();
      final SimpleExpressionProxy second = iter.next();
      if (first instanceof IdentifierProxy &&
          second instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy expr = (UnaryExpressionProxy) second;
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        if (first.equalsByContents(subterm)) {
          return null;
        } else {
          return new EFAVariableCombination(contents);
        }
      } else if (first instanceof UnaryExpressionProxy &&
                 second instanceof IdentifierProxy) {
        final UnaryExpressionProxy expr = (UnaryExpressionProxy) first;
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        if (second.equalsByContents(subterm)) {
          return null;
        } else {
          return new EFAVariableCombination(contents);
        }
      } else {
        return new EFAVariableCombination(contents);
      }
    default:
      return new EFAVariableCombination(contents);
    }
  }


  //#########################################################################
  //# Constructor
  private EFAVariableCombination
    (final ProxyAccessorMap<SimpleExpressionProxy> contents)
  {
    mContents =
      new ProxyAccessorHashMapByContents<SimpleExpressionProxy>(contents);
  }


  //#########################################################################
  //# Simple Access
  Collection<SimpleExpressionProxy> getContents()
  {
    return mContents.values();
  }

  ProxyAccessorMap<SimpleExpressionProxy> getContentsMap()
  {
    return mContents;
  }

  boolean contains(final SimpleExpressionProxy expr)
  {
    return mContents.containsProxy(expr);
  }

  
  //#########################################################################
  //# Simplification
  EFAVariableCombination getReducedCombination
    (final SimpleExpressionProxy removed)
  {
    if (contains(removed)) {
      final int size = mContents.size();
      if (size <= 2) {
        return null;
      } else {
        final ProxyAccessorMap<SimpleExpressionProxy> newmap =
          new ProxyAccessorHashMapByContents<SimpleExpressionProxy>(size - 1);
        for (final Map.Entry<ProxyAccessor<SimpleExpressionProxy>,
               SimpleExpressionProxy>
               entry : mContents.entrySet()) {
          final SimpleExpressionProxy expr = entry.getValue();
          if (!expr.equalsByContents(removed)) {
            final ProxyAccessor<SimpleExpressionProxy> accessor =
              entry.getKey();
            newmap.put(accessor, expr);
          }
        }
        return create(newmap);
      }
    } else {
      return this;
    }
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
