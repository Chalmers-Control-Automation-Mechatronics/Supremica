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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.OptionMap.OptionSubset;

/**
 *
 * @author Benjamin Wheeler
 */
public class OptionGroupPanel extends JPanel implements OptionContainer {

  public OptionGroupPanel(final GUIOptionContext context,
                      final OptionMap map) {
    this(context, map, new HashMap<>(), map.getTopOptionSubset());
  }

  public OptionGroupPanel(final GUIOptionContext context,
                      final OptionMap map,
                      final Map<String, OptionPanel<?>> optionPanels,
                      final OptionSubset subset) {

    mOptionPanes = new HashMap<>();
    mComboBoxes = new HashMap<>();
    mRootSubset = subset;

    generateComponents(context, map, optionPanels, subset);

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weighty = 0.0;

    final JPanel selectionPanel = new JPanel();
    selectionPanel.setLayout(new GridBagLayout());


    final JTextPane descriptionTextPane = new JTextPane();
    descriptionTextPane.setContentType("text/html");
    descriptionTextPane.setBackground(getBackground());
    //Prevent selection
    for (final MouseListener l : descriptionTextPane
      .getListeners(MouseListener.class)) {
      descriptionTextPane.removeMouseListener(l);
    }

    final JScrollPane scrollDescription =
        new JScrollPane(descriptionTextPane) {
      @Override
      public Dimension getPreferredSize()
      {
        final Dimension d = super.getPreferredSize();
        d.width = 0;
        return d;
      }
      private static final long serialVersionUID = -7065386236668370127L;
    };

    final GridBagConstraints c = new GridBagConstraints();

    final ActionListener handler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        if (!e.getActionCommand().equals("sub")) {
          removeAll();
          selectionPanel.removeAll();
          constraints.gridy = 0;
          constraints.weighty = 0.0;
          constraints.fill = GridBagConstraints.HORIZONTAL;
          constraints.insets = new Insets(4, 4, 4, 4);
          add(selectionPanel, constraints);

          c.gridy = 0;
          c.fill = GridBagConstraints.HORIZONTAL;

          final JComboBox<Object> comboBox = mComboBoxes.get(subset);
          this.actionPerformed(new ActionEvent(comboBox, 0, "sub"));
          return;
        }

        for (final Entry<OptionSubset, JComboBox<Object>> entry :
          mComboBoxes.entrySet()) {
          final JComboBox<Object> comboBox = entry.getValue();
          if (comboBox == e.getSource()) {

            final OptionSubset subset = entry.getKey();

            c.gridx = 0;
            c.insets.right = 10;
            c.weightx = 0.0f;
            c.weighty = 0.0f;
            if (subset.getTitle() != null) {
              final JLabel label = new JLabel();
              label.setText(subset.getTitle());
              label.setBorder(BorderFactory.createLineBorder(getBackground()));
              selectionPanel.add(label, c);
            }
            c.gridx = 1;
            c.insets.right = 0;
            c.weightx = 1.0f;
            selectionPanel.add(comboBox, c);
            c.gridy++;

            final Object selectedKey = comboBox.getSelectedItem();
            final OptionSubset selectedSubset = subset.getSubset(selectedKey);

            if (selectedSubset.hasSubsets()) {
              final JComboBox<Object> selectedComboBox =
                mComboBoxes.get(selectedSubset);
              this.actionPerformed(new ActionEvent(selectedComboBox, 0, "sub"));
            } else {
              final OptionListPanel pane = mOptionPanes.get(selectedSubset);
              pane.populateOptions(context, map, selectedSubset);
              constraints.gridy = 1;
              constraints.weighty = 1.0;
              constraints.fill = GridBagConstraints.BOTH;
              constraints.insets = new Insets(0, 0, 0, 0);
              add(pane, constraints);

              if (selectedSubset.getDescription() != null) {
                final String text = "<body style='text-align:justify'>"
                  + selectedSubset.getDescription() + "</body>";
                descriptionTextPane.setText(text);
                descriptionTextPane.setCaretPosition(0);
                c.gridx = 0;
                c.gridwidth = 2;
                c.weightx = 1.0f;
                c.weighty = 1.0f;
                c.fill = GridBagConstraints.BOTH;
                c.insets.top = 4;
                selectionPanel.add(scrollDescription, c);
                c.insets.top = 0;
                c.gridwidth = 1;
              }

            }
            break;
          }
        }

        revalidate();

      }
    };

    for (final JComboBox<Object> comboBox : mComboBoxes.values()) {
      comboBox.addActionListener(handler);
    }

    final JComboBox<Object> topComboBox = mComboBoxes.get(subset);
    handler.actionPerformed(new ActionEvent(topComboBox, 0, ""));
  }

  public void generateComponents(final GUIOptionContext context,
                                 final OptionMap map,
                                 final Map<String, OptionPanel<?>> optionPanels,
                                 final OptionSubset subset)
  {
    if (subset.hasSubsets()) {
      final JComboBox<Object> comboBox = new JComboBox<>();
      comboBox.setRenderer(new ComboboxToolTipRenderer(subset, 300));
      mComboBoxes.put(subset, comboBox);
      for (final Object key : subset.getSubsetKeys()) {
        comboBox.addItem(key);
        generateComponents(context, map, optionPanels,
                            subset.getSubset(key));
      }
      final Object selectedkey = subset.getSelected();
      if (selectedkey != null) {
        comboBox.setSelectedItem(selectedkey);
      }
    } else {
      final OptionListPanel pane = new OptionListPanel(context, map,
                                       optionPanels, subset);
      mOptionPanes.put(subset, pane);
    }
  }

  @Override
  public void commitOptions()
  {
    for (final Entry<OptionSubset, JComboBox<Object>> entry
        : mComboBoxes.entrySet()) {
      final Object selected = entry.getValue().getSelectedItem();
      entry.getKey().setSelected(selected);
    }
    for (final OptionListPanel pane : mOptionPanes.values()) {
      pane.commitOptions();
    }
  }

  @Override
  public void search(final SearchQuery query)
  {
    search(query, mRootSubset);
  }

  public void search(final SearchQuery query, final OptionSubset subset)
  {
    if (subset.hasSubsets()) {
      final JComboBox<Object> comboBox = mComboBoxes.get(subset);
      final Object selectedKey = comboBox.getSelectedItem();
      search(query, subset.getSubset(selectedKey));
      for (final Object key : subset.getSubsetKeys()) {
        search(query, subset.getSubset(key));
      }
    } else {
      final OptionListPanel pane = mOptionPanes.get(subset);
      pane.search(query);
    }
  }

  @Override
  public boolean selectOption(final OptionPanel<?> panel)
  {
    return selectOption(panel, mRootSubset);
  }

  public boolean selectOption(final OptionPanel<?> panel,
                              final OptionSubset subset)
  {
    if (subset.hasSubsets()) {
      final JComboBox<Object> comboBox = mComboBoxes.get(subset);
      final Object selectedKey = comboBox.getSelectedItem();
      if (selectOption(panel, subset.getSubset(selectedKey))) {
        comboBox.setSelectedItem(selectedKey);
        return true;
      }
      for (final Object key : subset.getSubsetKeys()) {
        if (selectOption(panel, subset.getSubset(key))) {
          comboBox.setSelectedItem(key);
          return true;
        }
      }
    } else {
      final OptionListPanel pane = mOptionPanes.get(subset);
      return pane.selectOption(panel);
    }
    return false;
  }

  @Override
  public void revalidate()
  {
    super.revalidate();
    final Component parent = getParent();
    if (parent != null) parent.revalidate();
  }


  private class ComboboxToolTipRenderer extends DefaultListCellRenderer {

    public ComboboxToolTipRenderer(final OptionSubset subset, final int toolTipWidth)
    {
      super();
      mSubset = subset;
      mToolTipWidth = toolTipWidth;
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus) {
      if (value != null) {

        final String text = mSubset.getSubset(value).getDescription();

        if (text != null && text.length() != 0) {
          final String htmlText = "<html><p width=" + mToolTipWidth + ">"
            + text + "</p></html>";

          list.setToolTipText(htmlText);
        }
        else list.setToolTipText(null);
      }
      return super.getListCellRendererComponent(list, value, index,
                                                isSelected, cellHasFocus);
    }

    private final int mToolTipWidth;
    private final OptionSubset mSubset;

    private static final long serialVersionUID = -3041815919444247332L;
  }


  private final Map<OptionSubset, OptionListPanel> mOptionPanes;
  private final Map<OptionSubset, JComboBox<Object>> mComboBoxes;
  private final OptionSubset mRootSubset;

  private static final long serialVersionUID = -6276738004584574667L;

}