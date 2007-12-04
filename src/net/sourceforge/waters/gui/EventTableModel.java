//# -*-  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventTableModel
//###########################################################################
//# $Id: EventTableModel.java,v 1.31 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.AddEventCommand;
import net.sourceforge.waters.gui.command.RemoveEventCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachEventSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


/**
 * <p>A table model for the events pane.</P>
 *
 * @author Robi Malik
 */

public class EventTableModel
  extends AbstractTableModel
  implements ModelObserver
{

  //#########################################################################
  //# Constructors
  EventTableModel(final GraphSubject graph,
                  final ModuleWindowInterface root,
                  final EditorEvents table)
  {
    mTable = table;
    mGraph = graph;
    mRoot = root;
    mEvents = collectEvents();
    final ModuleSubject module = mRoot.getModuleSubject();
    final ListSubject<EventDeclSubject> events =
      module.getEventDeclListModifiable();
    events.addModelObserver(this);
    graph.addModelObserver(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    final Subject source = event.getSource();
    final Object value = event.getValue();
    switch (event.getKind()) {
    case ModelChangeEvent.ITEM_ADDED:
      if (source.getParent() instanceof EventListExpressionSubject) {
        if (value instanceof IdentifierSubject) {
          final IdentifierSubject ident = (IdentifierSubject) value;
          addIdentifier(ident);
        } else if (value instanceof ForeachEventSubject) {
          final ForeachEventSubject foreach = (ForeachEventSubject) value;
          final List<AbstractSubject> body = foreach.getBodyModifiable();
          addIdentifiers(body);
        }
      } else if (value instanceof EventDeclSubject &&
                 mTable.isDisplayedEditor()) {
        final EventDeclSubject decl = (EventDeclSubject) value;
        final String name = decl.getName();
        final IdentifierSubject ident = new SimpleIdentifierSubject(name);
        addIdentifier(ident);
      }
      break;
    case ModelChangeEvent.ITEM_REMOVED:
      if (value instanceof EventDeclSubject) {
        final EventDeclSubject decl = (EventDeclSubject) value;
        final String name = decl.getName();
        removeIdentifiers(name);
      }
      break;
    default:
      break;
    }
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
      return ImageIcon.class;
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

  public void addIdentifier(final IdentifierSubject value)
  {
    final IdentifierSubject ident = value == null ? null : value.clone();
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index < 0) {
      final int inspoint = -index - 1;
      mEvents.add(inspoint, entry);
      fireTableRowsInserted(inspoint, inspoint);
    }
  }

  private void addIdentifiers(final List<AbstractSubject> list)
  {
    for (final AbstractSubject item : list) {
      if (item instanceof IdentifierSubject) {
        final IdentifierSubject ident = (IdentifierSubject) item;
        addIdentifier(ident);
      } else if (item instanceof ForeachEventSubject) {
        final ForeachEventSubject foreach = (ForeachEventSubject) item;
        final List<AbstractSubject> body = foreach.getBodyModifiable();
        addIdentifiers(body);
      }
    }
  }

  public void removeIdentifiers(final String name)
  {
    final IdentifierSubject tester = new SimpleIdentifierSubject(name);
    final EventEntry entry = new EventEntry(tester);
    final int first = Collections.binarySearch(mEvents, entry);
    if (first >= 0) {
      final int size = mEvents.size();
      int last = first;
      for (int index = first + 1; index < size; index++) {
        final IdentifierSubject ident = getEvent(index);
        if (ident.getName().equals(name)) {
          last++;
        } else {
          break;
        }
      }
      for (int index = last; index >= first; index--) {
        mEvents.remove(index);
      }
      fireTableRowsDeleted(first, last);
    }
  }

  public void removeIdentifier(final IdentifierSubject ident)
  {
    final EventEntry entry = new EventEntry(ident);
    final int index = Collections.binarySearch(mEvents, entry);
    if (index >= 0) {
      mEvents.remove(index);
      fireTableRowsDeleted(index, index);
    }
  }

  public void setValueAt(final Object value,
                         final int row,
                         final int column)
  {
    switch (column) {
      case 0:
        return;
      case 1:
        final IdentifierSubject ident =
          value == null ? null : ((IdentifierSubject) value).clone();
        final IdentifierSubject old = getEvent(row);
        if (ident == null) {
          Command c = new RemoveFromTableCommand(this, old);
          mTable.getEditorInterface().getUndoInterface().executeCommand(c);
        } else if (old == null) {
          Command c = new AddToTableCommand(this, ident);
          mTable.getEditorInterface().getUndoInterface().executeCommand(c);
        } else if (!old.equalsByContents(ident)) {
          Command c = new ChangeEventNameCommand(mGraph, this, old, ident);
          mTable.getEditorInterface().getUndoInterface().executeCommand(c);
        }
        return;
      default:
        throw new ArrayIndexOutOfBoundsException
          ("Bad column number for event table model!");
    }
  }


  //#########################################################################
  //# More Specific Access
  IdentifierSubject getEvent(final int row)
  {
    final EventEntry entry = mEvents.get(row);
    return entry.getName();
  }

  int createEvent()
  {
    final int row = mEvents.size();
    final EventEntry entry = new EventEntry();
    mEvents.add(entry);
    fireTableRowsInserted(row, row);
    return row;
  }

  String getToolTipText(final int row)
  {
    final ModuleContext context = mRoot.getModuleContext();
    final IdentifierSubject event = getEvent(row);
    return context.guessEventToolTipText(event);
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


  //#########################################################################
  //# Local Class EventEntry
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
        return
          mName == null ? entry.mName == null :
          mName.equalsByContents(entry.mName);
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
  //# Local Class ChangeEventNameCommand
  private class ChangeEventNameCommand
    implements Command
  {
    public ChangeEventNameCommand(final GraphSubject graph,
                                  final EventTableModel model,
                                  final IdentifierSubject old,
                                  final IdentifierSubject neo)
    {
      mGraph = graph;
      mOld = old;
      mNew = neo;
      mModel = model;
      mCommands = new CompoundCommand();
      if (mOld != null) {
        mCommands.addCommand(new RemoveFromTableCommand(mModel, mOld));
      }
      if (!mModel.containsEvent(mNew)) {
        mCommands.addCommand(new AddToTableCommand(mModel, mNew));
      }
      if (mOld != null) {
        addCommandsFromList(mGraph.getBlockedEvents());
        for (final EdgeSubject edge : mGraph.getEdgesModifiable()) {
          addCommandsFromList(edge.getLabelBlock());
        }
      }
      mCommands.end();
    }

    /**
     * Renames edge labels to reflect a rename operation in the event list
     * panel.
     */
    private void addCommandsFromList(final EventListExpressionSubject list)
    {
      if (list != null) {
        final ListIterator<AbstractSubject> iter =
          list.getEventListModifiable().listIterator();
        boolean removal = false;
        boolean add = true;
        int index = 0;
        while (iter.hasNext()) {
          final AbstractSubject a = iter.next();
          if (a.equalsByContents(mNew)) {
            add = false;
          }
          if (a.equalsByContents(mOld)) {
            mCommands.addCommand(new RemoveEventCommand(list, a));
            removal = true;
            index = iter.nextIndex() - 1;
          }
        }
        if (add && removal) {
          mCommands.addCommand(new AddEventCommand(list, mNew, index));
        }
      }
    }

    /**
     * Executes the Creation of the Node
     */
    public void execute()
    {
      mCommands.execute();
    }

    /**
     * Undoes the Command
     */
    public void undo()
    {
      mCommands.undo();
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return mDescription;
    }

    private final GraphSubject mGraph;
    private final IdentifierSubject mOld;
    private final IdentifierSubject mNew;
    private final EventTableModel mModel;
    private final CompoundCommand mCommands;
    private final static String mDescription = "Change Event Name";
  }


  //#########################################################################
  //# Local Class AddToTableCommand
  private class AddToTableCommand
    implements Command
  {
    private final EventTableModel mModel;
    private final IdentifierSubject mIdentifier;
    private final String mDescription = "Add Event";

    public AddToTableCommand(EventTableModel model,
                             IdentifierSubject identifier)
    {
      mModel = model;
      mIdentifier = identifier;
    }

    public void execute()
    {
      mModel.addIdentifier(mIdentifier);
    }

    /**
     * Undoes the command.
     */
    public void undo()
    {
      mModel.removeIdentifier(mIdentifier);
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return mDescription;
    }
  }


  //#########################################################################
  //# Local Class AddToTableCommand
  private class RemoveFromTableCommand
    implements Command
  {
    private final EventTableModel mModel;
    private final IdentifierSubject mIdentifier;
    private final String mDescription = "Add Event";

    public RemoveFromTableCommand(EventTableModel model,
                                  IdentifierSubject identifier)
    {
      mModel = model;
      mIdentifier = identifier;
    }

    public void execute()
    {
      mModel.removeIdentifier(mIdentifier);
    }

    /**
     * Undoes the Command.
     */
    public void undo()
    {
      mModel.addIdentifier(mIdentifier);
    }

    public boolean isSignificant()
    {
      return true;
    }

    public String getName()
    {
      return mDescription;
    }
  }


  //#######################################################################
  //# Data Members
  private final GraphSubject mGraph;
  private final ModuleWindowInterface mRoot;
  private final List<EventEntry> mEvents;
  private final EditorEvents mTable;
}
