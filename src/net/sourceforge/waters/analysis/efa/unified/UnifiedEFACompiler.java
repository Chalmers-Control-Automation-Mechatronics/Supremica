//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.efa.EFANormaliser;
import net.sourceforge.waters.model.compiler.groupnode.GroupNodeCompiler;
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
 * The compiler to convert a module ({@link ModuleProxy}) to a unified
 * EFA system ({@link UnifiedEFASystem}).
 *
 * @author Robi Malik, Sahar Mohajerani
 */
class UnifiedEFACompiler extends AbstractEFAAlgorithm
{
  //##########################################################################
  //# Constructor
  UnifiedEFACompiler(final DocumentManager manager,
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

  public void setConfiguredDefaultMarking(final IdentifierProxy marking)
  {
    mMarking = marking;
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
    if (mModuleInstanceCompiler != null) {
      mModuleInstanceCompiler.requestAbort();
    }
    if (mEFANormaliser != null) {
      mEFANormaliser.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mModuleInstanceCompiler != null) {
      mModuleInstanceCompiler.resetAbort();
    }
    if (mEFANormaliser != null) {
      mEFANormaliser.resetAbort();
    }
  }


  //##########################################################################
  //# Invocation
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mModuleInstanceCompiler = null;
    mEFANormaliser = null;
    mSystemBuilder = null;
  }

  public UnifiedEFASystem compile()
    throws EvalException, AnalysisException
  {
    return compile(null);
  }

  public UnifiedEFASystem compile(final List<ParameterBindingProxy> bindings)
    throws EvalException, AnalysisException
  {
    try
    {
      //Preparation
      setUp();
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      final IdentifierProxy marking;
      if (mMarking == null) {
        marking =
          modfactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
      } else {
        marking = mMarking;
      }
      final Collection<String> propositionNames =
        Collections.singletonList(marking.toString());
      mCompilationInfo = new CompilationInfo(mIsSourceInfoEnabled,
                                             mIsMultiExceptionsEnabled);

      //Instance Compiler
      mModuleInstanceCompiler = new ModuleInstanceCompiler
        (mDocumentManager, modfactory, mCompilationInfo, mInputModule);
      mModuleInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mModuleInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mModuleInstanceCompiler.setEnabledPropositionNames(propositionNames);
      ModuleProxy instantiated =
        mModuleInstanceCompiler.compile(bindings);
      mModuleInstanceCompiler = null;

      //Group Node Compiler
      mGroupNodeCompiler = new GroupNodeCompiler
                                (modfactory, mCompilationInfo, instantiated);
      instantiated = mGroupNodeCompiler.compile();
      mGroupNodeCompiler = null;

      //Normalisation
      final ModuleProxy normalised;
      final ProxyAccessorMap<IdentifierProxy, ConstraintList> map;
      mEFANormaliser =
        new EFANormaliser(modfactory, mCompilationInfo, instantiated);
      normalised = mEFANormaliser.compile();
      map = mEFANormaliser.getEventUpdateMap();
      mEFANormaliser = null;

      //Create UnifiedEFASystem
      mSystemBuilder = new UnifiedEFASystemBuilder
        (modfactory, mCompilationInfo, normalised, map);
      mSystemBuilder.setOptimizationEnabled(mIsOptimizationEnabled);
      mSystemBuilder.setConfiguredDefaultMarking(marking);
      return mSystemBuilder.compile();
    }

    finally { //Reset
      tearDown();
    }
  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mInputModule;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsSourceInfoEnabled = false;
  private boolean mIsMultiExceptionsEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private IdentifierProxy mMarking;

  private ModuleInstanceCompiler mModuleInstanceCompiler;
  private GroupNodeCompiler mGroupNodeCompiler;
  private EFANormaliser mEFANormaliser;
  private UnifiedEFASystemBuilder mSystemBuilder;
  private CompilationInfo mCompilationInfo;
}