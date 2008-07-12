//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphPopupFactory
//###########################################################################
//# $Id: GraphPopupFactory.java,v 1.3 2007-12-06 08:41:20 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


class GraphPopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  GraphPopupFactory(final WatersPopupActionManager master)
  {
    super(master);
  }


  //#########################################################################
  //# Shared Menu Items
  void addDefaultMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final MouseEvent event = getEvent();
    final Point point = event.getPoint();
    final IDEAction newblocked = master.getInsertBlockedEventListAction(point);
    popup.add(newblocked);
    popup.addSeparator();
    super.addDefaultMenuItems();
  }

  void addCommonMenuItems()
  {
    super.addCommonMenuItems();
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    popup.addSeparator();
    final IDEAction newevent = master.getInsertEventDeclAction();
    popup.add(newevent);
    final IDEAction layout = master.getGraphLayoutAction();
    popup.add(layout);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
  public Object visitEdgeProxy(final EdgeProxy edge)
  {
    visitProxy(edge);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction recall = master.getLabelRecallAction(edge);
    popup.add(recall);
    final IDEAction flip = master.getEdgeFlipAction(edge);
    popup.add(flip);
    return null;
  }

  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy block)
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final GuardActionBlockSubject subject = (GuardActionBlockSubject) block;
    final EdgeSubject parent = (EdgeSubject) subject.getParent();
    final IDEAction props = master.getPropertiesAction(parent);
    popup.add(props);
    final IDEAction delete = master.getDeleteAction(block);
    popup.add(delete);
    return null;
  }

  public Object visitLabelBlockProxy(final LabelBlockProxy block)
  { 
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final LabelBlockSubject subject = (LabelBlockSubject) block;
    final Subject parent = subject.getParent();
    if (parent instanceof EdgeSubject) {
      final EdgeSubject edge = (EdgeSubject) parent;
      final IDEAction props = master.getPropertiesAction(edge);
      popup.add(props);
    }
    final IDEAction delete = master.getDeleteAction(block);
    popup.add(delete);
    return null;
  }

  public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
  { 
    final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
    final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
    return visitSimpleNodeProxy(node);
  }

  public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
  {
    visitProxy(node);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction initial = master.getNodeInitialAction(node);
    popup.add(initial);
    final JMenu submenu = createMarkingsMenu(node);
    popup.add(submenu);
    final IDEAction selfloop = master.getNodeSelfloopAction(node);
    popup.add(selfloop);
    final IDEAction recall = master.getLabelRecallAction(node);
    popup.add(recall);
    return null;
  }


  //#######################################################################
  //# Submenus
  private JMenu createMarkingsMenu(final NodeProxy proxy)
  {
    final NodeSubject node = (NodeSubject) proxy;
    final SortedMap<String,IdentifierSubject> map =
      new TreeMap<String,IdentifierSubject>();
    for (final AbstractSubject prop :
           node.getPropositions().getEventListModifiable()) {
      if (prop instanceof IdentifierSubject) {
        final IdentifierSubject ident = (IdentifierSubject) prop;
        final String name = ident.toString();
        map.put(name, ident);
      }
    }
    final IdentifierSubject accepting;
    if (!map.containsKey(EventDeclProxy.DEFAULT_MARKING_NAME)) {
      accepting =
        new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_MARKING_NAME);
      map.put(EventDeclProxy.DEFAULT_MARKING_NAME, accepting);
    } else {
      accepting = null;
    }
    final IdentifierSubject forbidden;
    if (!map.containsKey(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
      forbidden =
        new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
      map.put(EventDeclProxy.DEFAULT_FORBIDDEN_NAME, forbidden);
    } else {
      forbidden = null;
    }
    final JMenu submenu = new JMenu("Marking");
    final WatersPopupActionManager master = getMaster();
    for (final IdentifierSubject ident : map.values()) {
      final IDEAction action = master.getNodeMarkingAction(node, ident);
      final JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
      item.setSelected(ident != accepting && ident != forbidden);
      submenu.add(item);
    }
    return submenu;
  }

}
