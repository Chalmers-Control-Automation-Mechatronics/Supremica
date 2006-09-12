//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledClause
//###########################################################################
//# $Id: CompiledClause.java,v 1.1 2006-09-12 14:32:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class CompiledClause implements Cloneable
{

  //#########################################################################
  //# Constructors
  CompiledClause(final BinaryOperator op)
  {
    mOperator = op;
    mLiterals = new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
  }

  CompiledClause(final BinaryOperator op,
                 final SimpleExpressionProxy literal)
  {
    this(op);
    add(literal);
  }


  //#########################################################################
  //# Access
  BinaryOperator getOperator()
  {
    return mOperator;
  }

  boolean isEmpty()
  {
    return mLiterals.isEmpty();
  }

  int size()
  {
    return mLiterals.size();
  }

  boolean contains(final SimpleExpressionProxy literal)
  {
    return mLiterals.containsProxy(literal);
  }

  boolean containsAll(final Collection<SimpleExpressionProxy> literals)
  {
    for (final SimpleExpressionProxy literal : literals) {
      if (!contains(literal)) {
        return false;
      }
    }
    return true;
  }

  boolean add(final SimpleExpressionProxy literal)
  {
    return mLiterals.addProxy(literal);
  }

  boolean addAll(final Collection<SimpleExpressionProxy> literals)
  {
    boolean changed = false;
    for (final SimpleExpressionProxy literal : literals) {
      changed |= mLiterals.addProxy(literal);
    }
    return changed;
  }

  Collection<SimpleExpressionProxy> getLiterals()
  {
    return mLiterals.values();
  }

  boolean isSubsumedBy(final SimpleExpressionProxy literal)
  {
    return contains(literal);
  }

  boolean isSubsumedBy(final CompiledClause clause)
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