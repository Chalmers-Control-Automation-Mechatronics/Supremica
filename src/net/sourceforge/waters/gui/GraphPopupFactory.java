//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphPopupFactory
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
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
import net.sourceforge.waters.xsd.base.EventKind;


class GraphPopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  GraphPopupFactory(final WatersPopupActionManager master,
                    final EditorWindowInterface editor)
  {
    super(master);
    mVisitor = new GraphPopupVisitor();
    mEditorWindowInterface = editor;
  }


  //#########################################################################
  //# Shared Menu Items
  protected void addDefaultMenuItems()
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

  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    try {
      proxy.acceptVisitor(mVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }
  
  protected void addCommonMenuItems()
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


  //#########################################################################
  //# Inner Class GraphPopupVisitor
  private class GraphPopupVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      addPropertiesAndDeleteMenuItems(proxy);
      return null;
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
      final Collection<AbstractSubject> props =
        node.getPropositions().getEventListModifiable();
      for (final AbstractSubject prop : props) {
        if (prop instanceof IdentifierSubject) {
          final IdentifierSubject ident = (IdentifierSubject) prop;
          final String name = ident.toString();
          map.put(name, ident);
        }
      }
      final ModuleWindowInterface root =
        mEditorWindowInterface.getModuleWindowInterface();
      final ModuleContext context = root.getModuleContext();
      final GraphEventPanel epanel = mEditorWindowInterface.getEventPanel();
      final EventTableModel emodel = (EventTableModel) epanel.getModel();
      for (int row = 0; row < emodel.getRowCount(); row++) {
        final IdentifierSubject ident = emodel.getIdentifier(row);
        final String name = ident.toString();
        final EventKind kind = context.guessEventKind(ident);
        if (kind == EventKind.PROPOSITION && !map.containsKey(name)) {
          map.put(name, ident);
        }
      }
      if (!map.containsKey(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        final IdentifierSubject accepting =
          new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_MARKING_NAME);
        map.put(EventDeclProxy.DEFAULT_MARKING_NAME, accepting);
      }
      if (!map.containsKey(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        final IdentifierSubject forbidden =
          new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_FORBIDDEN_NAME);
        map.put(EventDeclProxy.DEFAULT_FORBIDDEN_NAME, forbidden);
      }
      final JMenu submenu = new JMenu("Marking");
      final WatersPopupActionManager master = getMaster();
      for (final IdentifierSubject ident : map.values()) {
        final IDEAction action = master.getNodeMarkingAction(node, ident);
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
        item.setSelected(props.contains(ident));
        submenu.add(item);
      }
      return submenu;
    }

  }


  //#######################################################################
  //# Data Members
  private final ModuleProxyVisitor mVisitor;
  private final EditorWindowInterface mEditorWindowInterface;

}
