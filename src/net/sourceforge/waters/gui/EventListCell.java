
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: EventListCell.java,v 1.3 2005-02-21 11:01:59 knut Exp $
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
import net.sourceforge.waters.xsd.base.EventKind;

public class EventListCell
	extends JLabel
	implements ListCellRenderer
{
	final static ImageIcon controllableIcon = new ImageIcon(EventListCell.class.getResource("/icons/waters/controllable.gif"));
	final static ImageIcon uncontIcon = new ImageIcon(EventListCell.class.getResource("/icons/waters/uncontrollable.gif"));
	final static ImageIcon propIcon = new ImageIcon(EventListCell.class.getResource("/icons/waters/proposition.gif"));

	public Component getListCellRendererComponent(JList list, Object value,    // value to display
			int index,    // cell index
			boolean isSelected,    // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
	{
		String name;
		ImageIcon icon = null;

		name = ((EventDeclProxy) value).getNameWithRanges();

		if (((EventDeclProxy) value).getKind().equals(EventKind.CONTROLLABLE))
		{
			icon = controllableIcon;
		}
		else if (((EventDeclProxy) value).getKind().equals(EventKind.UNCONTROLLABLE))
		{
			icon = uncontIcon;
		}
		else
		{
			icon = propIcon;
		}

		setText(name);
		setIcon(icon);

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
