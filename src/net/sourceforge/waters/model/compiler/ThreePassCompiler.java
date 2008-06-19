//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ThreePassCompiler
//###########################################################################
//# $Id: ThreePassCompiler.java,v 1.1 2008-06-19 19:10:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.graph.ModuleGraphCompiler;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class ThreePassCompiler
{
 
  //##########################################################################
  //# Constructors
  public ThreePassCompiler(final DocumentManager manager,
                           final ProductDESProxyFactory factory,
                           final ModuleProxy module)
  {
    mDocumentManager = manager;
    mFactory = factory;
    mSourceInfoBuilder = new SourceInfoBuilder();
    mInputModule = module;
  }


  //##########################################################################
  //# Invocation
  public ProductDESProxy compile()
    throws EvalException
  {
    return compile(null);
  }

  public ProductDESProxy compile(final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    mSourceInfoBuilder.reset();
    final ModuleInstanceCompiler pass1 = new ModuleInstanceCompiler
      (mDocumentManager, mInputModule, mSourceInfoBuilder);
    final ModuleProxy step1 = pass1.compile(bindings);
    final boolean efa = pass1.getHasGuardActionBlocks();
    final ModuleProxy step2;
    if (efa && mIsExpandingEFATransitions) {
      throw new EvalException("EFA compilation not yet implemented!");
    } else {
      step2 = step1;
    }
    mSourceInfoBuilder.shift();
    final ModuleGraphCompiler pass3 =
      new ModuleGraphCompiler(mFactory, step2, mSourceInfoBuilder);
    return pass3.compile();
  }

  public Map<Proxy,SourceInfo> getSourceInfoMap()
  {
    return mSourceInfoBuilder.getResultMap();
  }


  //##########################################################################
  //# Configuration
  public boolean isExpandingEFATransitions()
  {
    return mIsExpandingEFATransitions;
  }

  public void setExpandingEFATransitions(final boolean expand)
  {
    mIsExpandingEFATransitions = expand;
  }



  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ProductDESProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final ModuleProxy mInputModule;

  private boolean mIsExpandingEFATransitions = true;

}
