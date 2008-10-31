//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   CompiledClause
//###########################################################################
//# $Id: CompiledClause.java,v 1.1 2008-06-29 07:13:43 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.dnf;

import java.util.Collection;

import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class CompiledClause implements Cloneable
{

  //#########################################################################
  //# Constructors
  public CompiledClause(final BinaryOperator op)
  {
    mOperator = op;
    mLiterals = new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
  }

  public CompiledClause(final BinaryOperator op, final int size)
  {
    mOperator = op;
    mLiterals =
      new ProxyAccessorHashMapByContents<SimpleExpressionProxy>(size);
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

  public boolean containsAll(final Collection<SimpleExpressionProxy> literals)
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

  public boolean addAll(final Collection<SimpleExpressionProxy> literals)
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

  public int hashCode()
  {
    return mOperator.hashCode() + 5 * mLiterals.hashCodeByAccessorEquality();
  }

  public String toString()
  {
    final StringBuffer buffer = new StringBuffer();
    boolean first = true;
    buffer.append('{');
    for (final SimpleExpressionProxy literal : getLiterals()) {
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      buffer.append(literal.toString());
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
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
  private final ProxyAccessorMap<SimpleExpressionProxy> mLiterals;

}