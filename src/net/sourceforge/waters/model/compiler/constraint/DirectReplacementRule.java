//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DirectReplacementRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class DirectReplacementRule extends SimplificationRule {

  //#########################################################################
  //# Constructors
  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy replacement,
                        final PlaceHolder placeholder)
  {
    super(template, placeholder);
    mReplacements = Collections.singletonList(replacement);
  }

  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy[] replacements,
                        final PlaceHolder placeholder)
  {
    super(template, placeholder);
    mReplacements = Arrays.asList(replacements);
  }

  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy replacement,
                        final PlaceHolder[] placeholders)
  {
    super(template, placeholders);
    mReplacements = Collections.singletonList(replacement);
  }
  DirectReplacementRule(final SimpleExpressionProxy template,
                        final SimpleExpressionProxy[] replacements,
                        final PlaceHolder[] placeholders)
  {
    super(template, placeholders);
    mReplacements = Arrays.asList(replacements);
  }


  //#########################################################################
  //# Invocation Interface
  boolean isMakingReplacement()
  {
    return true;
  }

  void execute(final ConstraintPropagator propagator)
  {
    final ModuleProxyFactory factory = propagator.getFactory();
    final ReplaceVisitor visitor = ReplaceVisitor.getInstance();
    for (final SimpleExpressionProxy template : mReplacements) {
      final SimpleExpressionProxy replacement =
        visitor.replace(template, this, factory);
      propagator.addConstraint(replacement);
    }
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleExpressionProxy> mReplacements;

}