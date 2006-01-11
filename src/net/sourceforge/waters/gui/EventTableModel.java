//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventTableModel
//###########################################################################
//# $Id: EventTableModel.java,v 1.11 2006-01-11 00:00:18 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.EventParameterSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.xsd.base.EventKind;



/**
 * <p>A table model for the events pane.</P>
 *
 * @author Robi Malik
 */

class EventTableModel
	extends AbstractTableModel
	implements ModelObserver
{  

	//#######################################################################
	//# Constructors
	EventTableModel(final GraphSubject graph,
					final ModuleSubject module,
					final JTable table)
	{
		((GraphSubject)graph).addModelObserver(this);
		mTable = table;
		mGraph = graph;
		mModule = module;
		mEvents = collectEvents();
		addTableModelListener(new TableHandler());
	}

	public void modelChanged(ModelChangeEvent e)
	{
		if(e.getSource() instanceof ListSubject &&
			e.getSource().getParent() instanceof EventListExpressionSubject)
		{
			ListSubject list = (ListSubject)e.getSource();
			for (Object o: list)
			{
				System.out.println(o);
				boolean alreadyListed = false;
				for (int i = 0; i < getRowCount(); i++)
				{
					System.out.println(i);
					if (getEvent(i).equals(o))
					{
						alreadyListed = true;
						break;
					}
				}
				if (!alreadyListed)
				{
					setValueAt(o, createEvent(), 1);
				}
			}
		}
	}

	//#######################################################################
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
		switch (column)
		{

		case 0 :
			return ImageIcon.class;

		case 1 :
			return IdentifierSubject.class;

		default :
			throw new ArrayIndexOutOfBoundsException
				("Bad column number for event table model!");
		}
	}


	public Object getValueAt(final int row, final int column)
	{
		final EventEntry entry = (EventEntry) mEvents.get(row);

		switch (column)
		{

		case 0 :
			final IdentifierSubject ident = entry.getName();
			final EventKind kind = guessEventKind(ident);

			return getIcon(kind);

		case 1 :
			return entry.getName();

		default :
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
		switch (column)
		{

		case 0 :
			return;

		case 1 :
			final IdentifierSubject ident = ((IdentifierSubject) value).clone();
			final IdentifierSubject old = getEvent(row);
			if (ident == null) {
				mEvents.remove(row);
				fireTableRowsDeleted(row, row);
			} else if (old == null || !old.equals(ident)) {
				if (old != null)
				{
					for (AbstractSubject a : mGraph.getBlockedEvents().getEventListModifiable())
					{
						if (((IdentifierSubject)a).getName().equals(old.getName()))
						{
							((IdentifierSubject)a).setName(ident.getName());
						}
					}
					for (EdgeSubject e : mGraph.getEdgesModifiable())
					{
						for (AbstractSubject a : e.getLabelBlock().getEventListModifiable())
						{
							System.out.println(((IdentifierSubject)a).getName() + " : " + old.getName());
							if (((IdentifierSubject)a).getName().equals(old.getName()))
							{
								((IdentifierSubject)a).setName(ident.getName());
							}
						}
					}
				}
				final EventEntry entry = new EventEntry(ident);
				mEvents.set(row, entry);
				fireTableRowsUpdated(row, row);
			}
			return;

		default :
			throw new ArrayIndexOutOfBoundsException
				("Bad column number for event table model!");
		}
	}



	//#######################################################################
	//# More Specific Access
	IdentifierSubject getEvent(final int row)
	{
		final EventEntry entry = (EventEntry) mEvents.get(row);

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
		final IdentifierSubject event = getEvent(row);
		final String name = event.toString();
		final int len = name.length();
		final StringBuffer buffer = new StringBuffer(len + 22);
		final EventKind kind = guessEventKind(event);
		if (kind == null) {
			buffer.append("Event");
		} else if (kind.equals(EventKind.CONTROLLABLE)) {
			buffer.append("Controllable event");
		} else if (kind.equals(EventKind.PROPOSITION)) {
			buffer.append("Proposition");
		} else if (kind.equals(EventKind.UNCONTROLLABLE)) {
			buffer.append("Uncontrollable event");
		} else {
			buffer.append("Event");
		}
		buffer.append(' ');
		buffer.append(name);
		return buffer.toString();
	}



	//#######################################################################
	//# Auxiliary Methods
	private List<EventEntry> collectEvents()
	{
		final Collection<IdentifierSubject> collected =
			new TreeSet<IdentifierSubject>();
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
		for (final IdentifierSubject ident : collected) {
			final EventEntry entry = new EventEntry(ident);
			result.add(entry);
		}
		return result;
	}


	private void collectEvents(final Collection<IdentifierSubject> dest,
							   final EventListExpressionProxy source)
	{
		collectEvents(dest, source.getEventList());
	}


	private void collectEvents(final Collection<IdentifierSubject> dest,
							   final List<? extends Proxy> source)
	{
		for (final Proxy proxy : source) {
			if (proxy instanceof ForeachEventProxy) {
				final ForeachEventProxy foreach = (ForeachEventProxy) proxy;
				final List<Proxy> body = foreach.getBody();
				collectEvents(dest, body);
			} else {
				final IdentifierSubject ident = (IdentifierSubject) proxy;
				dest.add(ident.clone());
			}
		}
	}



	private EventKind guessEventKind(final IdentifierSubject ident)
	{
		if (ident == null) {
			return null;
		}
		final String name = ident.getName();
		final IndexedList<EventDeclSubject> decls =
			mModule.getEventDeclListModifiable();
		final EventDeclSubject decl = decls.get(name);
		if (decl != null) {
			return decl.getKind();
		}
		final IndexedList<ParameterSubject> params =
			mModule.getParameterListModifiable();
		final ParameterSubject param = params.get(name);
		if (param != null && param instanceof EventParameterSubject) {
			final EventParameterSubject eparam = (EventParameterSubject) param;
			final EventDeclSubject edecl = eparam.getEventDecl();
			return edecl.getKind();
		}
		return null;
	}


	private ImageIcon getIcon(final EventKind kind)
	{
		if (kind == null)
		{
			return IconLoader.ICON_EVENT;
		}
		else if (kind.equals(EventKind.CONTROLLABLE))
		{
			return IconLoader.ICON_CONTROLLABLE;
		}
		else if (kind.equals(EventKind.PROPOSITION))
		{
			return IconLoader.ICON_PROPOSITION;
		}
		else if (kind.equals(EventKind.UNCONTROLLABLE))
		{
			return IconLoader.ICON_UNCONTROLLABLE;
		}
		else
		{
			return IconLoader.ICON_EVENT;
		}
	}

	public IdentifierWithKind createIdentifierWithKind(IdentifierSubject ip_)
	{
		return new IdentifierWithKind(ip_, guessEventKind(ip_));
	}


	//#######################################################################
	//# Local Class EventEntry
	private class EventEntry
	{

		//###################################################################
		//# Constructors
		private EventEntry()
		{
			mName = null;
		}


		private EventEntry(final IdentifierSubject name)
		{
			mName = name;
		}



		//###################################################################
		//# Overrides for baseclass java.lang.Object
		public String toString()
		{
			return mName == null ? "" : mName.toString();
		}



		//###################################################################
		//# Simple Access
		private IdentifierSubject getName()
		{
			return mName;
		}



		//###################################################################
		//# Data Members
		private final IdentifierSubject mName;

	}

	private class TableHandler implements TableModelListener
	{
		public void tableChanged(TableModelEvent e)
		{
			Collections.sort(mEvents, new StringComparator());
			mTable.repaint();
		}

		private class StringComparator implements Comparator
		{
			public int compare (Object o1, Object o2)
			{
				if (o1.toString() == "")
				{
					return 1;
				}
				if (o2.toString() == "")
				{
					return -1;
				}				
				return o1.toString().compareToIgnoreCase(o2.toString());
			}

			public boolean equals(Object o)
			{
				return this == o;
			}
		}
	}



	//#####################################################################
	//# Data Members
	private final GraphSubject mGraph;
	private final ModuleSubject mModule;
	private final List<EventEntry> mEvents;
	private final JTable mTable;

}
