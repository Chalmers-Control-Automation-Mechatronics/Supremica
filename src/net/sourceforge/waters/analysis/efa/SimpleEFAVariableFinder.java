//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAVariableFinder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

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
}
