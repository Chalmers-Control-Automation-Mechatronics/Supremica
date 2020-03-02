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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
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

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    mGroupPanel = (OptionGroupPanel) getOptionPage().createEditor(mContext);
    add(mGroupPanel, constraints);
    mGroupPanel.setSelectionChangedListener
      (new OptionGroupPanel.SelectionChangedListener() {
      @Override
      public void selectionChanged()
      {
        updateModelAnalyzer();
        pack();
      }
    });

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

    updateModelAnalyzer();
    pack();
    setLocationRelativeTo(mContext.getIDE());
    setVisible(true);
  }


  //#########################################################################
  //# Simple Access
  public GUIOptionContext getContext()
  {
    return mContext;
  }

  protected ModelAnalyzer getAnalyzer()
  {

    return mCurrentModelAnalyzer;
  }

  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return mContext.getProductDESProxyFactory();
  }

  protected abstract OptionPage getOptionPage();


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
      mGroupPanel.commitOptions();
      for (final Option<?> option : mGroupPanel.getSelectedOptions()) {
        mCurrentModelAnalyzer.setOption(option);
      }
      final ProductDESProxy des = mContext.getProductDES();
      final WatersAnalyzeDialog dialog = createAnalyzeDialog(ide, des);
      dispose();
      if (dialog != null) {
        dialog.setVisible(true);
      }
    }
  }

  private void updateModelAnalyzer()
  {
    try {
      final ModelAnalyzerFactoryLoader loader =
        (ModelAnalyzerFactoryLoader) mGroupPanel.getSelectedValue();
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      mCurrentModelAnalyzer = createAnalyzer(factory);
    } catch (ClassNotFoundException |
             AnalysisConfigurationException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final OptionGroupPanel mGroupPanel;
  private ModelAnalyzer mCurrentModelAnalyzer;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
