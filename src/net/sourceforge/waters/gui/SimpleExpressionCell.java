//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: SimpleExpressionCell.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
import net.sourceforge.waters.model.expr.IdentifierProxy;

class SimpleExpressionCell extends JLabel implements ListCellRenderer {
    final static ImageIcon controllableIcon = new ImageIcon(EventListCell.class.getResource("/icons/waters/controllable.gif"));
    final static ImageIcon uncontIcon = new ImageIcon(EventListCell.class.getResource("/icons/waters/uncontrollable.gif"));
    final static ImageIcon propIcon = new ImageIcon(EventListCell.class.getResource("/icons/waters/proposition.gif"));
    private ModuleProxy module;

    public Component getListCellRendererComponent(
                                                  JList list,
                                                  Object value,            // value to display
                                                  int index,               // cell index
                                                  boolean isSelected,      // is the cell selected
                                                  boolean cellHasFocus)    // the list and the cell have the focus
    {
        String name;
	ImageIcon icon = null;
	EventDeclProxy e = null;
	if (module != null){
	    for (int i = 0; i < module.getEventDeclList().size(); i++){
		if (((IdentifierProxy)value).getName().equals(((EventDeclProxy)module.getEventDeclList().get(i)).getName())){
		    e = (EventDeclProxy)module.getEventDeclList().get(i);
		}
	    }
	}
        name = ((IdentifierProxy)value).toString();

	if(e == null){
	    icon = propIcon;
	}
	else if(e.getKind().equals(EventKind.CONTROLLABLE)) {
	    icon = controllableIcon;
	}
	else if(e.getKind().equals(EventKind.UNCONTROLLABLE)) {
	    icon = uncontIcon;
	}
	else {
	    icon = propIcon;
	}

        setText(name);
	setIcon(icon);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }

    public SimpleExpressionCell(ModuleProxy m){
	module = m;
    }
}
