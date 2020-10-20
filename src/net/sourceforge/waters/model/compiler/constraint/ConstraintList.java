//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.compiler.constraint;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * A list of simple expressions, typically understood as a conjunction
 * guard constraints.
 *
 * @author Robi Malik
 */

public class ConstraintList
{

  //#########################################################################
  //# Constructors
  private ConstraintList()
  {
    mConstraints = mUnmodifiableConstraints = Collections.emptyList();
  }

  public ConstraintList(final List<SimpleExpressionProxy> constraints)
  {
    mConstraints = constraints;
    mUnmodifiableConstraints = Collections.unmodifiableList(constraints);
  }

  public ConstraintList(final Set<SimpleExpressionProxy> constraints)
  {
    mConstraints = new ArrayList<>(constraints);
    mUnmodifiableConstraints = Collections.unmodifiableList(mConstraints);
  }


  //#########################################################################
  //# Overrides for Baseclass java.lang.Object
  @Override
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      printer.pprint(mConstraints, "{", ", ", "}");
      return writer.getBuffer().toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public boolean equals(final Object other)
  {
    if (other != null && other.getClass() == getClass()) {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final ConstraintList clist = (ConstraintList) other;
      return eq.isEqualList(mConstraints, clist.mConstraints);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    final ModuleHashCodeVisitor hash =
      ModuleHashCodeVisitor.getInstance(false);
    return hash.getListHashCode(mConstraints);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Retrieves the list of elementary constraints. Unmodifiable.
   */
  public List<SimpleExpressionProxy> getConstraints()
  {
    return mUnmodifiableConstraints;
  }

  /**
   * Checks whether this constraint is always true.
   */
  public boolean isTrue()
  {
    return mConstraints.isEmpty();
  }

  /**
   * Gets the number of elementary constraints in this list.
   */
  public int size()
  {
    return mConstraints.size();
  }

  /**
   * Checks whether this constraints list contains an elementary constraint
   * equal to the given formula (using content-based equality).
   */
  public boolean contains(final SimpleExpressionProxy constraint)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    for (final SimpleExpressionProxy current : mConstraints) {
      if (eq.equals(current, constraint)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates an expression representing the combination of formulas in this
   * constraint list.
   * @param factory  Factory used to create expressions.
   * @param operator Operator used to combine subterms. This will typically
   *                 be an AND operator, but others are possible as well.
   */
  public SimpleExpressionProxy createExpression(final ModuleProxyFactory factory,
                                                final BinaryOperator operator)
  {
    final ListIterator<SimpleExpressionProxy> iter =
      mConstraints.listIterator(mConstraints.size());
    if (iter.hasPrevious()) {
      SimpleExpressionProxy result = iter.previous();
      while (iter.hasPrevious()) {
        final SimpleExpressionProxy previous = iter.previous();
        result = factory.createBinaryExpressionProxy(operator, previous, result);
      }
      return result;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Sorting
  public void sort(final Comparator<SimpleExpressionProxy> comparator)
  {
    Collections.sort(mConstraints, comparator);
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleExpressionProxy> mConstraints;
  private final List<SimpleExpressionProxy> mUnmodifiableConstraints;


  //#########################################################################
  //# Class Constants
  /**
   * An empty constraint list, representing the logical formula <I>true</I>.
   */
  public static final ConstraintList TRUE = new ConstraintList();

}
