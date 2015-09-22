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

package net.sourceforge.waters.model.compiler.dnf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;


public class CompiledNormalForm implements Cloneable
{

  //#########################################################################
  //# Static Access
  public static CompiledNormalForm getTrueCNF()
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final BinaryOperator andop = optable.getAndOperator();
    return new CompiledNormalForm(andop);
  }

  public static CompiledNormalForm getTrueDNF()
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final BinaryOperator andop = optable.getAndOperator();
    final BinaryOperator orop = optable.getOrOperator();
    final CompiledNormalForm dnf = new CompiledNormalForm(orop);
    final CompiledClause clause = new CompiledClause(andop);
    dnf.add(clause);
    return dnf;
  }

  public static CompiledNormalForm getFalseCNF()
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final BinaryOperator andop = optable.getAndOperator();
    final BinaryOperator orop = optable.getOrOperator();
    final CompiledNormalForm cnf = new CompiledNormalForm(andop);
    final CompiledClause clause = new CompiledClause(orop);
    cnf.add(clause);
    return cnf;
  }

  public static CompiledNormalForm getFalseDNF()
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final BinaryOperator orop = optable.getAndOperator();
    return new CompiledNormalForm(orop);
  }


  //#########################################################################
  //# Constructors
  public CompiledNormalForm(final BinaryOperator op)
  {
    mOperator = op;
    mClauses = new HashSet<CompiledClause>();
  }

  public CompiledNormalForm(final BinaryOperator op,
                            final CompiledClause clause)
  {
    this(op);
    add(clause);
  }


  //#########################################################################
  //# Access
  public BinaryOperator getOperator()
  {
    return mOperator;
  }

  public boolean isEmpty()
  {
    return mClauses.isEmpty();
  }

  public int size()
  {
    return mClauses.size();
  }

  public boolean contains(final CompiledClause clause)
  {
    return mClauses.contains(clause);
  }

  public boolean containsAll(final Collection<CompiledClause> clauses)
  {
    return mClauses.containsAll(clauses);
  }

  public boolean add(final CompiledClause clause)
  {
    return mClauses.add(clause);
  }

  public boolean addAll(final Collection<CompiledClause> clauses)
  {
    return mClauses.addAll(clauses);
  }

  public boolean addAll(final CompiledNormalForm partner)
  {
    if (getOperator() == partner.getOperator()) {
      final Collection<CompiledClause> clauses = partner.getClauses();
      return addAll(clauses);
    } else {
      throw new IllegalArgumentException
        ("Trying to merge normal forms of different type!");
    }
  }

  public boolean remove(final CompiledClause clause)
  {
    return mClauses.remove(clause);
  }

  public boolean removeAll(final Collection<CompiledClause> clauses)
  {
    return mClauses.removeAll(clauses);
  }

  public boolean removeAll(final CompiledNormalForm partner)
  {
    if (getOperator() == partner.getOperator()) {
      final Collection<CompiledClause> clauses = partner.getClauses();
      return removeAll(clauses);
    } else {
      throw new IllegalArgumentException
        ("Trying to subtract normal forms of different type!");
    }
  }

  public Collection<CompiledClause> getClauses()
  {
    return mClauses;
  }

  public boolean isTrue()
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    if (mOperator == optable.getAndOperator()) {
      return isEmpty();
    } else {
      return size() == 1 && mClauses.iterator().next().isEmpty();
    }
  }

  public boolean isFalse()
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    if (mOperator == optable.getOrOperator()) {
      return isEmpty();
    } else {
      return size() == 1 && mClauses.iterator().next().isEmpty();
    }
  }


  //#########################################################################
  //# Overrides for Baseclass java.lang.Object
  public boolean equals(final Object partner)
  {
    if (partner != null && partner.getClass() == getClass()) {
      final CompiledNormalForm nf = (CompiledNormalForm) partner;
      return mOperator == nf.mOperator && mClauses.equals(nf.mClauses);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mOperator.hashCode() + 5 * mClauses.hashCode();
  }

  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    boolean first = true;
    buffer.append('{');
    for (final CompiledClause clause : getClauses()) {
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      buffer.append(clause.toString());
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  public CompiledNormalForm clone()
  {
    final CompiledNormalForm result = new CompiledNormalForm(mOperator);
    final Collection<CompiledClause> clauses = getClauses();
    result.addAll(clauses);
    return result;
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mOperator;
  private final Set<CompiledClause> mClauses;

}







