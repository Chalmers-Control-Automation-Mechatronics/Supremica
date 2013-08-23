//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * @author Robi Malik
 */

public abstract class AbstractEFACompiler
 <L,
  V extends AbstractEFAVariable<L>,
  TR extends AbstractEFATransitionRelation<L>,
  C extends AbstractEFAVariableContext<L, V>,
  S extends AbstractEFASystem<L, V, TR, C>>
 extends AbortableCompiler
{

  //##########################################################################
  //# Constructors
  public AbstractEFACompiler(final DocumentManager manager,
                             final ModuleProxy module)
  {
    mDocumentManager = manager;
    mInputModule = module;
  }

  //##########################################################################
  //# Simple Access
  public ModuleProxy getInputModule()
  {
    return mInputModule;
  }

  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  //##########################################################################
  abstract S compile() throws EvalException;

  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mInputModule;
  
}
