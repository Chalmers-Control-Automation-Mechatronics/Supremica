//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: EventListCell.java,v 1.5 2005-11-09 03:20:56 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class EventListCell
	extends JLabel
	implements ListCellRenderer
{
	//#######################################################################
	//# Interface javax.swing.ListCellRenderer
	public Component getListCellRendererComponent(JList list,
												  Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus)
	{
		final EventDeclProxy decl = (EventDeclProxy) value;
		final String text = HTMLPrinter.toHTMLString(decl);
		final EventKind kind = decl.getKind();
		ImageIcon icon = null;
		if (kind.equals(EventKind.CONTROLLABLE)) {
			icon = controllableIcon;
		} else if (kind.equals(EventKind.UNCONTROLLABLE)) {
			icon = uncontIcon;
		} else if (kind.equals(EventKind.PROPOSITION)) {
			icon = propIcon;
		}
		setText(text);
		setIcon(icon);
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}


	//#######################################################################
	//# Class Constants
	static final ImageIcon controllableIcon =
	  new ImageIcon(EventListCell.class.getResource
			("/icons/waters/controllable.gif"));
	static final ImageIcon uncontIcon =
	  new ImageIcon(EventListCell.class.getResource
			("/icons/waters/uncontrollable.gif"));
	static final ImageIcon propIcon =
	  new ImageIcon(EventListCell.class.getResource
			("/icons/waters/proposition.gif"));
}
