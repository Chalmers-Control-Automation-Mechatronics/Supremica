
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   CustomListCell
//###########################################################################
//# $Id: CustomListCell.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.IdentifiedElementProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import java.util.ArrayList;

class CustomListCell
	extends JLabel
	implements ListCellRenderer
{
	final static ImageIcon longIcon = new ImageIcon(CustomListCell.class.getResource("/icons/waters/short.gif"));
	final static ImageIcon shortIcon = new ImageIcon(CustomListCell.class.getResource("/icons/waters/long.gif"));

	public Component getListCellRendererComponent(JList list, Object value,    // value to display
			int index,    // cell index
			boolean isSelected,    // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
	{
		String name;

		if (value.getClass().getName().endsWith("SimpleComponentProxy"))
		{
			name = ((IdentifiedElementProxy) value).getName();
		}
		else
		{
			ForeachProxy v = (ForeachProxy) value;
			ElementProxy range = v.getRange();
			ElementProxy guard = v.getGuard();

			name = "<html><b>foreach </b><i>" + ((NamedProxy) value).getName() + "</i>";

			if (range != null)
			{
				name += " <b>in<b> <i>" + range.toString() + "</i>";
			}

			if (guard != null)
			{
				name += " <b>where</b> <i>" + guard.toString() + "</i></html>";
			}
		}

		setText(name);

		//TODO: Find out how to differentiate between ForeachComponentProxy and ComponentProxy objects
		setIcon((value.getClass().getName().endsWith("SimpleComponentProxy"))
				? longIcon
				: shortIcon);

		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);

		return this;
	}
}
