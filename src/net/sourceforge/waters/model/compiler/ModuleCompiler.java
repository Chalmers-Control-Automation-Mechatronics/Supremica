//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.net.URI;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.efa.EFACompiler;
import net.sourceforge.waters.model.compiler.graph.ModuleGraphCompiler;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class ModuleCompiler
{
 
  //##########################################################################
  //# Constructors
  public ModuleCompiler(final DocumentManager manager,
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
    final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
    final ModuleInstanceCompiler pass1 = new ModuleInstanceCompiler
      (mDocumentManager, modfactory, mSourceInfoBuilder, mInputModule);
    pass1.setOptimizationEnabled(mIsOptimizationEnabled);
    final ModuleProxy step1 = pass1.compile(bindings);
    final boolean efa = pass1.getHasEFAElements();
    final ModuleProxy step2;
    if (efa && mIsExpandingEFATransitions) {
      final EFACompiler pass2 =
        new EFACompiler(modfactory, mSourceInfoBuilder, step1);
      step2 = pass2.compile();
    } else {
      step2 = step1;
    }
    mSourceInfoBuilder.shift();
    final ModuleGraphCompiler pass3 =
      new ModuleGraphCompiler(mFactory, mSourceInfoBuilder, step2);
    pass3.setOptimizationEnabled(mIsOptimizationEnabled);
    final ProductDESProxy des = pass3.compile();
    setLocation(des);
    return des;
  }

  public Map<Proxy,SourceInfo> getSourceInfoMap()
  {
    return mSourceInfoBuilder.getResultMap();
  }


  //##########################################################################
  //# Configuration
  public boolean isOptimizationEnabled()
  {
    return mIsOptimizationEnabled;
  }

  public void setOptimizationEnabled(final boolean enable)
  {
    mIsOptimizationEnabled = enable;
  }

  public boolean isExpandingEFATransitions()
  {
    return mIsExpandingEFATransitions;
  }

  public void setExpandingEFATransitions(final boolean expanding)
  {
    mIsExpandingEFATransitions = expanding;
  }

  public boolean isUsingEventAlphabet()
  {
    return mIsUsingEventAlphabet;
  }

  public void setUsingEventAlphabet(final boolean using)
  {
    mIsUsingEventAlphabet = using;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setLocation(final ProductDESProxy des)
  {
    final URI moduleLocation = mInputModule.getLocation();
    if (moduleLocation != null) {
      try {
        final ProxyMarshaller<ProductDESProxy> marshaller =
          mDocumentManager.findProxyMarshaller(ProductDESProxy.class);
        final String ext = marshaller.getDefaultExtension();
        final String name = mInputModule.getName();
        final URI desLocation = moduleLocation.resolve(name + ext);
        des.setLocation(desLocation);
      } catch (final IllegalArgumentException exception) {
        // No marshaller --- O.K.
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ProductDESProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final ModuleProxy mInputModule;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsExpandingEFATransitions = true;
  private boolean mIsUsingEventAlphabet = true;

}
