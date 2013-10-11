//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAVariableFinder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import java.util.Collection;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableFinder;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

/**
 * An implementation of {@link AbstractEFAVariableFinder}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariableFinder
 extends AbstractEFAVariableFinder<SimpleEFATransitionLabel, SimpleEFAVariable>
{

  public SimpleEFAVariableFinder(final CompilerOperatorTable optable)
  {
    super(optable);
  }

  public boolean findPrimeVariables(final ConstraintList constraints,
                                    final Collection<SimpleEFAVariable> vars)
  {
    for (final SimpleEFAVariable var : vars) {
      if (findPrimeVariable(constraints, var)) {
        return true;
      }
    }
    return false;
  }

  public boolean findPrimeVariables(final SimpleExpressionProxy exp,
                                    final Collection<SimpleEFAVariable> vars)
  {
    for (final SimpleEFAVariable var : vars) {
      if (findPrimeVariable(exp, var)) {
        return true;
      }
    }
    return false;
  }

}
