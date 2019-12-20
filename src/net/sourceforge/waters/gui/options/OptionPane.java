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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.OptionMap.OptionSubset;

/**
 *
 * @author Benjamin Wheeler
 */
public class OptionPane extends JScrollPane implements OptionContainer {

  public OptionPane(final GUIOptionContext context,
                    final OptionMap map,
                    final Map<String, OptionPanel<?>> optionPanels,
                    final OptionSubset subset) {
    super();
    mSharedOptionPanels = optionPanels != null ? optionPanels
      : new HashMap<>();
    mOptionPanels = new LinkedList<>();
    populateOptions(context, map, subset);
  }

  public OptionPane(final GUIOptionContext context, final OptionMap map) {
    this(context, map, null, map.getTopOptionSubset());
  }

  public void populateOptions(final GUIOptionContext context,
                              final OptionMap map,
                              final OptionSubset subset)
  {
    final JPanel internalPane = new JPanel();
    setViewportView(internalPane);

    internalPane.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 2, 0, 2);
    constraints.weightx = constraints.weighty = 1.0;

    mOptionPanels.clear();
    final boolean persistentOnly = context.getWatersAnalyzerPanel() == null;
    for (final String name : subset.getOptionNames()) {
      final Option<?> option = map.get(name);
      if (!persistentOnly || option.isPersistent()) {
        OptionPanel<?> panel = mSharedOptionPanels.get(option.getID());
        if (panel == null) {
          panel = (OptionPanel<?>) option.createEditor(context);
        }
        mSharedOptionPanels.put(option.getID(), panel);
        mOptionPanels.add(panel);
        final JLabel label = panel.getLabel();
        constraints.gridx = 0;
        internalPane.add(label, constraints);
        final Component entry = panel.getEntryComponent();
        constraints.gridx = 1;
        internalPane.add(entry, constraints);
        constraints.gridy++;
      }
    }
    revalidate();
  }

  @Override
  public void commitOptions()
  {
    for (final OptionPanel<?> panel : mOptionPanels) {
      panel.commitValue();
    }
  }

  @Override
  public void search(final SearchQuery query)
  {
    for (final OptionPanel<?> panel : mOptionPanels) {
      if (query.matches(panel.getLabel().getText())) {
        query.addResult(panel);
      }
    }
  }

  @Override
  public boolean selectOption(final OptionPanel<?> panel)
  {
    if (mOptionPanels.contains(panel)) {
      final Rectangle bounds = panel.getLabel().getBounds();
      bounds.x -= 2;
      bounds.y-= 2;
      bounds.width += 4;
      bounds.height += 4;
      ((JComponent)panel.getLabel().getParent()).scrollRectToVisible(bounds);
      return true;
    }
    return false;
  }

  private final Map<String, OptionPanel<?>> mSharedOptionPanels;
  private final List<OptionPanel<?>> mOptionPanels;

  private static final long serialVersionUID = 1843000430507667498L;

}