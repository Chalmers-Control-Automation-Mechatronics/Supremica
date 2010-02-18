//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   WatersPopupActionManager
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JComponent;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.NodeSubject;

import org.supremica.gui.ide.IDE;


public class WatersPopupActionManager
{

  //#########################################################################
  //# Constructor
  public WatersPopupActionManager(final IDE ide)
  {
    mIDE = ide;
  }


  //#########################################################################
  //# Installing and Using Actions
  public void installCutCopyPasteActions(final JComponent comp)
  {
    final WatersActionManager master = mIDE.getActions();
    master.installCutCopyPasteActions(comp);
  }

  public void invokeMouseClickAction(final IDEAction action,
                                     final MouseEvent event)
  {
    if (action.isEnabled()) {
      final String key = (String) action.getValue(Action.ACTION_COMMAND_KEY);
      final int mods = event.getModifiers();
      final ActionEvent newevent =
        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, key, mods);
      action.actionPerformed(newevent);
    }
  }


  //#########################################################################
  //# Access to Actions
  public IDEAction getCutAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDECutAction.class);
  }

  public IDEAction getCopyAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDECopyAction.class);
  }

  public IDEAction getDeleteAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDEDeleteAction.class);
  }

  public IDEAction getDeleteAction(final Proxy arg)
  {
    return arg == null ? getDeleteAction() : new IDEDeleteAction(mIDE, arg);
  }

  public IDEAction getDeselectAllAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDEDeselectAllAction.class);
  }

  public IDEAction getEdgeFlipAction(final Proxy arg)
  {
    return new EditEdgeFlipAction(mIDE, arg);
  }

  public IDEAction getEditEventLabelAction(final Proxy arg)
  {
    return new EditEventLabelAction(mIDE, arg);
  }

  public IDEAction getGotoModuleAction(final ModuleProxy parent,
                                       final String name)
  {
    try {
      final DocumentManager docman = mIDE.getDocumentManager();
      final URI uri = docman.resolve(parent, name, ModuleProxy.class);
      return new GotoModuleAction(mIDE, name, uri);
    } catch (final WatersUnmarshalException exception) {
      return null;
    }
  }

  public IDEAction getGraphLayoutAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(GraphLayoutAction.class);
  }

  public IDEAction getInsertBlockedEventListAction(final Point point)
  {
    return new InsertBlockedEventListAction(mIDE, point);
  }

  public IDEAction getInsertEventLabelAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertEventLabelAction.class);
  }

  public IDEAction getPasteAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDEPasteAction.class);
  }

  public IDEAction getPropertiesAction(final Proxy arg)
  {
    return new IDEPropertiesAction(mIDE, arg);
  }

  public IDEAction getLabelRecallAction(final Proxy arg)
  {
    return new EditLabelRecallAction(mIDE, arg);
  }

  public IDEAction getInsertEventDeclAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertEventDeclAction.class);
  }

  public IDEAction getInsertSimpleComponentAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertSimpleComponentAction.class);
  }

  public IDEAction getInsertVariableAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertVariableAction.class);
  }

  public IDEAction getNodeInitialAction(final Proxy arg)
  {
    return new EditNodeInitialAction(mIDE, arg);
  }

  public IDEAction getNodeMarkingAction(final NodeSubject node,
                                        final IdentifierSubject ident)
  {
    return new EditNodeMarkingAction(mIDE, node, ident);
  }

  public IDEAction getNodeSelfloopAction(final Proxy arg)
  {
    return new InsertNodeSelfloopAction(mIDE, arg);
  }

  public IDEAction getSelectAllAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDESelectAllAction.class);
  }

  public IDEAction getShowGraphAction(final Proxy arg)
  {
    return new ShowGraphAction(mIDE, arg);
  }

  public IDEAction getShowModuleCommentAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(ShowModuleCommentAction.class);
  }

  public IDEAction getDesktopCloseWindowAction(final AutomatonProxy auto)
  {
    return new DesktopCloseWindowAction(mIDE, auto);
  }

  public IDEAction getDesktopOpenWindowAction(final AutomatonProxy auto)
  {
    return new DesktopOpenWindowAction(mIDE, auto);
  }

  public IDEAction getDesktopExecuteAction(final AutomatonProxy automaton, final Proxy highlighted)
  {
    return new DesktopExecuteAction(mIDE, automaton, highlighted);
  }

  public IDEAction getDesktopEditAction(final AutomatonProxy aut)
  {
    return new DesktopEditAction(mIDE, aut);
  }

  public IDEAction getDesktopCloseAllAction()
  {
    return new DesktopCloseAllAction(mIDE);
  }

  public IDEAction getDesktopCloseOtherAction(final AutomatonProxy automaton)
  {
    return new DesktopCloseOtherAction(mIDE, automaton);
  }

  public IDEAction getDesktopOpenOtherAction(final AutomatonProxy automaton)
  {
    return new DesktopOpenOtherAction(mIDE, automaton);
  }

  public IDEAction getDesktopShowAllAction()
  {
    return new DesktopShowAllAction(mIDE);
  }

  public IDEAction getDesktopCascadeAction()
  {
    return new DesktopCascadeAction(mIDE);
  }

  public IDEAction getResizeAllAction()
  {
    return new DesktopResizeAllAction(mIDE);
  }

  public IDEAction getResizeAction(final AutomatonProxy automaton)
  {
    return new DesktopOriginalSizeAction(mIDE, automaton);
  }

  public IDEAction getResizeOtherAction(final AutomatonProxy automaton)
  {
    return new DesktopOriginalSizeOtherAction(mIDE, automaton);
  }

  public IDEAction getEventExecuteAction(final EventProxy event)
  {
    return new EventExecuteAction(mIDE, event);
  }

  public IDEAction getTraceTravelAction(final int time)
  {
    return new TraceTravelAction(mIDE, time);
  }

  public IDEAction getLanguageIncusionAction(final NamedProxy aut)
  {
    return new AnalyzeLanguageInclusionAction(mIDE, aut);
  }

  //#######################################################################
  //# Data Members
  private final IDE mIDE;

  public IDEAction getDesktopSetStateAction(final AutomatonProxy automaton,
                                            final NodeProxy node)
  {
    return new DesktopSwitchStateAction(mIDE, automaton, node);
  }



}
