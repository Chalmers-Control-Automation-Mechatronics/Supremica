//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   SimplificationRule
//###########################################################################
//# $Id$
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