//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class MatchVisitor extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  static MatchVisitor getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final MatchVisitor INSTANCE = new MatchVisitor();
  }

  private MatchVisitor()
  {
  }


  //#########################################################################
  //# Invocation
  boolean match(final SimpleExpressionProxy expr,
                final SimplificationRule rule,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    try {
      mCurrentContext = propagator;
      mCurrentRule = rule;
      final SimpleExpressionProxy template = rule.getTemplate();
      return match(template, expr);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mCurrentContext = null;
      mCurrentRule = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean match(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy expr)
    throws VisitorException
  {
    mCurrentExpression = expr;
    return (Boolean) template.acceptVisitor(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Boolean visitBinaryExpressionProxy
    (final BinaryExpressionProxy template)
    throws VisitorException
  {
    if (!(mCurrentExpression instanceof BinaryExpressionProxy)) {
      return false;
    }
    final BinaryExpressionProxy expr =
      (BinaryExpressionProxy) mCurrentExpression;
    if (template.getOperator() != expr.getOperator()) {
      return false;
    }
    final SimpleExpressionProxy tleft = template.getLeft();
    final SimpleExpressionProxy eleft = expr.getLeft();
    if (!match(tleft, eleft)) {
      return false;
    }
    final SimpleExpressionProxy tright = template.getRight();
    final SimpleExpressionProxy eright = expr.getRight();
    return match(tright, eright);
  }

  @Override
  public Boolean visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy template)
    throws VisitorException
  {
    if (!(mCurrentExpression instanceof FunctionCallExpressionProxy)) {
      return false;
    }
    final FunctionCallExpressionProxy expr =
      (FunctionCallExpressionProxy) mCurrentExpression;
    final List<SimpleExpressionProxy> tArgs = template.getArguments();
    final List<SimpleExpressionProxy> eArgs = expr.getArguments();
    if (tArgs.size() != eArgs.size()) {
      return false;
    }
    final Iterator<SimpleExpressionProxy> tIter = tArgs.iterator();
    final Iterator<SimpleExpressionProxy> eIter = eArgs.iterator();
    while (tIter.hasNext()) {
      final SimpleExpressionProxy tArg = tIter.next();
      final SimpleExpressionProxy eArg = eIter.next();
      if (!match(tArg, eArg)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Boolean visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy template)
    throws VisitorException
  {
    if (!(mCurrentExpression instanceof IndexedIdentifierProxy)) {
      return false;
    }
    final IndexedIdentifierProxy expr =
      (IndexedIdentifierProxy) mCurrentExpression;
    final List<SimpleExpressionProxy> tIndexes = template.getIndexes();
    final List<SimpleExpressionProxy> eIndexes = expr.getIndexes();
    if (tIndexes.size() != eIndexes.size()) {
      return false;
    }
    final Iterator<SimpleExpressionProxy> tIter = tIndexes.iterator();
    final Iterator<SimpleExpressionProxy> eIter = eIndexes.iterator();
    while (tIter.hasNext()) {
      final SimpleExpressionProxy tIndex = tIter.next();
      final SimpleExpressionProxy eIndex = eIter.next();
      if (!match(tIndex, eIndex)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Boolean visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy template)
    throws VisitorException
  {
    if (!(mCurrentExpression instanceof QualifiedIdentifierProxy)) {
      return false;
    }
    final QualifiedIdentifierProxy expr =
      (QualifiedIdentifierProxy) mCurrentExpression;
    final IdentifierProxy tbase = template.getBaseIdentifier();
    final IdentifierProxy ebase = expr.getBaseIdentifier();
    if (!match(tbase, ebase)) {
      return false;
    }
    final IdentifierProxy tcomp = template.getComponentIdentifier();
    final IdentifierProxy ecomp = expr.getComponentIdentifier();
    return match(tcomp, ecomp);
  }

  @Override
  public Boolean visitSimpleExpressionProxy
    (final SimpleExpressionProxy template)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    return eq.equals(template, mCurrentExpression);
  }

  @Override
  public Boolean visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy template)
    throws VisitorException
  {
    try {
      final PlaceHolder placeholder = mCurrentRule.getPlaceHolder(template);
      if (placeholder != null) {
        return placeholder.match(mCurrentExpression, mCurrentContext);
      } else {
        return visitSimpleExpressionProxy(template);
      }
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  @Override
  public Boolean visitUnaryExpressionProxy
    (final UnaryExpressionProxy template)
    throws VisitorException
  {
    if (!(mCurrentExpression instanceof UnaryExpressionProxy)) {
      return false;
    }
    final UnaryExpressionProxy expr =
      (UnaryExpressionProxy) mCurrentExpression;
    if (template.getOperator() != expr.getOperator()) {
      return false;
    }
    final SimpleExpressionProxy tsub = template.getSubTerm();
    final SimpleExpressionProxy esub = expr.getSubTerm();
    return match(tsub, esub);
  }


  //#########################################################################
  //# Data Members
  private ConstraintPropagator mCurrentContext;
  private SimplificationRule mCurrentRule;
  private SimpleExpressionProxy mCurrentExpression;

}
