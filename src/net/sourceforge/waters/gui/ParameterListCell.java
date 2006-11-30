//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ParameterListCell
//###########################################################################
//# $Id: ParameterListCell.java,v 1.8 2006-11-30 01:58:05 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.SimpleParameterProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class ParameterListCell
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
		final ParameterProxy param = (ParameterProxy) value;
		final String text = HTMLPrinter.getHTMLString(param);
		setText(text);

		ImageIcon icon = null;
		if (value instanceof EventParameterProxy) {
			final EventParameterProxy eparam = (EventParameterProxy) param;
			final EventDeclProxy decl = eparam.getEventDecl();
			final EventKind kind = decl.getKind();
			if (kind.equals(EventKind.CONTROLLABLE)) {
				icon = IconLoader.ICON_CONTROLLABLE;
			} else if (kind.equals(EventKind.UNCONTROLLABLE)) {
				icon = IconLoader.ICON_UNCONTROLLABLE;
			} else if (kind.equals(EventKind.PROPOSITION)) {
				icon = IconLoader.ICON_PROPOSITION;
			}
		} else if (value instanceof SimpleParameterProxy) {
			icon = IconLoader.ICON_SIMPLEPARAM;
		} else {
			throw new ClassCastException("Can't render parameter of class " +
										 value.getClass().getName() + "!");
		}
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
