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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.SimpleLeafOptionPage;

/**
 *
 * @author Benjamin Wheeler
 */
class SimpleLeafOptionPagePanel
  extends JPanel
  implements OptionPagePanel<SimpleLeafOptionPage>
{

  //#########################################################################
  //# Constructors
  SimpleLeafOptionPagePanel(final GUIOptionContext context,
                            final SimpleLeafOptionPage page)
  {
    this(context, null, page.getRegisteredOptions());
  }

  SimpleLeafOptionPagePanel(final GUIOptionContext context,
                            final Map<String, OptionPanel<?>> sharedPanels,
                            final Collection<Option<?>> options)
  {
    mContext = context;
    mSharedOptionPanels =
      sharedPanels != null ? sharedPanels : new HashMap<>();
    mOptionPanels = new LinkedList<>();
    setLayout(new GridBagLayout());
    showOptions(options);
  }


  //#########################################################################
  //# Set Up
  void replaceOptions(final Collection<Option<?>> options)
  {
    removeAll();
    mOptionPanels.clear();
    showOptions(options);
    revalidate();
  }

  private void showOptions(final Collection<Option<?>> options)
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 2, 0, 2);
    constraints.weightx = constraints.weighty = 1.0;
    final boolean persistentOnly = mContext.getWatersAnalyzerPanel() == null;
    for (final Option<?> option : options) {
      if (option.isEditable() && (!persistentOnly || option.isPersistent())) {
        final OptionPanel<?> panel = getPanel(option);
        if (panel != null) {
          mOptionPanels.add(panel);
          panel.addComponentsToPanel(this, constraints);
          constraints.gridy++;
        }
      }
    }
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.gui.options.OptionPagePanel<SimpleLeafOptionPage>
  @Override
  public JComponent asComponent()
  {
    return this;
  }

  @Override
  public JComponent asScrollableComponent()
  {
    return new JScrollPane(this);
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
    for (final OptionPanel<?> subPanel : mOptionPanels) {
      subPanel.search(query);
    }
  }

  @Override
  public boolean scrollToVisible(final OptionPanel<?> option)
  {
    for (final OptionPanel<?> subPanel : mOptionPanels) {
      if (subPanel.scrollToVisible(option)) {
        return true;
      }
    }
    return false;
  }


  //#########################################################################
  //# Auxiliary Methods
  private OptionPanel<?> getPanel(final Option<?> option)
  {
    OptionPanel<?> panel = mSharedOptionPanels.get(option.getID());
    if (panel == null) {
      panel = (OptionPanel<?>) option.createEditor(mContext);
      if (panel != null) {
        mSharedOptionPanels.put(option.getID(), panel);
      }
    }
    return panel;
  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final Map<String, OptionPanel<?>> mSharedOptionPanels;
  private final List<OptionPanel<?>> mOptionPanels;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1843000430507667498L;


}
