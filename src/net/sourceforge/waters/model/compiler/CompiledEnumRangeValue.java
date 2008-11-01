//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledEnumRangeValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.expr.AtomValue;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.unchecked.Casting;


class CompiledEnumRangeValue implements RangeValue
{

  //#########################################################################
  //# Constructors
  CompiledEnumRangeValue(final List<? extends AtomValue> atoms)
  {
    mAtoms = Collections.unmodifiableList(atoms);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer result = new StringBuffer("{");
    final Iterator<AtomValue> iter = mAtoms.iterator();
    while (iter.hasNext()) {
      final AtomValue atom = iter.next();
      result.append(atom.getName());
      if (iter.hasNext()) {
        result.append(", ");
      } 
    }
    result.append('}');
    return result.toString();
  }

  public int hashCode()
  {
    return mAtoms.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final CompiledEnumRangeValue range = (CompiledEnumRangeValue) partner;
      return mAtoms.equals(range.mAtoms);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  public int size()
  {
    return mAtoms.size();
  }

  public int indexOf(final Value value)
  {
    if (value instanceof AtomValue) {
      final AtomValue atom = (AtomValue) value;
      return indexOf(atom);
    } else {
      return -1;
    }
  }

  public boolean contains(final Value value)
  {
    if (value instanceof AtomValue) {
      final AtomValue atom = (AtomValue) value;
      return contains(atom);
    } else {
      return false;
    }
  }

  public List<IndexValue> getValues()
  {
    return Casting.toList(mAtoms);
  }


  //#########################################################################
  //# More Specific Access
  List<AtomValue> getAtoms()
  {
    return mAtoms;
  }

  boolean contains(final AtomValue value)
  {
    return mAtoms.contains(value);
  }

  int indexOf(final AtomValue value)
  {
    return mAtoms.indexOf(value);
  }


  //#########################################################################
  //# Data Members
  private final List<AtomValue> mAtoms;

}
