//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.actions;

import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.AbstractLanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class VerifyLanguageInclusionAction extends WatersVerificationAction
                                            implements ModelObserver
{
  //#########################################################################
  //# Constructors
  public VerifyLanguageInclusionAction(final IDE ide)
  {
    this(ide, null);
  }

  public VerifyLanguageInclusionAction(final IDE ide, final NamedProxy aut)
  {
    super(ide, AnalysisOperation.LANGUAGE_INCLUSION_CHECK);
    mNamedProxy = aut;
    if (mNamedProxy == null) {
      putValue(Action.NAME, "Check all properties");
    } else {
      String suffix = "y";
      if (mNamedProxy instanceof SimpleComponentSubject) {
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) mNamedProxy;
        if (SubjectTools.getAncestor(comp, ForeachSubject.class) != null) {
          suffix = "ies";
        }
      }
      String name = mNamedProxy.getName();
      if (name.length() > 32) {
        name = name.substring(0, 30) + "...";
      }
      putValue(Action.NAME, "Check propert" + suffix + ' ' + name);
    }
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  @Override
  protected String getCheckName()
  {
    return "Language Inclusion";
  }

  @Override
  protected String getFailureDescription()
  {
    if (mNamedProxy == null)
      return "does not satisfy Language Inclusion";
    else
      return "does not satisfy property " + mNamedProxy.getName();
  }

  @Override
  protected String getSuccessDescription()
  {
    if (mNamedProxy == null)
      return "satisfies Language Inclusion";
    else
      return "satisfies property " + mNamedProxy.getName();
  }

  @Override
  protected ModelVerifier createAndConfigureModelVerifier
    (final ProductDESProxyFactory desFactory)
  {
    final LanguageInclusionChecker checker =
      (LanguageInclusionChecker) super.createAndConfigureModelVerifier(desFactory);
    if (checker != null && mNamedProxy != null) {
      if (mNamedProxy instanceof AutomatonProxy) {
        final KindTranslator translator = new SingleAutomatonKindTranslator();
        checker.setKindTranslator(translator);
      } else if (mNamedProxy instanceof SimpleComponentSubject) {
        final ModuleContainer container = getActiveModuleContainer();
        if (container != null) {
          final Map<Object,SourceInfo> map = container.getSourceInfoMap();
          final KindTranslator translator =
              new SingleComponentKindTranslator(map);
          checker.setKindTranslator(translator);
        }
      }
    }
    return checker;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    switch (event.getKind()) {
    case ModelChangeEvent.STATE_CHANGED:
      if (event.getSource() instanceof SimpleComponentSubject)
        updateEnabledStatus();
      break;
    case ModelChangeEvent.ITEM_ADDED:
    case ModelChangeEvent.ITEM_REMOVED:
      if (event.getValue() instanceof SimpleComponentSubject) {
        updateEnabledStatus();
      }
      break;
    default:
      break;
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.DEFAULT_PRIORITY;
  }


  //#########################################################################
  //# Enablement
  @Override
  public void update(final EditorChangedEvent event)
  {
    final ModuleContainer container = getActiveModuleContainer();
    final ModuleSubject module =
      container == null ? null : container.getModule();
    if (module != mConnectedModule) {
      if (mConnectedModule != null) {
        mConnectedModule.removeModelObserver(this);
      }
      mConnectedModule = module;
      if (mConnectedModule != null) {
        mConnectedModule.addModelObserver(this);
        updateEnabledStatus();
      }
    }
  }

  @Override
  public void updateEnabledStatus()
  {
    super.updateEnabledStatus();
    if (isEnabled()) {
      final ModuleContainer container = getActiveModuleContainer();
      final ModuleProxy module = container.getModule();
      final PropertyFindVisitor visitor = new PropertyFindVisitor();
      if (!visitor.containsProperty(module)) {
        setEnabled(false);
      }
    }
  }


  //#########################################################################
  //# Inner Class SingleAutomatonKindTranslator
  private class SingleAutomatonKindTranslator
    extends AbstractLanguageInclusionKindTranslator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mNamedProxy) {
        return ComponentKind.SPEC;
      } else if (aut.getKind() == ComponentKind.PROPERTY) {
        return null;
      } else {
        return super.getComponentKind(aut);
      }
    }
  }

  //#########################################################################
  //# Inner Class SingleComponentKindTranslator
  private class SingleComponentKindTranslator
    extends AbstractLanguageInclusionKindTranslator
  {

    //#######################################################################
    //# Constructor
    private SingleComponentKindTranslator(final Map<Object,SourceInfo> map)
    {
      mSourceInfo = map;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final SourceInfo info = mSourceInfo.get(aut);
      if (info == null) {
        return super.getComponentKind(aut);
      } else if (info.getSourceObject() == mNamedProxy) {
        return ComponentKind.SPEC;
      } else if (aut.getKind() == ComponentKind.PROPERTY) {
        return null;
      } else {
        return super.getComponentKind(aut);
      }
    }

    //#######################################################################
    //# Data Members
    private final Map<Object,SourceInfo> mSourceInfo;
  }


  //#########################################################################
  //# Inner Class PropertyFindVisitor
  private class PropertyFindVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private boolean containsProperty(final ModuleProxy proxy)
    {
      try {
        return visitModuleProxy(proxy);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    private Boolean isProperty(final Proxy proxy)
    throws VisitorException
    {
      return (Boolean)proxy.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    @Override
    public Boolean visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
    {
      for (final Proxy child : foreach.getBody()) {
        if (isProperty(child)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Boolean visitModuleProxy(final ModuleProxy proxy)
    throws VisitorException
    {
      for (final Proxy child : proxy.getComponentList())
        if (isProperty(child))
          return true;
      return false;
    }

    @Override
    public Boolean visitSimpleComponentProxy(final SimpleComponentProxy proxy)
    {
      return proxy.getKind() == ComponentKind.PROPERTY;
    }
  }


  //#########################################################################
  //# Data Members
  private final NamedProxy mNamedProxy;
  private ModuleSubject mConnectedModule;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1008097797553564719L;

}
