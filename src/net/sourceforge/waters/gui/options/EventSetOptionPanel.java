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

package net.sourceforge.waters.gui.options;

import gnu.trove.set.hash.THashSet;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.supremica.gui.ide.IDE;


class EventSetOptionPanel
  extends OptionPanel<Set<EventProxy>>
{
  //#########################################################################
  //# Constructors
  EventSetOptionPanel(final GUIOptionContext context,
                      final EventSetOption option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Simple Access
  @Override
  JPanel getEntryComponent()
  {
    return (JPanel) super.getEntryComponent();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.options.OptionEditor
  @Override
  public EventSetOption getOption()
  {
    return (EventSetOption) super.getOption();
  }

  @Override
  public boolean commitValue()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.OptionPanel
  @Override
  JPanel createEntryComponent()
  {
    setInitialValue();

    final JPanel panel = new JPanel();
    final GridBagLayout layout = new GridBagLayout();
    panel.setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 0;

    mLabel = new JTextField();
    mLabel.setEditable(false);
    mLabel.setHorizontalAlignment(JTextField.CENTER);
    updateLabel();
    constraints.weightx = 1.0;
    panel.add(mLabel, constraints);
    final JButton button = new JButton("...");
    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        showEventListDialog();
      }
    };
    button.addActionListener(listener);
    constraints.weightx = 0.0;
    panel.add(button, constraints);

    return panel;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setInitialValue()
  {
    final EventSetOption option = getOption();
    final EventKind defaultKind = option.getDefaultKind();
    final GUIOptionContext context = getContext();
    final ProductDESProxy des = context.getProductDES();
    final Set<EventProxy> events = des.getEvents();
    final Set<EventProxy> selected = new THashSet<>(events.size());
    for (final EventProxy event : events) {
      if (event.getKind() == defaultKind) {
        selected.add(event);
      }
    }
    option.setValue(selected);
  }

  private void updateLabel()
  {
    final EventSetOption option = getOption();
    final Set<EventProxy> set = option.getValue();
    final StringBuilder builder = new StringBuilder("(");
    switch (set.size()) {
    case 0:
      builder.append("no event");
      break;
    case 1:
      final EventProxy event1 = set.iterator().next();
      builder.append(event1.getName());
      break;
    default:
      EventKind combinedKind = null;
      for (final EventProxy event : set) {
        final EventKind kind = event.getKind();
        if (combinedKind == null) {
          combinedKind = kind;
        } else if (combinedKind != kind) {
          combinedKind = null;
          break;
        }
      }
      final GUIOptionContext context = getContext();
      final ProductDESProxy des = context.getProductDES();
      int total = 0;
      for (final EventProxy event : des.getEvents()) {
        final EventKind kind = event.getKind();
        if (kind == combinedKind) {
          total++;
        } else if (combinedKind == null && kind != EventKind.PROPOSITION) {
          total++;
        }
      }
      if (set.size() == total) {
        builder.append("all ");
      }
      builder.append(set.size());
      if (combinedKind == EventKind.CONTROLLABLE) {
        builder.append(" controllable");
      } else if (combinedKind == EventKind.UNCONTROLLABLE) {
        builder.append(" uncontrollable");
      }
      builder.append(" events");
      break;
    }
    builder.append(")");
    mLabel.setText(builder.toString());
  }

  private void showEventListDialog()
  {
    final GUIOptionContext context = getContext();
    final IDE ide = context.getIDE();
    final EventListDialog dialog = new EventListDialog(ide);
    final Component parent = context.getDialogParent();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
    if (dialog.getResult() == JOptionPane.OK_OPTION) {
      final EventSetOption option = getOption();
      final Set<EventProxy> set = dialog.getSelectedEvents();
      option.setValue(set);
      updateLabel();
    }
  }


  //#########################################################################
  //# Inner Class EventListDialog
  private class EventListDialog extends JDialog
  {
    //#######################################################################
    //# Constructor
    public EventListDialog(final Frame owner)
    {
      super(owner, true);

      final GUIOptionContext context = getContext();
      final ProductDESProxy des = context.getProductDES();
      final EventSetOption option = getOption();
      final Set<EventProxy> selected = option.getValue();
      setTitle(option.getShortName());
      setLayout(new GridLayout(0, 2));

      mSelectedModel = new DefaultListModel<>();
      mUnselectedModel = new DefaultListModel<>();
      for (final EventProxy event : des.getEvents()) {
        if (event.getKind() == EventKind.PROPOSITION) {
          // skip
        } else if (selected.contains(event)) {
          mSelectedModel.addElement(event);
        } else {
          mUnselectedModel.addElement(event);
        }
      }
      final JList<EventProxy> unselectedList = createListView(mUnselectedModel);
      final JScrollPane unselectedScroll = new JScrollPane(unselectedList);
      final JList<EventProxy> selectedList = createListView(mSelectedModel);
      final JScrollPane selectedScroll = new JScrollPane(selectedList);
      final JButton selectButton =
        createButton(">>>", unselectedList, mUnselectedModel, mSelectedModel);
      final JButton unselectButton =
        createButton("<<<", selectedList, mSelectedModel, mUnselectedModel);
      final JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          mResult = JOptionPane.OK_OPTION;
          dispose();
        }
      });
      final JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          mResult = JOptionPane.CANCEL_OPTION;
          dispose();
        }
      });

      setLayout(new GridBagLayout());
      final GridBagConstraints constraints = new GridBagConstraints();
      constraints.insets = INSETS;
      constraints.gridy = 0;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;
      add(new JLabel("Non-Loop Events"), constraints);
      add(new JLabel("Loop Events"), constraints);

      constraints.gridy++;
      constraints.weightx = 1;
      constraints.weighty = 1;
      constraints.fill = GridBagConstraints.BOTH;
      add(unselectedScroll, constraints);
      add(selectedScroll, constraints);

      constraints.gridy++;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;
      add(selectButton, constraints);
      add(unselectButton, constraints);
      constraints.gridy++;
      add(okButton, constraints);
      add(cancelButton, constraints);

      pack();
    }

    //#######################################################################
    //# Simple Access
    private int getResult()
    {
      return mResult;
    }

    private Set<EventProxy> getSelectedEvents()
    {
      final int size = mSelectedModel.getSize();
      final Set<EventProxy> set = new THashSet<>(size);
      for (int i = 0; i < size; i++) {
        final EventProxy event = mSelectedModel.get(i);
        set.add(event);
      }
      return set;
    }

    //#######################################################################
    //# Auxiliary Methods
    private JList<EventProxy> createListView(final DefaultListModel<EventProxy> model)
    {
      sortModel(model);
      final JList<EventProxy> listView = new JList<>();
      listView.setModel(model);
      listView.setCellRenderer(new EventCellRenderer());
      listView.setLayoutOrientation(JList.VERTICAL);
      return listView;
    }

     private JButton createButton(final String label,
                                  final JList<EventProxy> fromList,
                                  final DefaultListModel<EventProxy> fromModel,
                                  final DefaultListModel<EventProxy> toModel)
    {
      final JButton button = new JButton(label);
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent event)
        {
          for (final EventProxy proxy : fromList.getSelectedValuesList()) {
            fromModel.removeElement(proxy);
            toModel.addElement(proxy);
          }
          sortModel(toModel);
        }
      });
      return button;
    }

    private void sortModel(final DefaultListModel<EventProxy> model)
    {
      final Object[] data =  model.toArray();
      Arrays.sort(data);
      model.clear();
      for (final Object o : data) {
        final EventProxy event = (EventProxy) o;
        model.addElement(event);
      }
    }

    //#######################################################################
    //# Data Members
    private final DefaultListModel<EventProxy> mSelectedModel;
    private final DefaultListModel<EventProxy> mUnselectedModel;
    private int mResult = JOptionPane.CANCEL_OPTION;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 4132888698192730783L;
  }


  //#########################################################################
  //# Inner Class EventCellRenderer
  private class EventCellRenderer extends JLabel
    implements ListCellRenderer<EventProxy>
  {
    //#######################################################################
    //# Interface javax.swing.ListCellRenderer<EventProxy>
    @Override
    public Component getListCellRendererComponent(final JList<? extends EventProxy> list,
                                                  final EventProxy event,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      final GUIOptionContext context = getContext();
      setText(event.getName());
      setIcon(context.getEventIcon(event));
      setBorder(new EmptyBorder(2, 2, 2, 2));
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setOpaque(true);
      return this;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 760104252849112475L;
  }


  //#########################################################################
  //# Data Members
  private JTextField mLabel;


  //#######################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
