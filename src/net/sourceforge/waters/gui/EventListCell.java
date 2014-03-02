//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;


class EventListCell
  extends JLabel
  implements ListCellRenderer<EventDeclSubject>
{

  //#########################################################################
  //# Constructor
  EventListCell(final ModuleContext context)
  {
    mContext = context;
  }


  //#########################################################################
  //# Interface javax.swing.ListCellRenderer
  @Override
  public Component getListCellRendererComponent(final JList<? extends EventDeclSubject> list,
                                                final EventDeclSubject value,
                                                final int index,
                                                final boolean isSelected,
                                                final boolean cellHasFocus)
  {
    final EventDeclProxy decl = value;
    final String text = HTMLPrinter.getHTMLString(decl, mContext);
    final Icon icon = mContext.getIcon(decl);
    final String tooltip = mContext.getToolTipText(decl);
    setText(text);
    setIcon(icon);
    setToolTipText(tooltip);
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
