//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventTableModel
//###########################################################################
//# $Id: EventTableModel.java,v 1.4 2005-02-22 22:38:42 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.xsd.base.EventKind;



/**
 * <p>A table model for the events pane.</P>
 *
 * @author Robi Malik
 */

class EventTableModel
	extends AbstractTableModel
{

	//#######################################################################
	//# Constructors
	EventTableModel(final GraphProxy graph, final ModuleProxy module)
	{
		mGraph = graph;
		mModule = module;
		mEvents = collectEvents();
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
			return IdentifierProxy.class;

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
			final IdentifierProxy ident = entry.getName();
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
			final IdentifierProxy ident = (IdentifierProxy) value;
			final IdentifierProxy old = getEvent(row);
			if (ident == null) {
				mEvents.remove(row);
				fireTableRowsDeleted(row, row);
			} else if (old == null || !old.equals(ident)) {
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
	IdentifierProxy getEvent(final int row)
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
		final IdentifierProxy event = getEvent(row);
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
	private List collectEvents()
	{
		final Collection collected = new TreeSet();
		final Collection blocked = mGraph.getBlockedEvents();
		collected.addAll(blocked);

		final Collection nodes = mGraph.getNodes();
		final Iterator nodeiter = nodes.iterator();
		while (nodeiter.hasNext()) {
			final NodeProxy node = (NodeProxy) nodeiter.next();
			final Collection props = node.getPropositions();
			collected.addAll(props);
		}

		final Collection edges = mGraph.getEdges();
		final Iterator edgeiter = edges.iterator();
		while (edgeiter.hasNext()) {
			final EdgeProxy edge = (EdgeProxy) edgeiter.next();
			final Collection labels = edge.getLabelBlock();
			collected.addAll(labels);
		}

		final List result = new ArrayList(collected.size());
		final Iterator iter = collected.iterator();
		while (iter.hasNext()) {
			final IdentifierProxy ident = (IdentifierProxy) iter.next();
			final EventEntry entry = new EventEntry(ident);
			result.add(entry);
		}
		return result;
	}



	private EventKind guessEventKind(final IdentifierProxy ident)
	{
		if (ident == null) {
			return null;
		}

		final String name = ident.getName();
		final EventDeclProxy decl = mModule.getEventDeclaration(name);

		if (decl != null)
		{
			return decl.getKind();
		}

		final ParameterProxy param = mModule.getParameter(name);

		if ((param != null) && (param instanceof EventParameterProxy))
		{
			final EventParameterProxy eparam = (EventParameterProxy) param;
			final EventDeclProxy edecl = eparam.getEventDecl();

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


		private EventEntry(final IdentifierProxy name)
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
		private IdentifierProxy getName()
		{
			return mName;
		}



		//###################################################################
		//# Data Members
		private final IdentifierProxy mName;

	}



	//#####################################################################
	//# Data Members
	private final GraphProxy mGraph;
	private final ModuleProxy mModule;
	private final List mEvents;

}
