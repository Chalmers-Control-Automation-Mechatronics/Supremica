//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui,actions
//# CLASS:   AnalyzeLanguageInclusionAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.AbstractLanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
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
        if (comp.getParent().getParent() instanceof ForeachComponentProxy) {
          suffix = "ies";
        }
      }
      putValue(Action.NAME,
               "Check propert" + suffix + ' ' + mNamedProxy.getName());
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.actions.WatersAnalyzeAction
  protected String getCheckName()
  {
    return "Language Inclusion";
  }

  protected String getFailureDescription()
  {
    if (mNamedProxy == null)
      return "does not satisfy Language Inclusion";
    else
      return "does not satisfy property " + mNamedProxy.getName();
  }

  protected String getSuccessDescription()
  {
    if (mNamedProxy == null)
      return "satisfies Language Inclusion";
    else
      return "satisfies property " + mNamedProxy.getName();
  }

  protected ModelVerifier getModelVerifier(final ModelVerifierFactory vfactory,
                                         final ProductDESProxyFactory desfactory)
  {
    final LanguageInclusionChecker checker = vfactory.createLanguageInclusionChecker(desfactory);
    if (mNamedProxy != null) {
      if (mNamedProxy instanceof AutomatonProxy) {
        final KindTranslator translator = new SingleAutomatonKindTranslator();
        checker.setKindTranslator(translator);
      } else if (mNamedProxy instanceof SimpleComponentSubject) {
        final ModuleContainer container = getActiveModuleContainer();
        if (container != null) {
          final Map<Proxy,SourceInfo> map = container.getSourceInfoMap();
          final KindTranslator translator =
              new SingleComponentKindTranslator(map);
          checker.setKindTranslator(translator);
        }
      }
    }
    return checker;
  }


  //#########################################################################
  //# Enablement
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
      /*
      final ModuleContainer container = getActiveModuleContainer();
      final ModuleSubject module = container.getModule();
      setObservedModule(module);
      */
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }

  public void modelChanged(final ModelChangeEvent event)
  {
    switch (event.getKind()) {
    case ModelChangeEvent.STATE_CHANGED:
      if (event.getSource() instanceof SimpleComponentSubject) {
        updateEnabledStatus();
      }
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

  /*
  public void updateEnabledStatus()
  {
    final EditorWindowInterface window = getActiveModuleWindowInterface();
    if (window == null) {
      setObservedWindow(null);
      setEnabled(false);
    } else {
      final GraphSubject graph = surface.getGraph();
      final boolean enabled = graph.getBlockedEvents() == null;
      setObservedGraph(graph);
      setEnabled(enabled);
    }
  }


  private void setObservedWindow(EditorWindowInterface window)
  {
    if (mWindow != window) {
      if (window != null) {
        mWindow.removeModelObserver(this);
      }
      mWindow = window;
      if (mWindow != null) {
        mWindow.addModelObserver(this);
      }
    }
  }*/


  //#######################################################################
  //# Inner Class SingleAutomatonKindTranslator
  private class SingleAutomatonKindTranslator
    extends AbstractLanguageInclusionKindTranslator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mNamedProxy) {
        return ComponentKind.SPEC;
      } else {
        return super.getComponentKind(aut);
      }
    }

  }


  //#######################################################################
  //# Inner Class SingleComponentKindTranslator
  private class SingleComponentKindTranslator
    extends AbstractLanguageInclusionKindTranslator
  {

    //#######################################################################
    //# Constructor
    private SingleComponentKindTranslator(final Map<Proxy,SourceInfo> map)
    {
      mSourceInfo = map;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (mSourceInfo.get(aut).getSourceObject() == mNamedProxy) {
        return ComponentKind.SPEC;
      } else {
        return super.getComponentKind(aut);
      }
    }

    //#######################################################################
    //# Data Members
    private final Map<Proxy,SourceInfo> mSourceInfo;
  }


  //#########################################################################
  //# Data Members
  private final NamedProxy mNamedProxy;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1008097797553564719L;

}
