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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.SelectorLeafOptionPage;


/**
 * <P>A panel to edit an option page with selectors.</P>
 *
 * <P>The selector leaf option page panel displays one or more combo boxes
 * to select option sets and, for the selected option set, its description
 * (if available) followed by the scrollable list of option panels.</P>
 *
 * <P>The selector leaf option page panel initialises all needed combo boxes
 * and sub-panels when it is first created, and afterwards switches between
 * the components as needed. This is done to facilitate operations like
 * search that depend on all option panels being available.</P>
 *
 * @author Benjamin Wheeler
 */

public class SelectorLeafOptionPagePanel<S>
  extends JPanel
  implements OptionPagePanel<SelectorLeafOptionPage<S>>
{

  //#########################################################################
  //# Constructors
  SelectorLeafOptionPagePanel(final GUIOptionContext context,
                              final SelectorLeafOptionPage<S> page)
  {
    mContext = context;
    mPage = page;
    mComboBoxMap = new HashMap<>();
    mPanelMap = new HashMap<>();
    final EnumOption<?> top = page.getTopSelectorOption();
    final Map<String,OptionPanel<?>> sharedPanels = new HashMap<>();
    initializeMaps(top, sharedPanels);
    setLayout(new GridBagLayout());
    createComponents();
  }


  //#########################################################################
  //# Set Up
  private void initializeMaps(final EnumOption<?> selectorOption,
                              final Map<String,OptionPanel<?>> sharedPanels)
  {
    final List<Option<?>> empty = Collections.emptyList();
    final ComboBoxHandler<?> handler = new ComboBoxHandler<>(selectorOption);
    mComboBoxMap.put(selectorOption, handler);
    for (final Object key : selectorOption.getEnumConstants()) {
      final EnumOption<?> subSelectorOption =
        mPage.getSubSelectorOption(selectorOption, key);
      if (subSelectorOption != null) {
        // has subselectors
        initializeMaps(subSelectorOption, sharedPanels);
      } else {
        // has options
        final SimpleLeafOptionPagePanel panel =
          new SimpleLeafOptionPagePanel(mContext, sharedPanels, empty);
        mPanelMap.put(key, panel);
      }
    }
  }

  /**
   * Initialises combo boxes and options panel for the first time.
   */
  private void createComponents()
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(4, 4, 4, 4);
    final JComponent selectorPanel = createSelectorPanel();
    add(selectorPanel, constraints);
    final S key = getSelectedValue();
    final JComponent descriptionPanel = createDescriptionPanel(key);
    if (descriptionPanel != null) {
      add(descriptionPanel, constraints);
    }
    final SimpleLeafOptionPagePanel panel = mPanelMap.get(key);
    final List<Option<?>> options = mPage.getOptions(key);
    panel.replaceOptions(options);
    constraints.weighty = 1.0;
    final JComponent scroll = panel.asScrollableComponent();
    add(scroll, constraints);
  }

  /**
   * Re-initialises combo boxes and options panel.
   */
  private void recreateComponents()
  {
    removeAll();
    createComponents();
    revalidate();
  }

  private JComponent createSelectorPanel()
  {
    final JPanel selectorPanel = new JPanel();
    selectorPanel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(4, 4, 4, 4);
    EnumOption<?> selectorOption = mPage.getTopSelectorOption();
    Object selectedItem = null;
    while (selectorOption != null) {
      final ComboBoxHandler<?> handler = mComboBoxMap.get(selectorOption);
      final JComboBox<?> comboBox = handler.getComboBox();
      final String labelText = selectorOption.getShortName();
      if (labelText != null) {
        final JLabel label = new JLabel();
        label.setText(labelText);
        label.setBorder(BorderFactory.createLineBorder(getBackground()));
        constraints.gridx = 0;
        constraints.weightx = 0.0;
        constraints.insets.right = 10;
        selectorPanel.add(label, constraints);
      }
      constraints.gridx = 1;
      constraints.weightx = 1.0;
      constraints.insets.right = 4;
      selectorPanel.add(comboBox, constraints);
      constraints.gridy++;
      selectedItem = comboBox.getSelectedItem();
      selectorOption = mPage.getSubSelectorOption(selectorOption, selectedItem);
    }
    return selectorPanel;
  }

  private JComponent createDescriptionPanel(final S key)
  {
    final String description = mPage.getDescription(key);
    if (description != null && description.length() > 0) {
      final int width;
      if (mDescriptionTextPane == null) {
        width = Math.round(PREFERRED_WIDTH *
                           IconAndFontLoader.GLOBAL_SCALE_FACTOR);
      } else {
        final Insets insets = mDescriptionTextPane.getInsets();
        width = mDescriptionTextPane.getWidth() - insets.left - insets.right;
      }
      final String htmlText = getHTMLText(description, width, false);
      mDescriptionTextPane = new JTextPane();
      mDescriptionTextPane.setContentType("text/html");
      mDescriptionTextPane.setBackground(getBackground());
      mDescriptionTextPane.setEditable(false);
      mDescriptionTextPane.setText(htmlText);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          final String altText = getHTMLText(description, -1, false);
          mDescriptionTextPane.setText(altText);
        }
      });
      final Border border =
        BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
      mDescriptionTextPane.setBorder(border);
      return mDescriptionTextPane;
    } else {
      return null;
    }
  }

  private String getHTMLText(final String description,
                             final int width,
                             final boolean toolTip)
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("<HTML><BODY STYLE=\"");
    if (width > 0) {
      builder.append("width: ");
      builder.append(width);
      builder.append("; ");
    }
    builder.append("font-size: ");
    builder.append(IconAndFontLoader.HTML_FONT_SIZE);
    builder.append("px;");
    if (!toolTip) {
      builder.append(" margin-left: ");
      final int margin =
        Math.round(3 * IconAndFontLoader.GLOBAL_SCALE_FACTOR);
      builder.append(margin);
      builder.append("px; text-align: justify;");
    }
    builder.append("\">");
    builder.append(description);
    builder.append("</BODY></HTML>");
    return builder.toString();
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.gui.options.OptionPagePanel<SelectorLeafOptionPage<S>>
  @Override
  public JComponent asComponent()
  {
    return this;
  }

  @Override
  public JComponent asScrollableComponent()
  {
    return this;
  }

  @Override
  public void commitOptions()
  {
    // This commits all options. Should we commit only the selected?
    for (final ComboBoxHandler<?> handler : mComboBoxMap.values()) {
      handler.commit();
    }
    for (final SimpleLeafOptionPagePanel panel : mPanelMap.values()) {
      panel.commitOptions();
    }
  }

  @Override
  public void search(final SearchQuery query)
  {
    final EnumOption<?> top = mPage.getTopSelectorOption();
    search(query, top);
  }

  @Override
  public boolean scrollToVisible(final OptionPanel<?> option)
  {
    final EnumOption<?> top = mPage.getTopSelectorOption();
    return selectOption(option, top);
  }

  @SuppressWarnings("unchecked")
  public S getSelectedValue()
  {
    EnumOption<?> selectorOption = mPage.getTopSelectorOption();
    while (true) {
      final ComboBoxHandler<?> handler = mComboBoxMap.get(selectorOption);
      final Object key = handler.getSelectedItem();
      selectorOption = mPage.getSubSelectorOption(selectorOption, key);
      if (selectorOption == null) {
        return (S) key;
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void search(final SearchQuery query,
                      final EnumOption<?> selectorOption)
  {
    final ComboBoxHandler<?> handler = mComboBoxMap.get(selectorOption);
    final Object selectedKey = handler.getSelectedItem();
    search(query, selectorOption, selectedKey);
    for (final Object key : selectorOption.getEnumConstants()) {
      if (key != selectedKey) {
        search(query, selectorOption, key);
      }
    }
  }

  private void search(final SearchQuery query,
                      final EnumOption<?> selectorOption,
                      final Object key)
  {
    final EnumOption<?> subSelectorOption =
      mPage.getSubSelectorOption(selectorOption, key);
    if (subSelectorOption == null) {
      final SimpleLeafOptionPagePanel panel = mPanelMap.get(key);
      panel.search(query);
    } else {
      search(query, subSelectorOption);
    }
  }

  private boolean selectOption(final OptionPanel<?> option,
                               final EnumOption<?> selectorOption)
  {
    final ComboBoxHandler<?> handler = mComboBoxMap.get(selectorOption);
    final Object selectedKey = handler.getSelectedItem();
    if (selectOption(option, selectorOption, selectedKey)) {
      return true;
    }
    for (final Object key : selectorOption.getEnumConstants()) {
      if (key != selectedKey) {
        if (selectOption(option, selectorOption, key)) {
          handler.setSelectedItem(key);
          return true;
        }
      }
    }
    return false;
  }

  private boolean selectOption(final OptionPanel<?> option,
                               final EnumOption<?> selectorOption,
                               final Object key)
  {
    final EnumOption<?> subSelectorOption =
      mPage.getSubSelectorOption(selectorOption, key);
    if (subSelectorOption == null) {
      final SimpleLeafOptionPagePanel panel = mPanelMap.get(key);
      if (panel.scrollToVisible(option)) {
        return true;
      }
    } else {
      if (selectOption(option, subSelectorOption)) {
        return true;
      }
    }
    return false;
  }


  //#########################################################################
  //# Inner Class ComboBoxHandler
  private class ComboBoxHandler<T>
    extends DefaultListCellRenderer
    implements ActionListener
  {
    //#######################################################################
    //# Constructor
    private ComboBoxHandler(final EnumOption<T> option)
    {
      mOption = option;
      final Vector<T> values = new Vector<>(option.getEnumConstants());
      if (mContext.getWatersAnalyzerPanel() != null) {
        values.remove(ModelAnalyzerFactoryLoader.Disabled);
      }
      mComboBox = new JComboBox<>(values);
      final T value = option.getValue();
      mComboBox.setSelectedItem(value);
      mComboBox.setRenderer(this);
      mComboBox.addActionListener(this);
    }

    //#######################################################################
    //# Access
    private JComboBox<T> getComboBox()
    {
      return mComboBox;
    }

    private void commit()
    {
      final T value = getSelectedItem();
      mOption.setValue(value);
    }

    private T getSelectedItem()
    {
      final int index = mComboBox.getSelectedIndex();
      return mComboBox.getItemAt(index);
    }

    private void setSelectedItem(final Object key)
    {
      mComboBox.setSelectedItem(key);
    }

    //#######################################################################
    //# Interface java.awt.ActionListener
    @Override
    public void actionPerformed(final ActionEvent e)
    {
      recreateComponents();
    }

    //#######################################################################
    //# Overrides for javax.swing.DefaultListCellRenderer
    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      if (value != null) {
        final String description = mPage.getDescription(value);
        if (description != null && description.length() > 0) {
          final int width = Math.round
            (0.75f * PREFERRED_WIDTH * IconAndFontLoader.GLOBAL_SCALE_FACTOR);
          final String htmlText = getHTMLText(description, width, true);
          list.setToolTipText(htmlText);
        } else {
          list.setToolTipText(null);
        }
      }
      return super.getListCellRendererComponent(list, value, index,
                                                isSelected, cellHasFocus);
    }

    //#######################################################################
    //# Data Members
    private final EnumOption<T> mOption;
    private final JComboBox<T> mComboBox;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -3041815919444247332L;
  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final SelectorLeafOptionPage<S> mPage;
  private final Map<EnumOption<?>,ComboBoxHandler<?>> mComboBoxMap;
  private final Map<Object,SimpleLeafOptionPagePanel> mPanelMap;
  private JTextPane mDescriptionTextPane;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -6276738004584574667L;

  private static final int PREFERRED_WIDTH = 480;

}
