//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


abstract class SimplificationRule {

  //#########################################################################
  //# Constructors
  SimplificationRule(final SimpleExpressionProxy template,
                     final int numplaceholders)
  {
    mTemplate = template;
    mPlaceHolders =
      new HashMap<SimpleIdentifierProxy,PlaceHolder>(numplaceholders);
  }

  SimplificationRule(final SimpleExpressionProxy template,
                     final PlaceHolder placeholder)
  {
    final SimpleIdentifierProxy ident = placeholder.getIdentifier();
    mTemplate = template;
    mPlaceHolders = Collections.singletonMap(ident, placeholder);
  }

  SimplificationRule(final SimpleExpressionProxy template,
                     final PlaceHolder[] placeholders)
  {
    mTemplate = template;
    mPlaceHolders =
      new HashMap<SimpleIdentifierProxy,PlaceHolder>(placeholders.length);
    for (final PlaceHolder placeholder : placeholders) {
      addPlaceHolder(placeholder);
    }
  }


  //#########################################################################
  //# Invocation Interface
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    for (final PlaceHolder placeholder : mPlaceHolders.values()) {
      placeholder.reset();
    }
    final MatchVisitor visitor = MatchVisitor.getInstance();
    final boolean result = visitor.match(constraint, this, propagator);
    mMatchedExpression = result ? constraint : null;
    return result;      
  }

  abstract boolean isMakingReplacement();

  abstract void execute(ConstraintPropagator propagator)
    throws EvalException;


  //#########################################################################
  //# Simple Access
  SimpleExpressionProxy getTemplate()
  {
    return mTemplate;
  }

  PlaceHolder getPlaceHolder(SimpleIdentifierProxy ident)
  {
    return mPlaceHolders.get(ident);
  }

  void addPlaceHolder(final PlaceHolder  placeholder)
  {
    final SimpleIdentifierProxy ident = placeholder.getIdentifier();
    mPlaceHolders.put(ident, placeholder);
  }


  //#########################################################################
  //# Simple Access
  SimpleExpressionProxy getMatchedExpression()
  {
    return mMatchedExpression;
  }


  //#########################################################################
  //# Static Access
  static ModuleProxyFactory getSharedModuleProxyFactory()
  {
    return ModuleElementFactory.getInstance();
  }

  static CompilerOperatorTable getSharedCompilerOperatorTable()
  {
    return CompilerOperatorTable.getInstance();
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mTemplate;
  private final Map<SimpleIdentifierProxy,PlaceHolder> mPlaceHolders;

  private SimpleExpressionProxy mMatchedExpression;

}
