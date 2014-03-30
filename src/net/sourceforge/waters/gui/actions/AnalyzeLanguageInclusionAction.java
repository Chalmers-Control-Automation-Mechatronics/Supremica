//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   AnalyzeLanguageInclusionAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractLanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
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
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class AnalyzeLanguageInclusionAction extends WatersAnalyzeAction
                                            implements ModelObserver
{
  //#########################################################################
  //# Constructors
  public AnalyzeLanguageInclusionAction(final IDE ide)
  {
    this(ide, null);
  }

  public AnalyzeLanguageInclusionAction(final IDE ide, final NamedProxy aut)
  {
    super(ide);
    mNamedProxy = aut;
    if (mNamedProxy == null) {
      putValue(Action.NAME, "Check all properties");
    } else {
      String suffix = "y";
      if (mNamedProxy instanceof SimpleComponentSubject) {
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) mNamedProxy;
        if (comp.getParent().getParent() instanceof ForeachProxy) {
          suffix = "ies";
        }
      }
      putValue(Action.NAME,
               "Check propert" + suffix + ' ' + mNamedProxy.getName());
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
  protected ModelVerifier getModelVerifier
    (final ModelAnalyzerFactory vfactory,
     final ProductDESProxyFactory desfactory)
  {
    final LanguageInclusionChecker checker =
      vfactory.createLanguageInclusionChecker(desfactory);
    if (mNamedProxy != null) {
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
        return ComponentKind.PROPERTY;
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
        return ComponentKind.PROPERTY;
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
