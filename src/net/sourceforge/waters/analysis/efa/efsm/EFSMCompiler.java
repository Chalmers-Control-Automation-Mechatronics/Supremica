//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
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

class EFSMCompiler extends AbstractEFSMAlgorithm
{

  //##########################################################################
  //# Constructors
  EFSMCompiler(final DocumentManager manager,
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
    if (mEFSMSystemBuilder != null) {
      mEFSMSystemBuilder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mModuleInstanceCompiler != null) {
      mModuleInstanceCompiler.resetAbort();
    }
    if (mEFSMSystemBuilder != null) {
      mEFSMSystemBuilder.resetAbort();
    }
  }


  //##########################################################################
  //# Invocation
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mModuleInstanceCompiler = null;
    mEFSMSystemBuilder = null;
  }

  EFSMSystem compile()
    throws EvalException, AnalysisException
  {
    return compile(null);
  }

  EFSMSystem compile(final List<ParameterBindingProxy> bindings)
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
      mCompilationInfo = new CompilationInfo(mIsSourceInfoEnabled,
                                             mIsMultiExceptionsEnabled);
      mModuleInstanceCompiler = new ModuleInstanceCompiler
        (mDocumentManager, modfactory, mCompilationInfo, mInputModule);
      mModuleInstanceCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      mModuleInstanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      mModuleInstanceCompiler.setEnabledPropositionNames(propositions);
      final ModuleProxy intermediate = mModuleInstanceCompiler.compile(bindings);
      mModuleInstanceCompiler = null;
      mEFSMSystemBuilder = new EFSMSystemBuilder
        (modfactory, mCompilationInfo, intermediate);
      mEFSMSystemBuilder.setOptimizationEnabled(mIsOptimizationEnabled);
      mEFSMSystemBuilder.setConfiguredDefaultMarking(marking);
      return mEFSMSystemBuilder.compile();
    } finally {
      tearDown();
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
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mInputModule;

  private ModuleInstanceCompiler mModuleInstanceCompiler;
  private EFSMSystemBuilder mEFSMSystemBuilder;
  private CompilationInfo mCompilationInfo;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsSourceInfoEnabled = false;
  private boolean mIsMultiExceptionsEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private IdentifierProxy mMarking;

}
