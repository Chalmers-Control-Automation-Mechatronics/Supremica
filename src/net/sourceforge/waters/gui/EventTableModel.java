
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventTableModel
//###########################################################################
//# $Id: EventTableModel.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
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

	//#########################################################################
	//# Constructors
	EventTableModel(final Collection events, final ModuleProxy module)
	{
		mModule = module;
		mEvents = new ArrayList(events.size());

		final Iterator iter = events.iterator();

		while (iter.hasNext())
		{
			final IdentifierProxy ident = (IdentifierProxy) iter.next();
			final EventEntry entry = new EventEntry(ident);

			mEvents.add(entry);
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
		switch (column)
		{

		case 0 :
			return ImageIcon.class;

		case 1 :
			return IdentifierProxy.class;

		default :
			throw new ArrayIndexOutOfBoundsException("Bad column number for event table model!");
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
			throw new ArrayIndexOutOfBoundsException("Bad column number for event table model!");
		}
	}

	public boolean isCellEditable(final int row, final int column)
	{
		return column == 1;
	}

	public void setValueAt(final Object value, final int row, final int column)
	{
		switch (column)
		{

		case 0 :
			return;

		case 1 :
			final IdentifierProxy ident = (IdentifierProxy) value;
			final EventEntry entry = new EventEntry(ident);

			mEvents.set(row, entry);
			fireTableCellUpdated(row, 0);
			fireTableCellUpdated(row, 1);

			return;

		default :
			throw new ArrayIndexOutOfBoundsException("Bad column number for event table model!");
		}
	}

	//#########################################################################
	//# More Specific Access
	IdentifierProxy getEvent(final int row)
	{
		final EventEntry entry = (EventEntry) mEvents.get(row);

		return entry.getName();
	}

	//#########################################################################
	//# Auxiliary Methods
	private EventKind guessEventKind(final IdentifierProxy ident)
	{
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

	//#########################################################################
	//# Local Class EventEntry
	private class EventEntry
	{

		//#######################################################################
		//# Constructors
		private EventEntry(final IdentifierProxy name)
		{
			mName = name;
		}

		//#######################################################################
		//# Overrides for baseclass java.lang.Object
		public String toString()
		{
			return mName.toString();
		}

		//#######################################################################
		//# Simple Access
		private IdentifierProxy getName()
		{
			return mName;
		}

		//#######################################################################
		//# Data Members
		private final IdentifierProxy mName;
	}

	//#########################################################################
	//# Data Members
	private final ModuleProxy mModule;
	private final List mEvents;
}
