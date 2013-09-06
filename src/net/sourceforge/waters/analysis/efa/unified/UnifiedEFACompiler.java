//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.efa.EFAUnifier;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

class UnifiedEFACompiler extends AbstractEFAAlgorithm
{

  //##########################################################################
  //# Constructors
  UnifiedEFACompiler(final DocumentManager manager,
                     final ModuleProxy module)
  {
    mDocumentManager = manager;
    mInputModule = module;
  }


  //##########################################################################
  //# Simple Access
  ModuleProxy getInputModule()
  {
    return mInputModule;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mModuleInstanceCompiler != null) {
      mModuleInstanceCompiler.requestAbort();
    }
    if (mEFAUnifier != null) {
      mEFAUnifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mModuleInstanceCompiler != null) {
      mModuleInstanceCompiler.resetAbort();
    }
    if (mEFAUnifier != null) {
      mEFAUnifier.resetAbort();
    }
  }


  //##########################################################################
  //# Invocation
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mModuleInstanceCompiler = null;
    mEFAUnifier = null;
  }

  UnifiedEFASystem compile()
    throws EvalException, AnalysisException
  {
    return compile(null);
  }

  UnifiedEFASystem compile(final List<ParameterBindingProxy> bindings)
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      final IdentifierProxy marking;
      if (mMarking == null) {
        marking =
          modfactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
      } else {
        marking = mMarking;
      }
      final Collection<String> propositions =
        Collections.singletonList(marking.toString());
      initSourceInfo();
      mModuleInstanceCompiler = new ModuleInstanceCompiler
        (mDocumentManager, modfactory, mSourceInfoBuilder, mInputModule);
      mModuleInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mModuleInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mModuleInstanceCompiler.setEnabledPropositionNames(propositions);
      final ModuleProxy instantiated = mModuleInstanceCompiler.compile(bindings);
      mModuleInstanceCompiler = null;
      shiftSourceInfo();
      mEFAUnifier = new EFAUnifier(modfactory, mSourceInfoBuilder, instantiated);
      final ModuleProxy unified = mEFAUnifier.compile();
      shiftSourceInfo();
      final ProxyAccessorMap<IdentifierProxy, ConstraintList> map =
        mEFAUnifier.getEventUpdateMap();
      mSystemBuilder =
        new UnifiedEFASystemBuilder(modfactory, mSourceInfoBuilder, unified, map);
      mSystemBuilder.setOptimizationEnabled(mIsOptimizationEnabled);
      mSystemBuilder.setConfiguredDefaultMarking(marking);
      return mSystemBuilder.compile();
    } finally {
      tearDown();
    }
  }

  public Map<Object,SourceInfo> getSourceInfoMap()
  {
    if (mIsSourceInfoEnabled) {
      return mSourceInfoBuilder.getResultMap();
    } else {
      return null;
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

  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mMarking;
  }


  //#########################################################################
  //# Auxiliary Methods
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

  private ModuleInstanceCompiler mModuleInstanceCompiler;
  private EFAUnifier mEFAUnifier;
  private UnifiedEFASystemBuilder mSystemBuilder;
  private SourceInfoBuilder mSourceInfoBuilder;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsSourceInfoEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private IdentifierProxy mMarking;

}
