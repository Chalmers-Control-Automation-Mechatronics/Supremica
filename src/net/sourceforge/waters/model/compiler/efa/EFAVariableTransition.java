//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableTransition
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * A compiler-internal representation of a transition to be generated as a
 * part of an EFA variable automaton. This class comprises the variable
 * values for the source and target state of a transition, and can be used
 * as a key in hash tables for subsumption tests.
 *
 * @author Robi Malik
 */

class EFAVariableTransition implements Comparable<EFAVariableTransition>
{

  //#########################################################################
  //# Constructors
  EFAVariableTransition(final SimpleExpressionProxy source,
                        final SimpleExpressionProxy target)
  {
    if (source.equalsByContents(target)) {
      mSource = mTarget = source;
    } else {
      mSource = source;
      mTarget = target;
    }
  }


  //#########################################################################
  //# Simple Access
  SimpleExpressionProxy getSource()
  {
    return mSource;
  }

  SimpleExpressionProxy getTarget()
  {
    return mTarget;
  }

  boolean isSelfloop()
  {
    return mSource == mTarget;
  }


  //#########################################################################
  //# Overrides for Base Class java.lang.Object
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      pprint(printer);
      final StringBuffer buffer = writer.getBuffer();
      return buffer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final EFAVariableTransition trans = (EFAVariableTransition) other;
      return
        mSource.equalsByContents(trans.mSource) &&
        mTarget.equalsByContents(trans.mTarget);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return 5 * mSource.hashCodeByContents() + mTarget.hashCodeByContents();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAVariableTransition trans)
  {
    final Comparator<SimpleExpressionProxy> comparator =
      ExpressionComparator.getInstance();
    final int result = comparator.compare(mSource, trans.mSource);
    if (result != 0) {
      return result;
    } else {
      return comparator.compare(mTarget, trans.mTarget);
    }
  }


  //#########################################################################
  //# Auxliary Methods
  void pprint(final ProxyPrinter printer)
    throws IOException
  {
    printer.pprint(mSource);
    printer.pprint(" -> ");
    printer.pprint(mTarget);
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mSource;
  private final SimpleExpressionProxy mTarget;

}
