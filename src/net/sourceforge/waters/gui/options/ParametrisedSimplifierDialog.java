//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.SelectorLeafOptionPage;
import net.sourceforge.waters.gui.analyzer.AutomataTable;
import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.dialog.ErrorLabel;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;


/**
 * Abstract class that auto-generates a GUI that is based on the provided simplifier(s) getParameters method
 * where one is provided on creation or populateAlgorithmComboBox() uses a class specific
 * list of simplifiers
 *
 * @author Benjamin Wheeler
 */

public abstract class ParametrisedSimplifierDialog extends JDialog
{
  //#########################################################################
  //# Constructor
  public ParametrisedSimplifierDialog(final WatersAnalyzerPanel panel)
  {
    super(panel.getModuleContainer().getIDE());
    final ErrorLabel errorLabel = new ErrorLabel();
    mContext = new GUIOptionContext(panel, this, errorLabel);
    mPage = OptionPage.Simplifier;

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    mGroupPanel = (OptionGroupPanel) OptionPage.Simplifier.createEditor(mContext);
    add(mGroupPanel, constraints);
    mGroupPanel.setSelectionChangedListener
      (new OptionGroupPanel.SelectionChangedListener() {
      @Override
      public void selectionChanged()
      {
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

    pack();
    setLocationRelativeTo(mContext.getIDE());

    final int numSelected = mContext.getWatersAnalyzerPanel()
      .getAutomataTable()
      .getCurrentSelection()
      .size();
    if (numSelected == 1) {
      setVisible(true);
    }
    else {
      LogManager.getLogger().error("Exactly one automaton must be selected.");
      dispose();
    }
  }

  //#########################################################################
  //# Simple Access
  public GUIOptionContext getContext()
  {
    return mContext;
  }

  //#########################################################################
  //# Auxiliary Methods
  private void commitDialog()
  {
    final IDE ide = mContext.getIDE();

    final WatersAnalyzerPanel panel = mContext.getWatersAnalyzerPanel();
    final AutomataTable table = panel.getAutomataTable();
    final List<AutomatonProxy> automata = table.getOperationArgument();
    final AutomatonProxy aut = automata.iterator().next();

    final AutomataTableModel model = panel.getAutomataTableModel();

    final Logger logger = LogManager.getLogger();

    try {

      final ProductDESProxyFactory factory =
        mContext.getProductDESProxyFactory();
      final AutomatonSimplifierCreator creator =
        (AutomatonSimplifierCreator) mGroupPanel.getSelectedValue();

      final FocusTracker tracker = ide.getFocusTracker();
      if (tracker.shouldYieldFocus(this)) {
        mGroupPanel.commitOptions();

        final AutomatonBuilder builder = creator.createBuilder(factory);
        builder.setModel(aut);

        final BooleanOption keepOriginalOption = (BooleanOption) mPage.get
          (AutomatonSimplifierFactory.
           OPTION_AutomatonSimplifierFactory_KeepOriginal);
        final boolean keepOriginal = keepOriginalOption.getBooleanValue();
        if (keepOriginal) {
          final String newName = model.getUniqueAutomatonName(aut.getName());
          builder.setOutputName(newName);
        } else {
          builder.setOutputName(aut.getName());
        }

        for (final Option<?> option : mGroupPanel.getSelectedOptions()) {
          builder.setOption(option);
        }
        builder.run();
        final AutomatonProxy result = builder.getComputedAutomaton();
        if (keepOriginal) {
          model.insertRow(result);
          final List<AutomatonProxy> autList = new ArrayList<>();
          autList.add(result);
          table.clearSelection();
          table.addToSelection(autList);
          table.scrollToVisible(autList);
        } else {
          model.replaceAutomaton(aut, result);
        }
        dispose();
      }

    } catch (final AnalysisException exception) {
      logger.error(exception.getMessage());
      return;
    }

  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final OptionGroupPanel mGroupPanel;
  private final SelectorLeafOptionPage mPage;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
