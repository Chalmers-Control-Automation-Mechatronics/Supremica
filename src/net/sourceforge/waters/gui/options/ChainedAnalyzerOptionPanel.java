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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.ChainedAnalyzerOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * An option panel to edit a {@link ChainedAnalyzerOption}.
 * Consists of a combo box to select the model analyser factory and
 * a panel to configure its options.
 *
 * @author Robi Malik
 */

class ChainedAnalyzerOptionPanel
  extends EnumOptionPanel<ModelAnalyzerFactoryLoader>
  implements ActionListener
{
  //#########################################################################
  //# Constructors
  ChainedAnalyzerOptionPanel(final GUIOptionContext context,
                             final ChainedAnalyzerOption option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.options.OptionEditor<ModelAnalyzerFactoryLoader>
  @Override
  public ChainedAnalyzerOption getOption()
  {
    return (ChainedAnalyzerOption) super.getOption();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.gui.Options.OptionPanel<ModelAnalyzerFactoryLoader>
  @Override
  JComboBox<ModelAnalyzerFactoryLoader> createEntryComponent()
  {
    final JComboBox<ModelAnalyzerFactoryLoader> comboBox =
      super.createEntryComponent();
    comboBox.addActionListener(this);
    return comboBox;
  }

  @Override
  void addComponentsToPanel(final JPanel panel,
                            final GridBagConstraints constraints)
  {
    super.addComponentsToPanel(panel, constraints);
    final ChainedAnalyzerOption option = getOption();
    final ChainedAnalyzerOptionPage subPage = option.getOptionPage();
    final List<Option<?>> subOptions = subPage.getCurrentOptions();
    final GUIOptionContext context = getContext();
    mSubPanel = new SimpleLeafOptionPagePanel(context, null, subOptions);
    updateBorder(subOptions);
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 2;
    panel.add(mSubPanel, constraints);
  }

  @Override
  public void commitValue()
  {
    super.commitValue();
    mSubPanel.commitOptions();
  }

  @Override
  void search(final SearchQuery query)
  {
    super.search(query);
    mSubPanel.search(query);
  }

  @Override
  boolean scrollToVisible(final OptionPanel<?> option)
  {
    if (super.scrollToVisible(option)) {
      return true;
    } else {
      return mSubPanel.scrollToVisible(option);
    }
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final JComboBox<ModelAnalyzerFactoryLoader> comboBox =
      getEntryComponent();
    final int index = comboBox.getSelectedIndex();
    final ModelAnalyzerFactoryLoader loader = comboBox.getItemAt(index);
    final ChainedAnalyzerOption option = getOption();
    final ChainedAnalyzerOptionPage subPage = option.getOptionPage();
    final List<Option<?>> subOptions = subPage.getOptions(loader);
    mSubPanel.replaceOptions(subOptions);
    updateBorder(subOptions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateBorder(final List<Option<?>> subOptions)
  {
    if (subOptions.isEmpty()) {
      mSubPanel.setBorder(null);
    } else {
      final Border border =
        BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
      mSubPanel.setBorder(border);
    }
  }


  //#########################################################################
  //# Data Members
  private SimpleLeafOptionPagePanel mSubPanel;

}
