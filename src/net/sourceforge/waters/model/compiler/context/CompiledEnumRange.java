//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
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
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
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
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final int newsize = size() - 1;
      final List<SimpleIdentifierProxy> newlist = new ArrayList<>(newsize);
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








