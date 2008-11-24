//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompiledEnumRange
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


public class CompiledEnumRange implements CompiledRange
{

  //#########################################################################
  //# Constructors
  public CompiledEnumRange(final List<? extends IdentifierProxy> atoms)
  {
    mAtoms = Collections.unmodifiableList(atoms);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final CompiledEnumRange range = (CompiledEnumRange) other;
      final Iterator<IdentifierProxy> iter1 = mAtoms.iterator();
      final Iterator<IdentifierProxy> iter2 = range.mAtoms.iterator();
      while (iter1.hasNext()) {
        if (!iter2.hasNext()) {
          return false;
        }
        final IdentifierProxy atom1 = iter1.next();
        final IdentifierProxy atom2 = iter2.next();
        if (!atom1.equalsByContents(atom2)) {
          return false;
        }
      }
      return !iter2.hasNext();      
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    int result = getClass().hashCode();
    for (final IdentifierProxy atom : mAtoms) {
      result *= 5;
      result += atom.hashCodeByContents();
    }
    return result;
  }

  public String toString()
  {
    final StringBuffer result = new StringBuffer("{");
    final Iterator<IdentifierProxy> iter = mAtoms.iterator();
    while (iter.hasNext()) {
      final IdentifierProxy atom = iter.next();
      result.append(atom.toString());
      if (iter.hasNext()) {
        result.append(", ");
      } 
    }
    result.append('}');
    return result.toString();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  public int size()
  {
    return mAtoms.size();
  }

  public int indexOf(final SimpleExpressionProxy value)
  {
    if (value instanceof IdentifierProxy) {
      final IdentifierProxy atom = (IdentifierProxy) value;
      return indexOf(atom);
    } else {
      return -1;
    }
  }

  public boolean contains(final SimpleExpressionProxy value)
  {
    if (value instanceof IdentifierProxy) {
      final IdentifierProxy atom = (IdentifierProxy) value;
      return contains(atom);
    } else {
      return false;
    }
  }

  public boolean intersects(final CompiledRange range)
  {
    if (range instanceof CompiledEnumRange) {
      if (size() < range.size()) {
        for (final IdentifierProxy atom : mAtoms) {
          if (range.contains(atom)) {
            return true;
          }
        }
      } else {
        final CompiledEnumRange enumrange = (CompiledEnumRange) range;
        for (final IdentifierProxy atom : enumrange.mAtoms) {
          if (contains(atom)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public CompiledEnumRange intersection(final CompiledRange range)
  {
    if (range instanceof CompiledEnumRange) {
      final CompiledEnumRange enumrange = (CompiledEnumRange) range;
      return intersection(enumrange);
    } else {
      return this;
    }
  }

  public CompiledEnumRange remove(final SimpleExpressionProxy value)
  {
    if (value instanceof IdentifierProxy) {
      final IdentifierProxy atom = (IdentifierProxy) value;
      return remove(atom);
    } else {
      return this;
    }
  }

  public List<IdentifierProxy> getValues()
  {
    return mAtoms;
  }


  //#########################################################################
  //# More Specific Access
  public boolean contains(final IdentifierProxy atom)
  {
    return indexOf(atom) >= 0;
  }

  public int indexOf(final IdentifierProxy value)
  {
    int i = 0;
    for (final IdentifierProxy atom : mAtoms) {
      if (atom.equalsByContents(value)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  public CompiledEnumRange intersection(final CompiledEnumRange range)
  {
    boolean change = false;
    for (final IdentifierProxy atom : mAtoms) {
      if (!range.contains(atom)) {
        change = true;
        break;
      }
    }
    if (change) {
      final int newsize = size() - 1;
      final List<IdentifierProxy> newlist =
        new ArrayList<IdentifierProxy>(newsize);
      for (final IdentifierProxy atom : mAtoms) {
        if (range.contains(atom)) {
          newlist.add(atom);
        }
      }
      return new CompiledEnumRange(newlist);
    } else {
      return this;
    }
  }

  public CompiledEnumRange remove(final IdentifierProxy value)
  {
    if (contains(value)) {
      final int newsize = size() - 1;
      final List<IdentifierProxy> newlist =
        new ArrayList<IdentifierProxy>(newsize);
      for (final IdentifierProxy atom : mAtoms) {
        if (!atom.equalsByContents(value)) {
          newlist.add(atom);
        }
      }
      return new CompiledEnumRange(newlist);
    } else {
      return this;
    }
  }


  //#########################################################################
  //# Data Members
  private final List<IdentifierProxy> mAtoms;

}
