//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   Expression class is undefined on line 10, column 16 in Templates/Classes/Class.java.
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;

/**
 * An implementation of the {@link AbstractEFAVariableContext}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariableContext
 extends AbstractEFAVariableContext<SimpleEFATransitionLabel, SimpleEFAVariable>
{

  public SimpleEFAVariableContext(final ModuleProxy module,
   final CompilerOperatorTable op)
  {
    super(module, op);
  }
  
}
