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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.efa.EFACompiler;
import net.sourceforge.waters.model.compiler.graph.ModuleGraphCompiler;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class ModuleCompiler extends AbortableCompiler
{

  //##########################################################################
  //# Constructors
  public ModuleCompiler(final DocumentManager manager,
                        final ProductDESProxyFactory factory,
                        final ModuleProxy module)
  {
    mDocumentManager = manager;
    mFactory = factory;
    mInputModule = module;
  }


  //##########################################################################
  //# Simple Access
  public ModuleProxy getInputModule()
  {
    return mInputModule;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mInstanceCompiler != null) {
      mInstanceCompiler.requestAbort();
    }
    if (mEFACompiler != null) {
      mEFACompiler.requestAbort();
    }
    if (mGraphCompiler != null) {
      mGraphCompiler.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mInstanceCompiler != null) {
      mInstanceCompiler.resetAbort();
    }
    if (mEFACompiler != null) {
      mEFACompiler.resetAbort();
    }
    if (mGraphCompiler != null) {
      mGraphCompiler.resetAbort();
    }
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
    try {
      setUp();
      mCompilationInfo = new CompilationInfo(mIsSourceInfoEnabled,
                                             mIsMultiExceptionsEnabled);
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      mInstanceCompiler = new ModuleInstanceCompiler
        (mDocumentManager, modfactory, mCompilationInfo, mInputModule);
      mInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mInstanceCompiler.setEnabledPropositionNames(mEnabledPropositionNames);
      mInstanceCompiler.setHISCCompileMode(mHISCCompileMode);
      checkAbort();
      ModuleProxy intermediate = mInstanceCompiler.compile(bindings);
      final boolean efa = mInstanceCompiler.getHasEFAElements();
      mInstanceCompiler = null;
      if (efa && mIsExpandingEFATransitions) {
        mCompilationInfo.shift();
        mEFACompiler =
          new EFACompiler(modfactory, mCompilationInfo, intermediate);
        checkAbort();
        intermediate = mEFACompiler.compile();
        mEFACompiler = null;
      }
      mCompilationInfo.shift();
      mGraphCompiler =
        new ModuleGraphCompiler(mFactory, mCompilationInfo, intermediate);
      mGraphCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      checkAbort();
      final ProductDESProxy des = mGraphCompiler.compile();
      setLocation(des);
      return des;
    } catch (final EvalException exception) {
      mCompilationInfo.raise(exception);
      throw mCompilationInfo.getExceptions();
    } finally {
      tearDown();
    }
  }

  public Map<Object,SourceInfo> getSourceInfoMap()
  {
    return mCompilationInfo.getResultMap();
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

  public boolean isSourceInfoEnabled()
  {
    return mIsSourceInfoEnabled;
  }

  public void setSourceInfoEnabled(final boolean enable)
  {
    mIsSourceInfoEnabled = enable;
  }

  public boolean isMultiExceptionsEnabled()
  {
    return mIsMultiExceptionsEnabled;
  }

  public void setMultiExceptionsEnabled(final boolean enable)
  {
    mIsMultiExceptionsEnabled = enable;
  }

  public Collection<String> getEnabledPropertyNames()
  {
    return mEnabledPropertyNames;
  }

  public void setEnabledPropertyNames(final Collection<String> names)
  {
    mEnabledPropertyNames = names;
  }

  public Collection<String> getEnabledPropositionNames()
  {
    return mEnabledPropositionNames;
  }

  public void setEnabledPropositionNames(final Collection<String> names)
  {
    mEnabledPropositionNames = names;
  }

  /**
   * Gets the current setting for partial compilation of HISC subsystems.
   * @see HISCCompileMode
   */
  public HISCCompileMode getHISCCompileMode()
  {
    return mHISCCompileMode;
  }

  /**
   * Configures the compiler for partial compilation of HISC subsystems.
   * @see HISCCompileMode
   */
  public void setHISCCompileMode(final HISCCompileMode mode)
  {
    mHISCCompileMode = mode;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUp()
  {
  }

  private void tearDown()
  {
    mInstanceCompiler = null;
    mEFACompiler = null;
    mGraphCompiler = null;
  }

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
  private final ModuleProxy mInputModule;

  private CompilationInfo mCompilationInfo;
  private ModuleInstanceCompiler mInstanceCompiler;
  private EFACompiler mEFACompiler;
  private ModuleGraphCompiler mGraphCompiler;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsExpandingEFATransitions = true;
  private boolean mIsUsingEventAlphabet = true;
  private boolean mIsSourceInfoEnabled = false;
  private boolean mIsMultiExceptionsEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private Collection<String> mEnabledPropositionNames = null;
  private HISCCompileMode mHISCCompileMode = HISCCompileMode.NOT_HISC;

}
