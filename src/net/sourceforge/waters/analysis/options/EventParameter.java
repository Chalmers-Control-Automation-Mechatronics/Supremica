//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * A configurable parameter to pass a proposition event ({@link EventProxy})
 * to a {@link ModelAnalyzer}.
 *
 * @author Brandon Bassett
 */

public class EventParameter extends Parameter
{
  public EventParameter(final EventParameter template)
  {
   this(template.getID(), template.getName(), template.getDescription(), template.getNullOptions());
  }

  public EventParameter(final int id,
                        final String name,
                        final String description,
                        final EventParameterType type)
  {
    super(id, name, description);
    mValue = null;
    mNullOptions = type;
  }

  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final List<EventProxy> propositions = new ArrayList<>();
    DESContext = model;

    final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
    final EventProxy noEvent = factory.createEventProxy("(none)", EventKind.PROPOSITION);

    for(final EventProxy event: model.getProductDES().getEvents()) {
      if(event.getKind() == EventKind.PROPOSITION) {
        propositions.add(event);
        //if :accepting exists and this is first creation
        if(event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME) && mValue == null)
          mValue = event;
      }
    }

    // 1 - Null is not allowed, and the default is :accepting if available,
    //     otherwise the first proposition in the model. (If there is no
    //     proposition in the model, use null as the only option.)
    if(mNullOptions == EventParameterType.PREVENT_NULL) {
      //:accepting not available
      if(mValue == null) {
        if(propositions.size() > 0)
          mValue = propositions.get(0);
        else {
          propositions.add(noEvent);
          mValue = noEvent;
        }
      }
    }
    // 2 - Null is allowed and the default is :accepting if available,
    //     otherwise the first proposition in the model, or null if
    //     there is no proposition in the model. (Unlike case 1,
    //     null is also an option if there are propositions in the model.)
    else if(mNullOptions == EventParameterType.ALLOW_NULL) {
      propositions.add(noEvent);
      //:accepting not available
      if(mValue == null) {
          mValue = propositions.get(0);
      }
    }
    // 3 - Null is allowed and is the default.
    else if(mNullOptions == EventParameterType.DEFAULT_NULL){
      propositions.add(noEvent);
      mValue = noEvent;
    }

    Collections.sort(propositions);

    final JComboBox<EventProxy> ret = new JComboBox<>(propositions.toArray(new EventProxy[propositions.size()]));
    final EventProxyRenderer renderer= new EventProxyRenderer();
    ret.setRenderer(renderer);
    ret.setSelectedItem(mValue);
    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<EventProxy> comboBox = (JComboBox<EventProxy>) comp;
    mValue = (EventProxy) comboBox.getSelectedItem();
  }

  public EventProxy getValue() { return mValue; }

  @SuppressWarnings("unchecked")
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<String> comboBox = (JComboBox<String>) comp;
    comboBox.setSelectedItem(mValue);
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((EventParameter) p).getValue();
  }

  public EventParameterType getNullOptions() {
    return mNullOptions;
  }

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() +" Value: " + getValue());
  }

  //#########################################################################
  //# Private Class

  private class EventProxyRenderer extends JLabel implements ListCellRenderer<EventProxy>
  {
    private static final long serialVersionUID = 1L;

    public EventProxyRenderer()
    {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends EventProxy> list,
                                                  final EventProxy value, final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {

      //Highlight when hover over
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      //Set the icon and text.  If icon null, show name.
      final Icon icon = DESContext.getEventIcon(value);
      setIcon(icon);
      if (icon != null) {
        setText(value.getName());
      } else {
        setText(value.getName());
      }

      return this;
    }
  }

  //#########################################################################
  //# Data Members
  private EventProxy mValue;
  private final EventParameterType mNullOptions;
  private ProductDESContext DESContext;
}
