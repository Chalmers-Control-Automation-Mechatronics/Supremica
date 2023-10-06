//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.waters.gui.analyzer.EventSetPanel;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.options.EventSetOption;
import net.sourceforge.waters.model.options.EventSetOption.DefaultKind;

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
  //# Interface net.sourceforge.waters.model.options.OptionEditor
  @Override
  public EventSetOption getOption()
  {
    return (EventSetOption) super.getOption();
  }

  @Override
  public void commitValue()
  {
    final EventSetOption option = getOption();
    option.setValue(mCurrentValue);
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
    if (mCurrentValue.size() == 0) {
      button.setEnabled(false);
    }
    constraints.weightx = 0.0;
    panel.add(button, constraints);

    return panel;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setInitialValue()
  {
    final EventSetOption option = getOption();
    final DefaultKind defaultKind = option.getDefaultKind();
    final GUIOptionContext context = getContext();
    final ProductDESProxy des = context.getProductDES();
    final Set<EventProxy> events = des.getEvents();
    mCurrentValue = new THashSet<>(events.size());
    for (final EventProxy event : events) {
      if (defaultKind.isDefault(event.getKind())) {
        mCurrentValue.add(event);
      }
    }
  }

  private void updateLabel()
  {
    final StringBuilder builder = new StringBuilder("(");
    switch (mCurrentValue.size()) {
    case 0:
      builder.append("no event");
      break;
    case 1:
      final EventProxy event1 = mCurrentValue.iterator().next();
      builder.append(event1.getName());
      break;
    default:
      EventKind combinedKind = null;
      for (final EventProxy event : mCurrentValue) {
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
        } else if (combinedKind == null && !DefaultKind.PROPOSITION.isDefault(kind)) {
          total++;
        }
      }
      if (mCurrentValue.size() == total) {
        builder.append("all ");
      }
      builder.append(mCurrentValue.size());
      if (DefaultKind.CONTROLLABLE.isDefault(combinedKind)) {
        builder.append(" controllable");
      } else if (DefaultKind.UNCONTROLLABLE.isDefault(combinedKind)) {
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
    final EventSetOption option = getOption();
    final EventListDialog dialog = new EventListDialog(ide,
                                                       option.getSelectedTitle(),
                                                       option.getUnselectedTitle());
    final Component parent = context.getDialogParent();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }


  //#########################################################################
  //# Inner Class EventListDialog
  private class EventListDialog extends JDialog
  {
    //#######################################################################
    //# Constructor
    public EventListDialog(final Frame owner,
                           final String selectedTitle,
                           final String unselectedTitle)
    {
      super(owner, true);

      final EventSetOption option = getOption();
      setTitle(option.getShortName());

      final GUIOptionContext context = getContext();

      final JPanel pane = new JPanel();
      setContentPane(pane);
      pane.setLayout(new BorderLayout());

      final EventSetPanel eventPanel = new EventSetPanel(context, option, mCurrentValue);
      pane.add(eventPanel, BorderLayout.CENTER);

      final JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          mCurrentValue = eventPanel.getSelectedEvents();
          updateLabel();
          dispose();
        }
      });
      final JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          dispose();
        }
      });

      final JPanel buttonPane = new JPanel();
      pane.add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));

      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      pack();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -1436884020322819782L;


  }





  //#########################################################################
  //# Data Members
  private JTextField mLabel;
  private Set<EventProxy> mCurrentValue;


  //#######################################################################
  //# Class Constants


}
