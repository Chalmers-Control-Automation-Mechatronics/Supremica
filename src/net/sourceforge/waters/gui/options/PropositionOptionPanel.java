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

package net.sourceforge.waters.gui.options;

import java.awt.Component;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.options.PropositionOption;


class PropositionOptionPanel
  extends OptionPanel<EventProxy>
{
  //#########################################################################
  //# Constructors
  PropositionOptionPanel(final GUIOptionContext context,
                         final PropositionOption option)
  {
    super(context, option);
    createMissingEvent();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionEditor
  @Override
  public PropositionOption getOption()
  {
    return (PropositionOption) super.getOption();
  }

  @Override
  public void commitValue()
  {
    final PropositionOption option = getOption();
    final JComboBox<EventProxy> comboBox = getEntryComponent();
    final int index = comboBox.getSelectedIndex();
    final EventProxy value = comboBox.getItemAt(index);
    if (value == mMissingEvent) {
      option.setValue(null);
    } else {
      option.setValue(value);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.OptionPanel
  @SuppressWarnings("unchecked")
  @Override
  JComboBox<EventProxy> getEntryComponent()
  {
    return (JComboBox<EventProxy>) super.getEntryComponent();
  }

  @Override
  JComboBox<EventProxy> createEntryComponent()
  {
    createMissingEvent();
    final PropositionOption option = getOption();
    EventProxy value = option.getValue();
    final PropositionOption.DefaultKind defaultKind = option.getDefaultKind();
    boolean valueValid = false;

    final Vector<EventProxy> choices = new Vector<>();
    final GUIOptionContext context = getContext();
    final ProductDESProxy des = context.getProductDES();
    for (final EventProxy event: des.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {
        choices.add(event);
        if (event == value) {
          valueValid = true;
        }
      }
    }
    if (defaultKind != PropositionOption.DefaultKind.PREVENT_NULL ||
        choices.isEmpty()) {
      choices.add(mMissingEvent);
    }

    if (!valueValid) {
      value = mMissingEvent;
      if (defaultKind != PropositionOption.DefaultKind.DEFAULT_NULL) {
        for (final EventProxy event : choices) {
          final String name = event.getName();
          if (name.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
            value = event;
            break;
          }
        }
      }
    }

    final JComboBox<EventProxy> comboBox = new JComboBox<>(choices);
    comboBox.setSelectedItem(value);
    final EventCellRenderer renderer= new EventCellRenderer();
    comboBox.setRenderer(renderer);
    return comboBox;
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventProxy createMissingEvent()
  {
    if (mMissingEvent == null) {
      final GUIOptionContext context = getContext();
      final ProductDESProxyFactory factory = context.getProductDESProxyFactory();
      mMissingEvent = factory.createEventProxy("(none)", EventKind.PROPOSITION);
    }
    return mMissingEvent;
  }


  //#########################################################################
  //# Inner Class EventCellRenderer
  private class EventCellRenderer extends JLabel
    implements ListCellRenderer<EventProxy>
  {
    //#######################################################################
    //# Constructor
    public EventCellRenderer()
    {
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
    }

    //#######################################################################
    //# Interface javax.swing.ListCellRenderer<EventProxy>
    @Override
    public Component getListCellRendererComponent(final JList<? extends EventProxy> list,
                                                  final EventProxy event,
                                                  final int index,
                                                  final boolean selected,
                                                  final boolean cellHasFocus)
    {
      // Highlight when hover over
      if (selected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      // Set the icon and text. If icon null, show only name.
      final GUIOptionContext context = getContext();
      final Icon icon = context.getEventIcon(event);
      setIcon(icon);
      setText(event.getName());
      return this;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -5976102332552680659L;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mMissingEvent;

}
