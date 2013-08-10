//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.Collection;
import java.util.Map;

import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * @author Robi Malik
 */

public abstract class AbstractEFACompiler
  <L,
   V extends AbstractEFAVariable<L>,
   TR extends AbstractEFATransitionRelation<L>,
   C extends AbstractEFAVariableContext<L,V>,
   S extends AbstractEFASystem<L,V,TR,C>>
{

  //##########################################################################
  //# Constructors
  public AbstractEFACompiler(final DocumentManager manager, final ModuleProxy module)
  {
    mDocumentManager = manager;
    mInputModule = module;
  }

  //##########################################################################
  //# Compile the system
  abstract S compile();


  //##########################################################################
  //# Simple Access
  public ModuleProxy getInputModule()
  {
    return mInputModule;
  }

  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
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

  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mMarking;
  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ModuleProxy mInputModule;
  private SourceInfoBuilder mSourceInfoBuilder;

  private boolean mIsOptimizationEnabled = true;
  private boolean mIsExpandingEFATransitions = true;
  private boolean mIsUsingEventAlphabet = true;
  private boolean mIsSourceInfoEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private IdentifierProxy mMarking;

}
