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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


class EFAVariableCombination {

  //#########################################################################
  //# Creation
  static EFAVariableCombination create(final Collection<EFAVariable> contents)
  {
    final Set<EFAVariable> set = new HashSet<EFAVariable>(contents);
    return create(set);
  }

  static EFAVariableCombination create(final Set<EFAVariable> contents)
  {
    switch (contents.size()) {
    case 0:
    case 1:
      return null;
    case 2:
      final Iterator<EFAVariable> iter = contents.iterator();
      final EFAVariable first = iter.next();
      final EFAVariable second = iter.next();
      if (first.isPartnerOf(second)) {
        return null;
      } else {
        return new EFAVariableCombination(contents);
      }
    default:
      return new EFAVariableCombination(contents);
    }
  }


  //#########################################################################
  //# Constructor
  private EFAVariableCombination(final Set<EFAVariable> contents)
  {
    mContents = contents;
  }


  //#########################################################################
  //# Simple Access
  Set<EFAVariable> getContents()
  {
    return mContents;
  }

  boolean contains(final EFAVariable expr)
  {
    return mContents.contains(expr);
  }

  
  //#########################################################################
  //# Simplification
  EFAVariableCombination getReducedCombination(final EFAVariable removed)
  {
    if (contains(removed)) {
      final int size = mContents.size();
      if (size <= 2) {
        return null;
      } else {
        final Set<EFAVariable> newset = new HashSet<EFAVariable>(size - 1);
        for (final EFAVariable var : mContents) {
          if (var == removed) {
            newset.add(var);
          }
        }
        return create(newset);
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
      return mContents.equals(combination.mContents);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mContents.hashCode();
  }


  //#########################################################################
  //# Data Members
  private final Set<EFAVariable> mContents;

}
