//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
