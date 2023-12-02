//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFACompiler;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * A utility to compile the model and construct
 * an EFA system ({@link SimpleEFASystem}).
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFACompiler
 extends AbstractEFACompiler<Integer, SimpleEFAVariable, SimpleEFAComponent, SimpleEFAVariableContext, SimpleEFASystem>
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
    mEnabledPropositionNames = null;
  }

  @Override
  public SimpleEFASystem compile() throws AnalysisException
  {
    return compile(null);
  }

  public SimpleEFASystem compile(final List<ParameterBindingProxy> bindings)
   throws AnalysisException
  {
    setUp();
    final ModuleInstanceCompiler mModuleInstanceCompiler;
    try {
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      final CompilationInfo mCompilationInfo = new CompilationInfo(mIsSourceInfoEnabled,
              mIsMultiExceptionsEnabled);
      mModuleInstanceCompiler = new ModuleInstanceCompiler(mDocumentManager,
                                                           modfactory,
                                                           mCompilationInfo,
                                                           mInputModule);
      mModuleInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mModuleInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mModuleInstanceCompiler.setEnabledPropositionNames(mEnabledPropositionNames);
      checkAbort();
      final ModuleProxy intermediate = mModuleInstanceCompiler.compile(bindings);
      mEFASystemBuilder = new SimpleEFASystemBuilder(modfactory, mCompilationInfo, intermediate);
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

  //#########################################################################
  @Override
public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    if (mEFASystemBuilder != null) {
      mEFASystemBuilder.requestAbort(sender);
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

  private void setUp()
  {
    if (mEnabledPropositionNames == null) {
      mEnabledPropositionNames = new ArrayList<>(2);
      mEnabledPropositionNames.add(EventDeclProxy.DEFAULT_MARKING_NAME);
      mEnabledPropositionNames.add(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
    }
  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mInputModule;
  private SimpleEFASystemBuilder mEFASystemBuilder;
  private boolean mIsOptimizationEnabled = true;
  private boolean mIsSourceInfoEnabled;
  private boolean mIsMultiExceptionsEnabled;
  private boolean mIsMarkingVariablEFAEnable;
  private Collection<String> mEnabledPropertyNames;
  private Collection<String> mEnabledPropositionNames;
  private boolean mIsExpandingEFATransitions;
}
