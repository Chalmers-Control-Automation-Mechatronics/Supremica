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

package net.sourceforge.waters.model.compiler.context;

import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A simple implementation of the occurs-check. The occurs-check is needed
 * to determine whether a given expression contains a particular subterm.
 * It is used by the EFA compiler to check whether expressions contain
 * particular variables, e.g., to avoid cyclic bindings.
 *
 * @author Robi Malik
 */

public class OccursChecker extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  public static OccursChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final OccursChecker INSTANCE = new OccursChecker();
  }

  private OccursChecker()
  {
  }


  //#########################################################################
  //# Invocation
  /**
   * Searches a constraint list for a variable.
   * @return <CODE>true</CODE> if the given variable occurs in the given
   *         constraint list, in its primed or unprimed form.
   */
  public boolean occurs(final SimpleExpressionProxy varname,
                        final ConstraintList constraints)
  {
    try {
      mVarName = varname;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        if (occurs(expr)) {
          return true;
        }
      }
      return false;
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mVarName = null;
    }
  }

  /**
   * Searches an expression for a variable.
   * @return <CODE>true</CODE> if the given variable occurs in the given
   *         expression, in its primed or unprimed form.
   */
  public boolean occurs(final SimpleExpressionProxy varname,
                        final SimpleExpressionProxy expr)
  {
    try {
      mVarName = varname;
      return occurs(expr);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mVarName = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean occurs(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    if (eq.equals(expr, mVarName)) {
      return true;
    } else {
      return (Boolean) expr.acceptVisitor(this);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Boolean visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    final SimpleExpressionProxy rhs = expr.getRight();
    return occurs(lhs) || occurs(rhs);
  }

  @Override
  public Boolean visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy expr)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> args = expr.getArguments();
    for (final SimpleExpressionProxy arg : args) {
      if (occurs(arg)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Boolean visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> indexes = ident.getIndexes();
    for (final SimpleExpressionProxy index : indexes) {
      if (occurs(index)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Boolean visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident)
    throws VisitorException
  {
    final IdentifierProxy base = ident.getBaseIdentifier();
    final Boolean occbase = (Boolean) base.acceptVisitor(this);
    if (occbase) {
      return occbase;
    }
    final IdentifierProxy comp = ident.getComponentIdentifier();
    return (Boolean) comp.acceptVisitor(this);
  }

  @Override
  public Boolean visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
  {
    return false;
  }

  @Override
  public Boolean visitUnaryExpressionProxy
    (final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy subterm = expr.getSubTerm();
    return occurs(subterm);
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mVarName;

}
