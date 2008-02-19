//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: EventListCell.java,v 1.8 2008-02-19 02:56:50 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.module.EventDeclProxy;


class EventListCell
  extends JLabel
  implements ListCellRenderer
{

  //#########################################################################
  //# Constructor
  EventListCell(final ModuleContext context)
  {
    mContext = context;
  }


  //#########################################################################
  //# Interface javax.swing.ListCellRenderer
  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus)
  {
    final EventDeclProxy decl = (EventDeclProxy) value;
    final String text = HTMLPrinter.getHTMLString(decl);
    final Icon icon = mContext.getIcon(decl);
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


  //#########################################################################
  //# Data Members
  private final ModuleContext mContext;

}
