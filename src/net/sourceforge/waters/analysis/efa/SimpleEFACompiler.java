//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
  }

  @Override
  public SimpleEFASystem compile() throws EvalException
  {
    return compile(null);
  }

  public SimpleEFASystem compile(final List<ParameterBindingProxy> bindings)
   throws EvalException
  {
    ModuleInstanceCompiler mModuleInstanceCompiler;
    try {
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      final IdentifierProxy marking;
      if (mMarking == null) {
        marking =
         modfactory.createSimpleIdentifierProxy(
         EventDeclProxy.DEFAULT_MARKING_NAME);
      } else {
        marking = mMarking;
      }
      final Collection<String> propositions =
       Collections.singletonList(marking.toString());
      initSourceInfo();
      mModuleInstanceCompiler = new ModuleInstanceCompiler(mDocumentManager,
                                                           modfactory,
                                                           mSourceInfoBuilder,
                                                           mInputModule);
      mModuleInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mModuleInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mModuleInstanceCompiler.setEnabledPropositionNames(propositions);
      checkAbort();
      final ModuleProxy intermediate = mModuleInstanceCompiler.compile(bindings);
      mModuleInstanceCompiler = null;
      shiftSourceInfo();
      mEFASystemBuilder = new SimpleEFASystemBuilder(modfactory,
                                                     mSourceInfoBuilder,
                                                     intermediate);
      mEFASystemBuilder.setOptimizationEnabled(mIsOptimizationEnabled);
      mEFASystemBuilder.setConfiguredDefaultMarking(marking);
      mEFASystemBuilder.setMarkingVariablEFAEnable(mIsMarkingVariablEFAEnable);
      return mEFASystemBuilder.compile();
    } finally {
      mModuleInstanceCompiler = null;
      mEFASystemBuilder = null;
    }
  }

  //#########################################################################
  //# Simple Access Methods
  public ModuleProxy getModel()
  {
    return super.getInputModule();
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

  public Collection<String> getEnabledPropertyNames()
  {
    return mEnabledPropertyNames;
  }

  public void setEnabledPropertyNames(final Collection<String> names)
  {
    mEnabledPropertyNames = names;
  }

  public void setConfiguredDefaultMarking(final IdentifierProxy marking)
  {
    mMarking = marking;
  }

  public boolean isMarkingVariablEFAEnable()
  {
    return mIsMarkingVariablEFAEnable;
  }

  public void setMarkingVariablEFAEnable(final boolean enable)
  {
    mIsMarkingVariablEFAEnable = enable;
  }

  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mMarking;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
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
  private IdentifierProxy mMarking;
  private boolean mIsUsingEventAlphabet;
  private boolean mIsExpandingEFATransitions;
  
}
