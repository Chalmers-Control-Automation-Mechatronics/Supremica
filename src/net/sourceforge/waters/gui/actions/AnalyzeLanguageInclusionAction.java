package net.sourceforge.waters.gui.actions;

import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class AnalyzeLanguageInclusionAction extends WatersAnalyzeAction
                                            implements ModelObserver
{


  public AnalyzeLanguageInclusionAction(final IDE ide)
  {
    this(ide, null);
  }

  public AnalyzeLanguageInclusionAction(final IDE ide, final NamedProxy aut)
  {
    super(ide);
    mNamedProxy = aut;
    if (mNamedProxy == null)
      putValue(Action.NAME, "Check all properties");
    else
    {
      if (mNamedProxy instanceof AutomatonProxy)
        putValue(Action.NAME, "Check property " + mNamedProxy.getName());
      else if (mNamedProxy instanceof SimpleComponentSubject)
      {
        if (((SimpleComponentSubject)mNamedProxy).getParent() instanceof GroupNodeProxy)
          putValue(Action.NAME, "Check properties " + mNamedProxy.getName());
        else
          putValue(Action.NAME, "Check property " + mNamedProxy.getName());
      }
    }
  }

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
    if (mNamedProxy != null)
    {
      final Map<Proxy, SourceInfo> map = ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSourceInfoMap();
      final SinglePropertyKindTranslator translator = new SinglePropertyKindTranslator(mNamedProxy, map);
      checker.setKindTranslator(translator);
    }
    return checker;
  }
  /*
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }
  */
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() == ModelChangeEvent.STATE_CHANGED)
    {
      updateEnabledStatus();
    }
    else if (event.getKind() == ModelChangeEvent.ITEM_ADDED || event.getKind() == ModelChangeEvent.ITEM_REMOVED)
    {
      updateEnabledStatus();
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


  private final NamedProxy mNamedProxy;

  private static final long serialVersionUID = -1008097797553564719L;
}
