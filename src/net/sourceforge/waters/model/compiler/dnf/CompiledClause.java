//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.compiler.dnf;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public class CompiledClause implements Cloneable
{

  //#########################################################################
  //# Constructors
  public CompiledClause(final BinaryOperator op)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mOperator = op;
    mLiterals = new ProxyAccessorHashSet<>(eq);
  }

  public CompiledClause(final BinaryOperator op, final int size)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mOperator = op;
    mLiterals = new ProxyAccessorHashSet<>(eq, size);
  }

  public CompiledClause(final BinaryOperator op,
                        final SimpleExpressionProxy literal)
  {
    this(op);
    add(literal);
  }


  //#########################################################################
  //# Access
  public BinaryOperator getOperator()
  {
    return mOperator;
  }

  public boolean isEmpty()
  {
    return mLiterals.isEmpty();
  }

  public int size()
  {
    return mLiterals.size();
  }

  public boolean contains(final SimpleExpressionProxy literal)
  {
    return mLiterals.containsProxy(literal);
  }

  public boolean containsAll
    (final Collection<? extends SimpleExpressionProxy> literals)
  {
    for (final SimpleExpressionProxy literal : literals) {
      if (!contains(literal)) {
        return false;
      }
    }
    return true;
  }

  public boolean add(final SimpleExpressionProxy literal)
  {
    return mLiterals.addProxy(literal);
  }

  public boolean addAll
    (final Collection<? extends SimpleExpressionProxy> literals)
  {
    boolean changed = false;
    for (final SimpleExpressionProxy literal : literals) {
      changed |= mLiterals.addProxy(literal);
    }
    return changed;
  }

  public Collection<SimpleExpressionProxy> getLiterals()
  {
    return mLiterals.values();
  }

  public boolean isSubsumedBy(final SimpleExpressionProxy literal)
  {
    return contains(literal);
  }

  public boolean isSubsumedBy(final CompiledClause clause)
  {
    final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
    return containsAll(literals);
  }


  //#########################################################################
  //# Overrides for Baseclass java.lang.Object
  @Override
  public boolean equals(final Object partner)
  {
    if (partner != null && partner.getClass() == getClass()) {
      final CompiledClause clause = (CompiledClause) partner;
      return
	mOperator == clause.mOperator &&
	mLiterals.equalsByAccessorEquality(clause.mLiterals);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return mOperator.hashCode() + 5 * mLiterals.hashCodeByAccessorEquality();
  }

  @Override
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      printer.pprint(getLiterals(), "{", ", ", "}");
      return writer.getBuffer().toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public CompiledClause clone()
  {
    final CompiledClause result = new CompiledClause(mOperator);
    final Collection<SimpleExpressionProxy> literals = getLiterals();
    result.addAll(literals);
    return result;
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mOperator;
  private final ProxyAccessorSet<SimpleExpressionProxy> mLiterals;

}
