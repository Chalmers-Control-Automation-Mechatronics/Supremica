//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.compiler.efa;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
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
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    if (eq.equals(source, target)) {
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
  @Override
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      pprint(printer);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final EFAVariableTransition trans = (EFAVariableTransition) other;
      return
        eq.equals(mSource, trans.mSource) && eq.equals(mTarget, trans.mTarget);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    final ModuleHashCodeVisitor hash =
      ModuleHashCodeVisitor.getInstance(false);
    return 5 * hash.hashCode(mSource) + hash.hashCode(mTarget);
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  @Override
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
