//# -*-  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventTableModel
//###########################################################################
//# $Id: EventTableModel.java,v 1.36 2008-04-24 03:45:31 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.IndexedIdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


/**
 * <p>A table model for the events pane.</P>
 *
 * @author Robi Malik
 */

public class EventTableModel
  extends AbstractTableModel
{

  //#########################################################################
  //# Constructors
  EventTableModel(final GraphSubject graph,
                  final GraphEventHandler handler,
                  final ModuleWindowInterface root)
  {
    mGraph = graph;
    mEventHandler = handler;
    mRoot = root;
    mEvents = collectEvents();
    final ModuleSubject module = mRoot.getModuleSubject();
    final ListSubject<EventDeclSubject> events =
      module.getEventDeclListModifiable();
    mEventDeclListModelObserver = new EventDeclListModelObserver();
    mGraphModelObserver = new GraphModelObserver();
    mIdentifierCollectVisitor = new IdentifierCollectVisitor();
    mGraphSearchVisitor = new GraphSearchVisitor();
    events.addModelObserver(mEventDeclListModelObserver);
    graph.addModelObserver(mGraphModelObserver);
  }


  //#########################################################################
  //# Clean Up
  void close()
  {
    final ModuleSubject module = mRoot.getModuleSubject();
    final ListSubject<EventDeclSubject> events =
      module.getEventDeclListModifiable();
    events.removeModelObserver(mEventDeclListModelObserver);
    mGraph.removeModelObserver(mGraphModelObserver);
  }


  //#########################################################################
  //# Interface javax.swing.TableModel
  public int getRowCount()
  {
    return mEvents.size();
  }

  public int getColumnCount()
  {
    return 2;
  }

  public Class getColumnClass(final int column)
  {
    switch (column) {
    case 0:
      return Icon.class;
    case 1:
      return IdentifierSubject.class;
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for event table model!");
    }
  }

  public Object getValueAt(final int row, final int column)
  {
    final EventEntry entry = (EventEntry) mEvents.get(row);
    switch (column) {
    case 0:
      final ModuleContext context = mRoot.getModuleContext();
      final IdentifierSubject ident = entry.getName();
      return context.guessEventIcon(ident);
    case 1:
      return entry.getName();
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for event table model!");
    }
  }

  public boolean isCellEditable(final int row, final int column)
  {
    return column == 1;
  }

  public void setValueAt(final Object value,
                         final int row,
                         final int column)
  {
    switch (column) {
      case 0:
        return;
      case 1:
        final Proxy proxy = (Proxy) value;
        final ModuleProxyCloner cloner =
          ModuleSubjectFactory.getCloningInstance();
        final IdentifierSubject neo =
          (IdentifierSubject) cloner.getClone(proxy);
        final IdentifierSubject old = getIdentifier(row);
        if (old == null) {
          cleanUpNullItemAtEnd();
          if (neo != null) {
            mEventHandler.addEvent(neo);
          }
        } else if (neo == null) {
          mEventHandler.removeEvent(old);
        } else if (!old.equalsWithGeometry(neo)) {
          mEventHandler.replaceEvent(old, neo);
        }
        return;
      default:
        throw new ArrayIndexOutOfBoundsException
          ("Bad column number for event table model!");
    }
  }


  //#########################################################################
  //# More Specific Access
  IdentifierSubject getIdentifier(final int row)
  {
    final EventEntry entry = mEvents.get(row);
    return entry.getName();
  }

  int getRow(final IdentifierSubject ident)
  {
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index < 0) {
      return index;
    } else if (getIdentifier(index) == ident) {
      return index;
    } else {
      return -1;
    }
  }

  boolean containsEqualIdentifier(final IdentifierSubject ident)
  {
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index < 0) {
      return false;
    } else {
      final IdentifierSubject found = getIdentifier(index);
      return found.equalsByContents(ident);
    }
  }

  void addIdentifier(final IdentifierSubject ident)
  {
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index < 0) {
      final int inspoint = -index - 1;
      mEvents.add(inspoint, entry);
      fireTableRowsInserted(inspoint, inspoint);
    }
  }

  void removeIdentifiers(final String name)
  {
    final IdentifierSubject tester = new SimpleIdentifierSubject(name);
    final EventEntry entry = new EventEntry(tester);
    final int first = Collections.binarySearch(mEvents, entry);
    if (first >= 0) {
      final int size = mEvents.size();
      int last = first;
      for (int index = first + 1; index < size; index++) {
        final IdentifierSubject ident = getIdentifier(index);
        final String iname;
        if (ident instanceof SimpleIdentifierSubject) {
          final SimpleIdentifierSubject simple =
            (SimpleIdentifierSubject) ident;
          iname = simple.getName();
        } else if (ident instanceof IndexedIdentifierSubject) {
          final IndexedIdentifierSubject indexed =
            (IndexedIdentifierSubject) ident;
          iname = indexed.getName();
        } else {
          break;
        }
        if (iname.equals(name)) {
          last++;
        } else {
          break;
        }
      }
      removeIdentifierRange(first, last);
    }
  }

  void removeIdentifier(final IdentifierSubject ident)
  {
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index >= 0) {
      mEvents.remove(index);
      fireTableRowsDeleted(index, index);
    }
  }

  int createEvent()
  {
    final int row = mEvents.size();
    final EventEntry entry = new EventEntry();
    mEvents.add(entry);
    fireTableRowsInserted(row, row);
    return row;
  }

  void cleanUpNullItemAtEnd()
  {
    final int row = getRowCount() - 1;
    if (row >= 0 && getIdentifier(row) == null) {
      removeIdentifierRange(row, row);
    }
  }

  String getToolTipText(final int row)
  {
    final ModuleContext context = mRoot.getModuleContext();
    final IdentifierSubject event = getIdentifier(row);
    return context.guessEventToolTipText(event);
  }

  boolean isDisplayed()
  {
    final EditorWindowInterface iface = mRoot.getActiveEditorWindowInterface();
    if (iface == null) {
      return false;
    } else {
      final ControlledSurface surface = iface.getControlledSurface();
      return surface.getGraph() == mGraph;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<EventEntry> collectEvents()
  {
    final ProxyAccessorMap<IdentifierSubject> collected =
      new ProxyAccessorHashMapByContents<IdentifierSubject>();
    final EventListExpressionProxy blocked = mGraph.getBlockedEvents();
    collectEvents(collected, blocked);
    final Collection<NodeProxy> nodes = mGraph.getNodes();
    for (final NodeProxy node : nodes) {
      final EventListExpressionProxy props = node.getPropositions();
      collectEvents(collected, props);
    }
    final Collection<EdgeProxy> edges = mGraph.getEdges();
    for (final EdgeProxy edge : edges) {
      final EventListExpressionProxy labels = edge.getLabelBlock();
      collectEvents(collected, labels);
    }
    final List<EventEntry> result =
      new ArrayList<EventEntry>(collected.size());
    final Iterator<IdentifierSubject> iter = collected.iterator();
    while (iter.hasNext()) {
      final IdentifierSubject ident = iter.next();
      final EventEntry entry = new EventEntry(ident);
      result.add(entry);
    }
    Collections.sort(result);
    return result;
  }

  private void collectEvents(final ProxyAccessorMap<IdentifierSubject> dest,
                             final EventListExpressionProxy source)
  {
    if (source != null) {
      collectEvents(dest, source.getEventList());
    }
  }

  private void collectEvents(final ProxyAccessorMap<IdentifierSubject> dest,
                             final List<? extends Proxy> source)
  {
    for (final Proxy proxy : source) {
      if (proxy instanceof ForeachEventProxy) {
        final ForeachEventProxy foreach = (ForeachEventProxy) proxy;
        final List<Proxy> body = foreach.getBody();
        collectEvents(dest, body);
      } else {
        final IdentifierSubject ident = (IdentifierSubject) proxy;
        dest.addProxy(ident.clone());
      }
    }
  }

  private boolean containsEvent(final IdentifierSubject ident)
  {
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    return index >= 0;
  }

  private int getFirstRow(final EventDeclSubject decl)
  {
    final IdentifierSubject ident = decl.getIdentifier();
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index < 0) {
      return -index - 1;
    } else {
      return index;
    }
  }

  private EventDeclProxy guessEventDecl(final int row)
  {
    final ModuleContext context = mRoot.getModuleContext();
    final IdentifierSubject ident = getIdentifier(row);
    return context.guessEventDecl(ident);
  }

  private void removeIdentifierRange(final int row0, final int row1)
  {
    for (int row = row1; row >= row0; row--) {
      mEvents.remove(row);
    }
    fireTableRowsDeleted(row0, row1);
  }


  //#########################################################################
  //# Inner Class GraphModelObserver
  private class GraphModelObserver
    implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        final Proxy value = (Proxy) event.getValue();
        mIdentifierCollectVisitor.addClonedIdentifiers(value);
        break;
      case ModelChangeEvent.STATE_CHANGED:
        final Subject source = event.getSource();
        if (source == mGraph) {
          final LabelBlockSubject block = mGraph.getBlockedEvents();
          if (block != null) {
            mIdentifierCollectVisitor.addClonedIdentifiers(block);
          }
        } else {
          // more?
        }
        break;
      default:
        break;
      }
    }

  }


  //#########################################################################
  //# Inner Class EventDeclListModelObserver
  private class EventDeclListModelObserver
    implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      int kind = event.getKind();
      final AbstractSubject subject;
      switch (kind) {
      case ModelChangeEvent.ITEM_ADDED:
      case ModelChangeEvent.ITEM_REMOVED:
        subject = (AbstractSubject) event.getValue();
        if (!(subject instanceof EventDeclSubject)) {
          kind = ModelChangeEvent.STATE_CHANGED;
        }
        break;
      default:
        subject = (AbstractSubject) event.getSource(); 
        break;
      }
      final EventDeclSubject decl =
        subject.getAncestor(EventDeclSubject.class);
      final int rowcount = getRowCount();
      int row0, row1;
      switch (kind) {
      case ModelChangeEvent.ITEM_ADDED:
        if (isDisplayed()) {
          mIdentifierCollectVisitor.addClonedIdentifiers(decl);
        }
        // fall through ...
      case ModelChangeEvent.GEOMETRY_CHANGED:
        row0 = row1 = getFirstRow(decl);
        while (row1 < rowcount && guessEventDecl(row1) == decl) {
          row1++;          
        }
        if (row0 < row1) {
          fireTableRowsUpdated(row0, row1 - 1);
        }
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        boolean deleting = false;
        row0 = -1;
        for (row1 = getFirstRow(decl);
             row1 < getRowCount() && guessEventDecl(row1) == decl;
             row1++) {
          final IdentifierProxy ident = getIdentifier(row1);
          if (mGraphSearchVisitor.isEventInGraph(ident)) {
            if (row0 < 0) {
              row0 = row1;
              deleting = true;
            } else if (!deleting) {
              fireTableRowsUpdated(row0, row1 - 1);
              row0 = row1;
              deleting = true;
            }              
          } else {
            if (row0 < 0) {
              row0 = row1;
              deleting = false;
            } else if (deleting) {
              removeIdentifierRange(row0, row1 - 1);
              row1 = row0;
              deleting = false;
            }              
          }
        }
        if (row0 >= 0) {
          if (deleting) {
            removeIdentifierRange(row0, row1 - 1);
          } else {
            fireTableRowsUpdated(row0, row1 - 1);
          }
        }
        break;
      case ModelChangeEvent.STATE_CHANGED:
        row0 = row1 = -1;
        for (int row = 0; row < rowcount; row++) {
          if (guessEventDecl(row) == decl) {
            if (row0 < 0) {
              row0 = row;
            }
            row1 = row;
          } else if (row0 >= 0) {
            fireTableRowsUpdated(row0, row1);
            row0 = -1;
          }
        }
        if (row0 >= 0) {
          fireTableRowsUpdated(row0, row1);
        }
        break;
      default:
        fireTableDataChanged();
        break;
      }
    }

  }


  //#########################################################################
  //# Inner Class EventEntry
  private class EventEntry implements Comparable<EventEntry>
  {

    //#######################################################################
    //# Constructors
    private EventEntry()
    {
      mName = null;
    }

    private EventEntry(final IdentifierSubject name)
    {
      mName = name;
    }


    //#######################################################################
    //# Overrides for baseclass java.lang.Object
    public String toString()
    {
      return mName == null ? "" : mName.toString();
    }

    public boolean equals(final Object partner)
    {
      if (partner != null && partner.getClass() == getClass()) {
        final EventEntry entry = (EventEntry) partner;
        return ProxyTools.equalsByContents(mName, entry.mName);
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mName == null ? 0 : mName.hashCodeByContents();
    }


    //#######################################################################
    //# Interface java.lang.Comparable
    public int compareTo(final EventEntry entry)
    {
      if (mName == null) {
        return entry.mName == null ? 0 : 1;
      } else if (entry.mName == null) {
        return -1;
      } else {
        final String s1 = toString();
        final String s2 = entry.toString();
        return s1.compareToIgnoreCase(s2);
      }
    }


    //#######################################################################
    //# Simple Access
    private IdentifierSubject getName()
    {
      return mName;
    }


    //#######################################################################
    //# Data Members
    private final IdentifierSubject mName;

  }


  //#########################################################################
  //# Inner Class IdentifierCollectVisitor
  private class IdentifierCollectVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void addClonedIdentifiers(final Proxy proxy)
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null) {
        visitLabelBlockProxy(block);
      }
      return null;
    }

    public Object visitEventDeclProxy(final EventDeclProxy decl)
      throws VisitorException
    {
      final IdentifierProxy ident = decl.getIdentifier();
      return ident.acceptVisitor(this);
    }

    public Object visitForeachEventProxy(final ForeachEventProxy foreach)
      throws VisitorException
    {
      final List<? extends Proxy> body = foreach.getBody();
      visitCollection(body);
      return null;
    }

    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitLabelBlockProxy(blocked);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final IdentifierSubject cloned =
        (IdentifierSubject) cloner.getClone(ident);
      addIdentifier(cloned);
      return null;
    }

    public Object visitEventListExpressionProxy
      (final EventListExpressionProxy expr)
      throws VisitorException
    {
      final List<? extends Proxy> eventlist = expr.getEventList();
      visitCollection(eventlist);
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
      throws VisitorException
    {
      final PlainEventListProxy props = node.getPropositions();
      return visitPlainEventListProxy(props);
    }

  }


  //#########################################################################
  //# Inner Class GraphSearchVisitor
  private class GraphSearchVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean isEventInGraph(final IdentifierProxy ident)
    {
      try {
        mIdentifier = ident;
        return (Boolean) mGraph.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Boolean visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null) {
        return (Boolean) visitLabelBlockProxy(block);
      } else {
        return false;
      }
    }

    public Boolean visitForeachEventProxy(final ForeachEventProxy foreach)
      throws VisitorException
    {
      final List<? extends Proxy> body = foreach.getBody();
      return processList(body);
    }

    public Boolean visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy blocked = graph.getBlockedEvents();
      if (blocked != null && (Boolean) visitLabelBlockProxy(blocked)) {
        return true;
      }
      return
        processList(graph.getNodes()) ||
        processList(graph.getEdges());
    }

    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
    {
      return mIdentifier.equalsByContents(ident);
    }

    public Boolean visitEventListExpressionProxy
      (final EventListExpressionProxy expr)
      throws VisitorException
    {
      final List<? extends Proxy> eventlist = expr.getEventList();
      return processList(eventlist);
    }

    public Boolean visitNodeProxy(final NodeProxy node)
      throws VisitorException
    {
      final PlainEventListProxy props = node.getPropositions();
      return (Boolean) visitPlainEventListProxy(props);
    }

    //#######################################################################
    //# Auxiliary Methods
    private Boolean processList(final Collection<? extends Proxy> list)
      throws VisitorException
    {
      for (final Proxy proxy : list) {
        final boolean found = (Boolean) proxy.acceptVisitor(this);
        if (found) {
          return true;
        }
      }
      return false;
    }

    //#######################################################################
    //# Data Members
    private IdentifierProxy mIdentifier;

  }


  //#######################################################################
  //# Data Members
  private final GraphSubject mGraph;
  private final GraphEventHandler mEventHandler;
  private final ModuleWindowInterface mRoot;
  private final List<EventEntry> mEvents;
  private final EventDeclListModelObserver mEventDeclListModelObserver;
  private final GraphModelObserver mGraphModelObserver;
  private final IdentifierCollectVisitor mIdentifierCollectVisitor;
  private final GraphSearchVisitor mGraphSearchVisitor;

}
