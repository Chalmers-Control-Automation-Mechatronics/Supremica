//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   CompiledNormalForm
//###########################################################################
//# $Id: CompiledNormalForm.java,v 1.1 2008-06-29 07:13:43 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.dnf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.expr.BinaryOperator;


public class CompiledNormalForm implements Cloneable
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

  public Collection<CompiledClause> getClauses()
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