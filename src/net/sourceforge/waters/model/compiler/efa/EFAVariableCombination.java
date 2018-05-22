//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
