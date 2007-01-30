//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: EventListCell.java,v 1.7 2007-01-30 08:51:28 flordal Exp $
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
		final String text = HTMLPrinter.getHTMLString(decl);
		final EventKind kind = decl.getKind();
		ImageIcon icon = null;
		if (kind.equals(EventKind.CONTROLLABLE)) {
			icon = IconLoader.ICON_CONTROLLABLE;
		} else if (kind.equals(EventKind.UNCONTROLLABLE)) {
			icon = IconLoader.ICON_UNCONTROLLABLE;
		} else if (kind.equals(EventKind.PROPOSITION)) {
                    if (decl.getName().equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                        icon = IconLoader.ICON_FORBIDDEN;
                    else
                        icon = IconLoader.ICON_PROPOSITION;
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
}
