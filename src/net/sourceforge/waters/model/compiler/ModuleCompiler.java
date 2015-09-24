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

package net.sourceforge.waters.model.compiler;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.efa.EFACompiler;
import net.sourceforge.waters.model.compiler.efa.EFANormaliser;
import net.sourceforge.waters.model.compiler.graph.ModuleGraphCompiler;
import net.sourceforge.waters.model.compiler.groupnode.GroupNodeCompiler;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class ModuleCompiler extends AbortableCompiler
{
  //##########################################################################
  //# Constructor
  public ModuleCompiler(final DocumentManager manager,
                        final ProductDESProxyFactory factory,
                        final ModuleProxy module)
  {
    mDocumentManager = manager;
    mFactory = factory;
    mInputModule = module;
    mCompilationInfoIsDirty = true;
    mCompilationInfo = new CompilationInfo();
  }


  //##########################################################################
  //# Simple Access
  public ModuleProxy getInputModule()
  {
    return mInputModule;
  }

  public void setInputModule(final ModuleProxy module, final boolean clone)
  {
    if (clone) {
      mCompilationInfo = new CompilationInfo();
      mCompilationInfoIsDirty = false;
      final ModuleProxyFactory modfactory =
        ModuleElementFactory.getInstance();
      final SourceInfoCloner cloner =
        new SourceInfoCloner(modfactory, mCompilationInfo);
      mInputModule = (ModuleProxy) cloner.getClone(module);
      mInputModule.setLocation(module.getLocation());
    } else {
      mCompilationInfoIsDirty = true;
      mInputModule = module;
    }
  }


  //##########################################################################
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
    try
    {
      if (mCompilationInfoIsDirty) {
        mCompilationInfo = new CompilationInfo(mIsSourceInfoEnabled,
                                               mIsMultiExceptionsEnabled);
      }
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();

      // Resolve instances.
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
      checkAbort();

      // Simplify group nodes.
      mGroupNodeCompiler =
        new GroupNodeCompiler(modfactory, mCompilationInfo, intermediate);
      intermediate = mGroupNodeCompiler.compile();
      mGroupNodeCompiler = null;
      checkAbort();

      if (efa && mIsExpandingEFATransitions)
      {
        if (mIsNormalizationEnabled)
        { // Perform normalisation.
          mEFANormaliser = new EFANormaliser(modfactory, mCompilationInfo, intermediate);
          mEFANormaliser.setUsesEventNameBuilder(true);
          mEFANormaliser.setCreatesGuardAutomaton(true);
          mEFANormaliser.setUsesEventAlphabet(mIsUsingEventAlphabet);
          intermediate = mEFANormaliser.compile();
          mEFANormaliser = null;
        }

        // Create variable automata.
        mEFACompiler =
                  new EFACompiler(modfactory, mCompilationInfo, intermediate);
        checkAbort();
        intermediate = mEFACompiler.compile();
        mEFACompiler = null;
      }

      // Build Product DES.
      mGraphCompiler =
        new ModuleGraphCompiler(mFactory, mCompilationInfo, intermediate);
      mGraphCompiler.setOptimizationEnabled(mIsOptimizationEnabled);
      checkAbort();
      final ProductDESProxy des = mGraphCompiler.compile();
      setLocation(des);
      return des;
    }

    catch (final EvalException exception) {
      mCompilationInfo.raise(exception);
      throw mCompilationInfo.getExceptions();
    }

    finally {
      tearDown();
    }
  }

  public Map<Object,SourceInfo> getSourceInfoMap()
  {
    return mCompilationInfo.getResultMap();
  }


  //##########################################################################
  //# Static Invocation
  /**
   * Creates a name for a compiled module with its parameters appended
   * in angle brackets.
   * @param  module   The module being compiled.
   * @param  bindings The list of parameter bindings, or <CODE>null</CODE>.
   * @return A name string such as <CODE>&quot;factory&lt;N=5&gt;&quot;</CODE>.
   */
  public static String getParametrizedName
    (final ModuleProxy module, final List<ParameterBindingProxy> bindings)
  {
    try {
      final String name = module.getName();
      if (bindings == null || bindings.isEmpty()) {
        return name;
      }
      final StringWriter writer = new StringWriter();
      writer.append(name);
      char sep = '<';
      for (final ParameterBindingProxy binding : bindings) {
        writer.append(sep);
        writer.append(binding.getName());
        writer.append('=');
        ProxyPrinter.printProxy(writer, binding.getExpression());
        sep = ',';
      }
      writer.append('>');
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
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

  public boolean isNormalizationEnabled()
  {
    return mIsNormalizationEnabled;
  }

  public void setNormalizationEnabled(final boolean enable)
  {
    mIsNormalizationEnabled = enable;
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
    mCompilationInfo.setSourceInfoEnabled(enable);
  }

  public boolean isMultiExceptionsEnabled()
  {
    return mIsMultiExceptionsEnabled;
  }

  public void setMultiExceptionsEnabled(final boolean enable)
  {
    mIsMultiExceptionsEnabled = enable;
    mCompilationInfo.setMultiExceptionsEnabled(enable);
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


  //##########################################################################
  //# Auxiliary Methods
  private void tearDown()
  {
    mCompilationInfoIsDirty = true;
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
      } catch (final IllegalArgumentException exception) { }
    }
  }


  //##########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ProductDESProxyFactory mFactory;

  private ModuleProxy mInputModule;
  private CompilationInfo mCompilationInfo;
  private boolean mCompilationInfoIsDirty;

  private ModuleInstanceCompiler mInstanceCompiler;
  private GroupNodeCompiler mGroupNodeCompiler;
  private EFANormaliser mEFANormaliser;
  private EFACompiler mEFACompiler;
  private ModuleGraphCompiler mGraphCompiler;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsNormalizationEnabled = false;
  private boolean mIsExpandingEFATransitions = true;
  private boolean mIsUsingEventAlphabet = true;
  private boolean mIsSourceInfoEnabled = false;
  private boolean mIsMultiExceptionsEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private Collection<String> mEnabledPropositionNames = null;
  private HISCCompileMode mHISCCompileMode = HISCCompileMode.NOT_HISC;

}
