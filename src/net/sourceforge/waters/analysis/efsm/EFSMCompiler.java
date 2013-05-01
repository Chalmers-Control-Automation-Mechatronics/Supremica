//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
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
public class EFSMCompiler
{
  //##########################################################################
  //# Constructors
  public EFSMCompiler(final DocumentManager manager,
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
  //# Invocation
  public EFSMSystem compile()
    throws EvalException
  {
    return compile(null);
  }

  public EFSMSystem compile(final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
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
    ModuleInstanceCompiler pass1 = new ModuleInstanceCompiler
      (mDocumentManager, modfactory, mSourceInfoBuilder, mInputModule);
    pass1.setOptimizationEnabled(mIsOptimizationEnabled);
    pass1.setEnabledPropertyNames(mEnabledPropertyNames);
    pass1.setEnabledPropositionNames(propositions);
    final ModuleProxy intermediate = pass1.compile(bindings);
    pass1 = null;
    shiftSourceInfo();
    final EFSMSystemBuilder pass2= new EFSMSystemBuilder
      (modfactory, mSourceInfoBuilder, intermediate);
    pass2.setOptimizationEnabled(mIsOptimizationEnabled);
    pass2.setConfiguredDefaultMarking(marking);
    return pass2.compile();
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

  public boolean isExpandingEFATransitions()
  {
    return mIsExpandingEFSMTransitions;
  }

  public void setExpandingEFATransitions(final boolean expanding)
  {
    mIsExpandingEFSMTransitions = expanding;
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

  private SourceInfoBuilder mSourceInfoBuilder;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsExpandingEFSMTransitions = true;
  private boolean mIsUsingEventAlphabet = true;
  private boolean mIsSourceInfoEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private IdentifierProxy mMarking;

}
