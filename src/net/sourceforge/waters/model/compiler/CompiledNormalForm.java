//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledNormalForm
//###########################################################################
//# $Id: CompiledNormalForm.java,v 1.1 2006-09-12 14:32:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.expr.BinaryOperator;


class CompiledNormalForm implements Cloneable
{

  //#########################################################################
  //# Constructors
  CompiledNormalForm(final BinaryOperator op)
  {
    mOperator = op;
    mClauses = new HashSet<CompiledClause>();
  }

  CompiledNormalForm(final BinaryOperator op, final CompiledClause clause)
  {
    this(op);
    add(clause);
  }


  //#########################################################################
  //# Access
  BinaryOperator getOperator()
  {
    return mOperator;
  }

  boolean isEmpty()
  {
    return mClauses.isEmpty();
  }

  int size()
  {
    return mClauses.size();
  }

  boolean contains(final CompiledClause clause)
  {
    return mClauses.contains(clause);
  }

  boolean containsAll(final Collection<CompiledClause> clauses)
  {
    return mClauses.containsAll(clauses);
  }

  boolean add(final CompiledClause clause)
  {
    return mClauses.add(clause);
  }

  boolean addAll(final Collection<CompiledClause> clauses)
  {
    return mClauses.addAll(clauses);
  }

  Collection<CompiledClause> getClauses()
  {
    return mClauses;
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
    final StringBuffer buffer = new StringBuffer();
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