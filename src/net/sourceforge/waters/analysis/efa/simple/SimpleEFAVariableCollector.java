//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;


import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableCollector;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

/**
 * An implementation of {@link AbstractEFAVariableCollector}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariableCollector
 extends AbstractEFAVariableCollector<Integer, SimpleEFAVariable>
{
  public SimpleEFAVariableCollector(final CompilerOperatorTable optable,
   final SimpleEFAVariableContext context)
  {
    super(optable, context);
  }
}
