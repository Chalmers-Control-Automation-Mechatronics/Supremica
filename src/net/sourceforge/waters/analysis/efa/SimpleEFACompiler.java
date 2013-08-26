//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.ArrayList;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

/**
 * A utility to compile the model and construct 
 * an EFA system ({@link SimpleEFASystem}).
 * 
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFACompiler
 extends AbstractEFACompiler<SimpleEFATransitionLabel, 
                             SimpleEFAVariable, 
                             SimpleEFAComponent, 
                             SimpleEFAVariableContext, 
                             SimpleEFASystem>
{

  public SimpleEFACompiler(final ModuleProxy module)
  {
    this(new DocumentManager(), module);
  }

  public SimpleEFACompiler(final DocumentManager manager,
                           final ModuleProxy module)
  {
    super(manager, module);
    mInputModule = super.getInputModule();
    mDocumentManager = super.getDocumentManager();
    mEnabledPropositionNames = new ArrayList<>(2);
    mEnabledPropositionNames.add(EventDeclProxy.DEFAULT_MARKING_NAME);
    mEnabledPropositionNames.add(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
  }

  @Override
  public SimpleEFASystem compile() throws EvalException
  {
    return compile(null);
  }

  public SimpleEFASystem compile(final List<ParameterBindingProxy> bindings)
   throws EvalException
  {
    setUp();
    ModuleInstanceCompiler mModuleInstanceCompiler;
    try {
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      initSourceInfo();
      mModuleInstanceCompiler = new ModuleInstanceCompiler(mDocumentManager,
                                                           modfactory,
                                                           mSourceInfoBuilder,
                                                           mInputModule);
      mModuleInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mModuleInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mModuleInstanceCompiler.setEnabledPropositionNames(mEnabledPropositionNames);
      checkAbort();
      final ModuleProxy intermediate = mModuleInstanceCompiler.compile(bindings);
      shiftSourceInfo();
      mEFASystemBuilder = new SimpleEFASystemBuilder(modfactory,
                                                     mSourceInfoBuilder,
                                                     intermediate);
      mEFASystemBuilder.setOptimizationEnabled(mIsOptimizationEnabled);
      mEFASystemBuilder.setMarkingVariablEFAEnable(mIsMarkingVariablEFAEnable);
      return mEFASystemBuilder.compile();
    } finally {
      mEFASystemBuilder = null;
    }
  }

  //#########################################################################
  //# Simple Access Methods
  public ModuleProxy getModel()
  {
    return super.getInputModule();
  }

  //##########################################################################
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

  public boolean isSourceInfoEnabled()
  {
    return mIsSourceInfoEnabled;
  }

  public void setSourceInfoEnabled(final boolean enable)
  {
    mIsSourceInfoEnabled = enable;
  }

  public Collection<String> getEnabledPropertyNames()
  {
    return mEnabledPropertyNames;
  }

  public void setEnabledPropertyNames(final Collection<String> names)
  {
    mEnabledPropertyNames = names;
  }

  public boolean isMarkingVariablEFAEnable()
  {
    return mIsMarkingVariablEFAEnable;
  }

  public void setMarkingVariablEFAEnable(final boolean enable)
  {
    mIsMarkingVariablEFAEnable = enable;
  }

  public Collection<String> getEnabledPropositionNames()
  {
    return mEnabledPropositionNames;
  }

  public void setEnabledPropositionNames(final Collection<String> names)
  {
    mEnabledPropositionNames = names;
  }

  private void setUp()
  {
    if (mEnabledPropositionNames == null) {
      mEnabledPropositionNames = new ArrayList<>(2);
      mEnabledPropositionNames.add(EventDeclProxy.DEFAULT_MARKING_NAME);
      mEnabledPropositionNames.add(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
    }
  }
  
  //#########################################################################
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mEFASystemBuilder != null) {
      mEFASystemBuilder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mEFASystemBuilder != null) {
      mEFASystemBuilder.resetAbort();
    }
  }
  private void initSourceInfo()
  {
    if (mIsSourceInfoEnabled) {
      mSourceInfoBuilder = new SourceInfoBuilder();
    }
  }

    private void shiftSourceInfo()
  {
    if (mIsSourceInfoEnabled) {
      mSourceInfoBuilder.shift();
    }
  }
  //#########################################################################
  //# Data Members  
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mInputModule;
  private SimpleEFASystemBuilder mEFASystemBuilder;
  private SourceInfoBuilder mSourceInfoBuilder;
  private boolean mIsOptimizationEnabled = true;
  private boolean mIsSourceInfoEnabled = false;
  private boolean mIsMarkingVariablEFAEnable = false;
  private Collection<String> mEnabledPropertyNames = null;
  private Collection<String> mEnabledPropositionNames = null;
  private boolean mIsExpandingEFATransitions;
}
