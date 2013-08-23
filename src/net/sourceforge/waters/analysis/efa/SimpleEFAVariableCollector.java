//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.Collection;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * An implementation of {@link AbstractEFAVariableCollector}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariableCollector
 extends AbstractEFAVariableCollector<SimpleEFATransitionLabel, SimpleEFAVariable>
{

  public SimpleEFAVariableCollector(final CompilerOperatorTable optable,
   final SimpleEFAVariableContext context)
  {
    super(optable, context);
  }

  void collectAllVariables(final SimpleEFATransitionLabelEncoding encoding,
   final Collection<SimpleEFAVariable> vars)
  {
    collectAllVariables(encoding, vars, vars);
  }

  void collectAllVariables(final SimpleEFATransitionLabelEncoding encoding,
   final Collection<SimpleEFAVariable> unprimed,
   final Collection<SimpleEFAVariable> primed)
  {
    for (final SimpleEFATransitionLabel label : encoding.getTransitionLabels()) {
      final ConstraintList update = label.getConstraint();
      collectAllVariables(update, unprimed, primed);
    }
  }
  
}
