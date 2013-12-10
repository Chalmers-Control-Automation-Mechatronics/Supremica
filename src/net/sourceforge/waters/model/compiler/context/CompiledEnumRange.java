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

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


public class CompiledEnumRange implements CompiledRange
{

  //#########################################################################
  //# Constructors
  public CompiledEnumRange(final List<? extends SimpleIdentifierProxy> atoms)
  {
    mAtoms = Collections.unmodifiableList(atoms);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final CompiledEnumRange range = (CompiledEnumRange) other;
      final Iterator<SimpleIdentifierProxy> iter1 = mAtoms.iterator();
      final Iterator<SimpleIdentifierProxy> iter2 = range.mAtoms.iterator();
      while (iter1.hasNext()) {
        if (!iter2.hasNext()) {
          return false;
        }
        final IdentifierProxy atom1 = iter1.next();
        final IdentifierProxy atom2 = iter2.next();
        final ModuleEqualityVisitor eq =
          ModuleEqualityVisitor.getInstance(false);
        if (!eq.equals(atom1, atom2)) {
          return false;
        }
      }
      return !iter2.hasNext();
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    final ModuleHashCodeVisitor hash =
      ModuleHashCodeVisitor.getInstance(false);
    int result = getClass().hashCode();
    for (final IdentifierProxy atom : mAtoms) {
      result *= 5;
      result += hash.hashCode(atom);
    }
    return result;
  }

  @Override
  public String toString()
  {
    final StringBuilder result = new StringBuilder("{");
    final Iterator<SimpleIdentifierProxy> iter = mAtoms.iterator();
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
  @Override
  public int size()
  {
    return mAtoms.size();
  }

  @Override
  public boolean isEmpty()
  {
    return mAtoms.isEmpty();
  }

  @Override
  public int indexOf(final SimpleExpressionProxy value)
  {
    if (value instanceof IdentifierProxy) {
      final IdentifierProxy atom = (IdentifierProxy) value;
      return indexOf(atom);
    } else {
      return -1;
    }
  }

  @Override
  public boolean contains(final SimpleExpressionProxy value)
  {
    if (value instanceof IdentifierProxy) {
      final IdentifierProxy atom = (IdentifierProxy) value;
      return contains(atom);
    } else {
      return false;
    }
  }

  @Override
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

  @Override
  public CompiledEnumRange intersection(final CompiledRange range)
  {
    if (range instanceof CompiledEnumRange) {
      final CompiledEnumRange enumrange = (CompiledEnumRange) range;
      return intersection(enumrange);
    } else {
      return this;  // TODO BUG? Should this not be empty?
    }
  }

  @Override
  public CompiledEnumRange union(final CompiledRange range)
  {
    if (range instanceof CompiledEnumRange) {
      final CompiledEnumRange enumrange = (CompiledEnumRange) range;
      return union(enumrange);
    } else {
      return null;  // TODO BUG? Should this not be an error?
    }
  }

  @Override
  public CompiledEnumRange remove(final SimpleExpressionProxy value)
  {
    if (value instanceof IdentifierProxy) {
      final IdentifierProxy atom = (IdentifierProxy) value;
      return remove(atom);
    } else {
      return this;
    }
  }

  @Override
  public List<SimpleIdentifierProxy> getValues()
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
    final ModuleEqualityVisitor eq =
      ModuleEqualityVisitor.getInstance(false);
    int i = 0;
    for (final IdentifierProxy atom : mAtoms) {
      if (eq.equals(atom, value)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  public CompiledEnumRange intersection(final CompiledEnumRange range)
  {
    if (size() > range.size()) {
      return range.intersection(this);
    } else {
      boolean change = false;
      for (final IdentifierProxy atom : mAtoms) {
        if (!range.contains(atom)) {
          change = true;
          break;
        }
      }
      if (change) {
        final int newSize = size() - 1;
        final List<SimpleIdentifierProxy> newList =
          new ArrayList<SimpleIdentifierProxy>(newSize);
        for (final SimpleIdentifierProxy atom : mAtoms) {
          if (range.contains(atom)) {
            newList.add(atom);
          }
        }
        return new CompiledEnumRange(newList);
      } else {
        return this;
      }
    }
  }

  public CompiledEnumRange union(final CompiledEnumRange range)
  {
    if (size() > range.size()) {
      return range.intersection(this);
    } else {
      boolean change = false;
      for (final IdentifierProxy atom : mAtoms) {
        if (!range.contains(atom)) {
          change = true;
          break;
        }
      }
      if (change) {
        final int newSize = size() + range.size();
        final List<SimpleIdentifierProxy> newList =
          new ArrayList<SimpleIdentifierProxy>(newSize);
        newList.addAll(range.getValues());
        for (final SimpleIdentifierProxy atom : mAtoms) {
          if (!range.contains(atom)) {
            newList.add(atom);
          }
        }
        return new CompiledEnumRange(newList);
      } else {
        return range;
      }
    }
  }

  public CompiledEnumRange remove(final IdentifierProxy value)
  {
    if (contains(value)) {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      final int newsize = size() - 1;
      final List<SimpleIdentifierProxy> newlist =
        new ArrayList<SimpleIdentifierProxy>(newsize);
      for (final SimpleIdentifierProxy atom : mAtoms) {
        if (!eq.equals(atom, value)) {
          newlist.add(atom);
        }
      }
      return new CompiledEnumRange(newlist);
    } else {
      return this;
    }
  }

  @Override
  public SimpleExpressionProxy createExpression(final ModuleProxyFactory factory,
                                                final CompilerOperatorTable optable)
  {
    return factory.createEnumSetExpressionProxy(mAtoms);
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleIdentifierProxy> mAtoms;

}
