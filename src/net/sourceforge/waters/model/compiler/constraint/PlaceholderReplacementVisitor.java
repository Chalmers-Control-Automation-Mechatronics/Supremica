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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A visitor that replaces simple identifiers that match placeholders of
 * a {@link SimplificationRule} with their matches values.
 *
 * @author Robi Malik
 */

class PlaceholderReplacementVisitor extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  static PlaceholderReplacementVisitor getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final PlaceholderReplacementVisitor INSTANCE =
      new PlaceholderReplacementVisitor();
  }

  private PlaceholderReplacementVisitor()
  {
  }


  //#########################################################################
  //# Invocation
  SimpleExpressionProxy replace(final SimpleExpressionProxy template,
                                final SimplificationRule rule,
                                final ModuleProxyFactory factory)
  {
    try {
      mFactory = factory;
      mCurrentRule = rule;
      return replace(template);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mFactory = null;
      mCurrentRule = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleExpressionProxy replace(final SimpleExpressionProxy template)
    throws VisitorException
  {
    return (SimpleExpressionProxy) template.acceptVisitor(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public BinaryExpressionProxy visitBinaryExpressionProxy
    (final BinaryExpressionProxy binary)
    throws VisitorException
  {
    final SimpleExpressionProxy oldleft = binary.getLeft();
    final SimpleExpressionProxy newleft = replace(oldleft);
    final SimpleExpressionProxy oldright = binary.getRight();
    final SimpleExpressionProxy newright = replace(oldright);
    if (newleft == oldleft && newright == oldright) {
      return binary;
    } else {
      final BinaryOperator op = binary.getOperator();
      return mFactory.createBinaryExpressionProxy(op, newleft, newright);
    }
  }

  @Override
  public FunctionCallExpressionProxy visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy expr)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> args =
      new ArrayList<SimpleExpressionProxy>(expr.getArguments());
    final int numArgs = args.size();
    boolean change = false;
    for (int i = 0; i < numArgs; i++) {
      final SimpleExpressionProxy oldArg = args.get(i);
      final SimpleExpressionProxy newArg = replace(oldArg);
      if (newArg != oldArg) {
        args.set(i, newArg);
        change = true;
      }
    }
    if (change) {
      final String name = expr.getFunctionName();
      return mFactory.createFunctionCallExpressionProxy(name, args);
    } else {
      return expr;
    }
  }

  @Override
  public IndexedIdentifierProxy visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> indexes =
      new ArrayList<SimpleExpressionProxy>(ident.getIndexes());
    final int numIndexes = indexes.size();
    boolean change = false;
    for (int i = 0; i < numIndexes; i++) {
      final SimpleExpressionProxy oldIndex = indexes.get(i);
      final SimpleExpressionProxy newIndex = replace(oldIndex);
      if (newIndex != oldIndex) {
        indexes.set(i, newIndex);
        change = true;
      }
    }
    if (change) {
      final String name = ident.getName();
      return mFactory.createIndexedIdentifierProxy(name, indexes);
    } else {
      return ident;
    }
  }

  @Override
  public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy qual)
    throws VisitorException
  {
    final IdentifierProxy oldbase = qual.getBaseIdentifier();
    final IdentifierProxy newbase = (IdentifierProxy) replace(oldbase);
    final IdentifierProxy oldcomp = qual.getComponentIdentifier();
    final IdentifierProxy newcomp = (IdentifierProxy) replace(oldcomp);
    if (newbase == oldbase && newcomp == oldcomp) {
      return qual;
    } else {
      return mFactory.createQualifiedIdentifierProxy(newbase, newcomp);
    }
  }

  @Override
  public SimpleExpressionProxy visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
  {
    return expr;
  }

  @Override
  public SimpleExpressionProxy visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy ident)
  {
    final PlaceHolder placeholder = mCurrentRule.getPlaceHolder(ident);
    if (placeholder != null) {
      return placeholder.getBoundExpression();
    } else {
      return visitSimpleExpressionProxy(ident);
    }
  }

  @Override
  public UnaryExpressionProxy visitUnaryExpressionProxy
    (final UnaryExpressionProxy unary)
    throws VisitorException
  {
    final SimpleExpressionProxy oldsub = unary.getSubTerm();
    final SimpleExpressionProxy newsub = replace(oldsub);
    if (newsub == oldsub) {
      return unary;
    } else {
      final UnaryOperator op = unary.getOperator();
      return mFactory.createUnaryExpressionProxy(op, newsub);
    }
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;
  private SimplificationRule mCurrentRule;

}
