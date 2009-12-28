//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   CompiledClause
//###########################################################################
//# $Id$
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
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mOperator = op;
    mLiterals = new ProxyAccessorHashSet<SimpleExpressionProxy>(eq);
  }

  public CompiledClause(final BinaryOperator op, final int size)
  {
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mOperator = op;
    mLiterals =
      new ProxyAccessorHashSet<SimpleExpressionProxy>(eq, size);
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