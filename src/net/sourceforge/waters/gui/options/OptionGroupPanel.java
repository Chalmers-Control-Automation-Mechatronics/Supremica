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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
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

import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.SelectorLeafOptionPage;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;

/**
 *
 * @author Benjamin Wheeler
 */
public class OptionGroupPanel extends JPanel
  implements OptionContainer<SelectorLeafOptionPage>
{

  public OptionGroupPanel(final GUIOptionContext context,
                          final SelectorLeafOptionPage page)
  {
    this(context, page, new HashMap<>());
  }

  public OptionGroupPanel(final GUIOptionContext context,
                          final SelectorLeafOptionPage page,
                          final Map<String, OptionPanel<?>> optionPanels)
  {
    mPage = page;
    mOptionPanes = new HashMap<>();
    mComboBoxes = new HashMap<>();

    generateComponents(context, page, optionPanels,
                       page.getTopSelectorOption());

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weighty = 0.0;

    final ActionListener handler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        populateOptions(context, page);
      }
    };

    for (final JComboBox<Object> comboBox : mComboBoxes.values()) {
      comboBox.addActionListener(handler);
    }

    populateOptions(context, page);
  }

  public void populateOptions(final GUIOptionContext context,
                              final SelectorLeafOptionPage page)
  {
    removeAll();

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;

    constraints.gridy = 0;
    constraints.weighty = 0.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(4, 4, 4, 4);
    final JPanel selectionPanel = new JPanel();
    selectionPanel.setLayout(new GridBagLayout());
    add(selectionPanel, constraints);

    final GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;

    EnumOption<?> selectorOption = page.getTopSelectorOption();
    EnumOption<?> finalSelectorOption = null;
    Object selectedItem = null;

    while (selectorOption != null) {
      final JComboBox<Object> comboBox = mComboBoxes.get(selectorOption);
      if (context.getWatersAnalyzerPanel() != null) {
        comboBox.removeItem(ModelAnalyzerFactoryLoader.Disabled);
      }
      c.gridx = 0;
      c.insets.right = 10;
      c.weightx = 0.0;
      c.weighty = 0.0;
      final String labelText = selectorOption.getShortName();
      if (labelText != null) {
        final JLabel label = new JLabel();
        label.setText(labelText);
        label.setBorder(BorderFactory.createLineBorder(getBackground()));
        selectionPanel.add(label, c);
      }
      c.gridx = 1;
      c.insets.right = 0;
      c.weightx = 1.0;
      selectionPanel.add(comboBox, c);
      c.gridy++;

      finalSelectorOption = selectorOption;
      selectedItem = comboBox.getSelectedItem();

      selectorOption = page.getSubSelector(selectorOption, selectedItem);
    }

    final OptionListPanel pane = getOptionListPanel(finalSelectorOption, selectedItem);
    final List<Option<?>> options = page.getOptionsForSelector(finalSelectorOption, selectedItem);
    pane.populateOptions(context, page, options);
    constraints.gridy = 1;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    add(pane, constraints);

    final String description = finalSelectorOption.getDescription(selectedItem);
    if (description != null) {
      final JTextPane descriptionTextPane = new JTextPane();
      descriptionTextPane.setContentType("text/html");
      descriptionTextPane.setBackground(getBackground());
      descriptionTextPane.setEditable(false);

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

      final StringBuilder builder = new StringBuilder();
      builder.append("<HTML><BODY STYLE=\"font-size: ");
      builder.append(IconAndFontLoader.HTML_FONT_SIZE);
      builder.append("px; font-family: serif; text-align: justify;\">");
      builder.append(description);
      builder.append("</BODY></HTML>");
      final String text = builder.toString();
      descriptionTextPane.setText(text);
      c.gridx = 0;
      c.gridwidth = 2;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.fill = GridBagConstraints.BOTH;
      c.insets.top = 4;
      selectionPanel.add(scrollDescription, c);
      c.insets.top = 0;
      c.gridwidth = 1;
    }

    revalidate();
    if (mSelectionChangedListener != null) {
      mSelectionChangedListener.selectionChanged();
    }
  }

  private OptionListPanel getOptionListPanel
    (final EnumOption<?> selectorOption, final Object key)
  {
    final Map<Object, OptionListPanel> map = mOptionPanes.get(selectorOption);
    return map.get(key);
  }

  private void addOptionListPanel(final EnumOption<?> selectorOption,
                                  final Object key,
                                  final OptionListPanel panel)
  {
    Map<Object, OptionListPanel> map = mOptionPanes.get(selectorOption);
    if (map == null) {
      map = new HashMap<>();
      mOptionPanes.put(selectorOption, map);
    }
    map.put(key, panel);
  }


  public void generateComponents(final GUIOptionContext context,
                                 final SelectorLeafOptionPage map,
                                 final Map<String, OptionPanel<?>> optionPanels,
                                 final EnumOption<?> selectorOption)
  {
    final Object[] values = selectorOption.getEnumConstants().toArray();
    final JComboBox<Object> comboBox = new JComboBox<>(values);
    comboBox.setRenderer(new ComboboxToolTipRenderer(selectorOption, 300));
    mComboBoxes.put(selectorOption, comboBox);

    final Object selectedkey = selectorOption.getValue();
    if (selectedkey != null) {
      comboBox.setSelectedItem(selectedkey);
    }

    for (final Object key : selectorOption.getEnumConstants()) {
      final EnumOption<?> subSelectorOption = map.getSubSelector(selectorOption, key);
      if (subSelectorOption != null) {
        //Has subselectors
        generateComponents(context, map, optionPanels, subSelectorOption);
      }
      else {
        //Has options
        final List<Option<?>> options = map.getOptionsForSelector(selectorOption, key);
        final OptionListPanel pane = new OptionListPanel(context, map, optionPanels, options);
        addOptionListPanel(selectorOption, key, pane);
      }
    }

  }


  @SuppressWarnings("unchecked")
  @Override
  public void commitOptions()
  {
    for (final Entry<EnumOption<?>, JComboBox<Object>> entry
        : mComboBoxes.entrySet()) {
      final Object selected = entry.getValue().getSelectedItem();
      ((EnumOption<Object>)entry.getKey()).setValue(selected);
    }
    for (final Map<Object, OptionListPanel> map : mOptionPanes.values()) {
      for (final OptionListPanel pane : map.values()) {
        pane.commitOptions();
      }
    }
  }

  @Override
  public void search(final SearchQuery query)
  {
    search(query, mPage.getTopSelectorOption());
  }

  private boolean hasSubselectors(final EnumOption<?> selectorOption)
  {
    for (final Object key : selectorOption.getEnumConstants()) {
      if (mPage.getSubSelector(selectorOption, key) != null) return true;
    }
    return false;
  }

  public void search(final SearchQuery query, final EnumOption<?> selectorOption)
  {
    final JComboBox<Object> comboBox = mComboBoxes.get(selectorOption);
    final Object selectedKey = comboBox.getSelectedItem();
    if (hasSubselectors(selectorOption)) {
      search(query, mPage.getSubSelector(selectorOption, selectedKey));
      for (final Object key : selectorOption.getEnumConstants()) {
        final EnumOption<?> subSelector = mPage.getSubSelector(selectorOption, key);
        search(query, subSelector);
      }
    } else {
      final OptionListPanel selectedPane = getOptionListPanel(selectorOption, selectedKey);
      selectedPane.search(query);
      for (final Object key : selectorOption.getEnumConstants()) {
        final OptionListPanel pane = getOptionListPanel(selectorOption, key);
        pane.search(query);
      }
    }
  }

  @Override
  public boolean selectOption(final OptionPanel<?> panel)
  {
    return selectOption(panel, mPage.getTopSelectorOption());
  }

  public boolean selectOption(final OptionPanel<?> panel,
                              final EnumOption<?> selectorOption)
  {
    final JComboBox<Object> comboBox = mComboBoxes.get(selectorOption);
    final Object selectedKey = comboBox.getSelectedItem();
    if (hasSubselectors(selectorOption)) {
      if (selectOption(panel, mPage.getSubSelector(selectorOption, selectedKey))) {
        comboBox.setSelectedItem(selectedKey);
        return true;
      }
      for (final Object key : selectorOption.getEnumConstants()) {
        if (selectOption(panel, mPage.getSubSelector(selectorOption, key))) {
          comboBox.setSelectedItem(key);
          return true;
        }
      }
    } else {
      final OptionListPanel selectedPane = getOptionListPanel(selectorOption, selectedKey);
      if (selectedPane.selectOption(panel)) {
        comboBox.setSelectedItem(selectedKey);
        return true;
      }
      for (final Object key : selectorOption.getEnumConstants()) {
        final OptionListPanel pane = getOptionListPanel(selectorOption, key);
        if (pane.selectOption(panel)) {
          comboBox.setSelectedItem(key);
          return true;
        }
      }

    }
    return false;
  }

  public Object getSelectedValue() {
    EnumOption<?> selectorOption = mPage.getTopSelectorOption();
    while (true) {
      final JComboBox<?> comboBox = mComboBoxes.get(selectorOption);
      final Object key = comboBox.getSelectedItem();
      selectorOption = mPage.getSubSelector(selectorOption, key);
      if (selectorOption == null) return key;
    }
  }

  public List<Option<?>> getSelectedOptions() {
    EnumOption<?> selectorOption = mPage.getTopSelectorOption();
    while (true) {
      final JComboBox<?> comboBox = mComboBoxes.get(selectorOption);
      final Object key = comboBox.getSelectedItem();
      final EnumOption<?> subSelectorOption =
        mPage.getSubSelector(selectorOption, key);
      if (subSelectorOption == null) {
        return mPage.getOptionsForSelector(selectorOption, key);
      }
      else selectorOption = mPage.getSubSelector(selectorOption, key);
    }
  }

  @Override
  public void revalidate()
  {
    super.revalidate();
    final Component parent = getParent();
    if (parent != null) parent.revalidate();
  }

  public void setSelectionChangedListener(final SelectionChangedListener listener) {
    mSelectionChangedListener = listener;
  }


  private class ComboboxToolTipRenderer extends DefaultListCellRenderer
  {

    public ComboboxToolTipRenderer(final EnumOption<?> selectorOption, final int toolTipWidth)
    {
      super();
      mSelectorOption = selectorOption;
      mToolTipWidth = toolTipWidth;
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      if (value != null) {

        final String text = mSelectorOption.getDescription(value);

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
    private final EnumOption<?> mSelectorOption;

    private static final long serialVersionUID = -3041815919444247332L;
  }

  public interface SelectionChangedListener
  {
    public void selectionChanged();
  }

  private final SelectorLeafOptionPage mPage;
  private final Map<EnumOption<?>, Map<Object, OptionListPanel>> mOptionPanes;
  private final Map<EnumOption<?>, JComboBox<Object>> mComboBoxes;
  private SelectionChangedListener mSelectionChangedListener;

  private static final long serialVersionUID = -6276738004584574667L;

}
