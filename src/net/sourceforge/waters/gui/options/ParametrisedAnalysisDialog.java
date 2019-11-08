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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionEditor;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.dialog.ErrorLabel;
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;


/**
 * Abstract class that auto-generates a GUI that is based on the provided algorithm(s) getParameters method
 * where one is provided on creation or populateAlgorithmComboBox() uses a class specific
 * list of algorithms
 *
 * @author Brandon Bassett
 */

public abstract class ParametrisedAnalysisDialog extends JDialog
{
  //#########################################################################
  //# Constructor
  public ParametrisedAnalysisDialog(final WatersAnalyzerPanel panel)
  {
    super(panel.getModuleContainer().getIDE());
    final ErrorLabel errorLabel = new ErrorLabel();
    mContext = new GUIOptionContext(panel, this, errorLabel);

    mOptionDB = new OptionMap();
    mCurrentParameterPanels = new LinkedList<>();

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;

    // Algorithm selector combo box
    final JPanel algorithmPanel = new RaisedDialogPanel();
    algorithmPanel.setLayout(new FlowLayout());
    final JLabel algorithmComboboxLabel = new JLabel("Algorithm");
    mAnalyzerComboBox = new JComboBox<>();
    for (final ModelAnalyzerFactoryLoader loader :
         ModelAnalyzerFactoryLoader.values()) {
      try {
        final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
        final ModelAnalyzer analyzer = createAnalyzer(factory);
        if (analyzer != null) {
          factory.registerOptions(mOptionDB);
          mAnalyzerComboBox.addItem(loader);
        }
      } catch (NoClassDefFoundError |
               ClassNotFoundException |
               UnsatisfiedLinkError |
               AnalysisConfigurationException exception) {
        // skip this factory
      }
    }
    final ActionListener algorithmChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        showAlgorithmParameters();
        pack();
      }
    };
    mAnalyzerComboBox.addActionListener(algorithmChanged);
    algorithmPanel.add(algorithmComboboxLabel);
    algorithmPanel.add(mAnalyzerComboBox);
    add(algorithmPanel, constraints);

    // Parameter list
    mParameterListPanel = new JPanel();
    mParameterListPanel.setLayout(new GridBagLayout());
    final JScrollPane scroll = new JScrollPane(mParameterListPanel);
    final JPanel scrollPanel = new RaisedDialogPanel(0);
    scrollPanel.setLayout(new GridBagLayout());
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weighty = 1.0;
    scrollPanel.add(scroll, constraints);
    add(scrollPanel, constraints);
    showAlgorithmParameters();

    // Error label
    final JPanel errorPanel = new RaisedDialogPanel();
    errorPanel.add(errorLabel);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weighty = 0.0;
    add(errorPanel, constraints);

    // Buttons
    final JPanel buttonsPanel = new JPanel();
    final ActionListener commitHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commitHandler);
    buttonsPanel.add(okButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
    buttonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
    DialogCancelAction.register(this);
    add(buttonsPanel, constraints);

    pack();
    setLocationRelativeTo(mContext.getIDE());
    setVisible(true);
  }


  //#########################################################################
  //# Updating Algorithm
  private void showAlgorithmParameters()
  {
    try {
      final int index = mAnalyzerComboBox.getSelectedIndex();
      final ModelAnalyzerFactoryLoader loader = mAnalyzerComboBox.getItemAt(index);
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      mCurrentAnalyzer = createAnalyzer(factory);
      final List<Option<?>> params = mCurrentAnalyzer.getOptions(mOptionDB);
      updateParameterList(params);
    } catch (NoClassDefFoundError |
             ClassNotFoundException |
             UnsatisfiedLinkError |
             AnalysisConfigurationException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  private void updateParameterList(final List<Option<?>> params)
  {
    mParameterListPanel.removeAll();
    mCurrentParameterPanels.clear();
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 2, 0, 2);
    constraints.weightx = constraints.weighty = 1.0;
    for (final Option<?> param : params) {
      final OptionEditor<?> editor = param.createEditor(mContext);
      final OptionPanel<?> panel = (OptionPanel<?>) editor;
      mCurrentParameterPanels.add(panel);
      final JLabel label = panel.getLabel();
      constraints.gridx = 0;
      mParameterListPanel.add(label, constraints);
      final Component entry = panel.getEntryComponent();
      constraints.gridx = 1;
      mParameterListPanel.add(entry, constraints);
      constraints.gridy++;
    }
  }


  //#########################################################################
  //# Simple Access
  public GUIOptionContext getContext()
  {
    return mContext;
  }

  protected ModelAnalyzer getAnalyzer()
  {
    return mCurrentAnalyzer;
  }

  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return mContext.getProductDESProxyFactory();
  }


  //#########################################################################
  //# Hooks
  /**
   * Class-specific way to generate a model analyser.
   */
  protected abstract ModelAnalyzer createAnalyzer(ModelAnalyzerFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Generates the pop up dialog that shows the result of using the analyser.
   */
  protected abstract WatersAnalyzeDialog createAnalyzeDialog(IDE ide,
                                                             ProductDESProxy des);


  //#########################################################################
  //# Auxiliary Methods
  private void commitDialog()
  {
    final IDE ide = mContext.getIDE();
    final FocusTracker tracker = ide.getFocusTracker();
    if (tracker.shouldYieldFocus(this)) {
      for (final OptionPanel<?> panel : mCurrentParameterPanels) {
        panel.commitValue();
        final Option<?> option = panel.getOption();
        mCurrentAnalyzer.setOption(option);
      }
      final ProductDESProxy des = mContext.getProductDES();
      final WatersAnalyzeDialog dialog = createAnalyzeDialog(ide, des);
      dispose();
      if (dialog != null) {
        dialog.setVisible(true);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final JComboBox<ModelAnalyzerFactoryLoader> mAnalyzerComboBox;
  private final JPanel mParameterListPanel;
  private final OptionMap mOptionDB;

  private ModelAnalyzer mCurrentAnalyzer;
  private final List<OptionPanel<?>> mCurrentParameterPanels;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
