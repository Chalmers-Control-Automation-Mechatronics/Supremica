//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.gui.dialog;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import net.sourceforge.waters.analysis.abstraction.DefaultSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesisResult;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.automata.algorithms.ControllableSynthesisKindTranslator;


/**
 * @author George Hewlett, Robi Malik
 */
public class AutomatonSynthesizerDialog extends JDialog
{

  //#######################################################################
  //# Constructor
  public AutomatonSynthesizerDialog(final WatersAnalyzerPanel panel)
  {
    super((Frame) panel.getTopLevelAncestor());
    mAnalyzerPanel = panel;
    mAutomatonList = mAnalyzerPanel.getAutomataTable().getCurrentSelection();

    setTitle("Supervisor synthesis");
    createComponents();
    layoutComponents();

    setLocationRelativeTo(panel.getTopLevelAncestor());
    mNamePrefix.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final ActionListener commithandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };
    // Main panel ...
    mMainPanel = new RaisedDialogPanel();
    mNamePrefixLabel = new JLabel("Name Prefix:");
    mNamePrefix = new JTextField();
    mNamePrefix.setText("sup");
    mNamePrefix.addActionListener(commithandler);
    mNamePrefix.setToolTipText("Name prefix for synthesised supervisor components");
    mObjectLabel = new JLabel("Objective: ");
    mControllable = new JCheckBox("Controllable", true);
    mNonBlocking = new JCheckBox("Nonblocking", true);
    final ActionListener ObjectiveHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final JCheckBox checkbox = (JCheckBox) event.getSource();
        objectiveChanged(checkbox);
      }
    };
    mControllable.addActionListener(ObjectiveHandler);
    mNonBlocking.addActionListener(ObjectiveHandler);
    mSupReductionLabel = new JLabel("Supervisor reduction: ");
    mSupReductionType = new JComboBox<>
      (DefaultSupervisorReductionFactory.class.getEnumConstants());
    mSupReductionType.setSelectedIndex(0);
    final ActionListener ReductionHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        reductionChanged();
      }
    };
    mSupReductionType.addActionListener(ReductionHandler);
    mLocalisedSupervisor = new JCheckBox("Supervisor localization", false);
    mLocalisedSupervisor.setEnabled(false);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commithandler);
    mButtonsPanel.add(okButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
    mButtonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
    DialogCancelAction.register(this);
  }

  /**
   * Fill the panels and layout all buttons and components. It is assumed that
   * all needed components have been created by a call to
   * {@link #createComponents()} before.
   */
  private void layoutComponents()
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = INSETS;

    // First, layout the main panel ...
    final GridBagLayout mainlayout = new GridBagLayout();
    mMainPanel.setLayout(mainlayout);
    // mNameLabel
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mNamePrefixLabel, constraints);
    mMainPanel.add(mNamePrefixLabel);
    // mNameInput
    mNamePrefix.setColumns(20);
    constraints.gridx = constraints.gridx + 2;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNamePrefix, constraints);
    mMainPanel.add(mNamePrefix);
    // mObjectLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mObjectLabel, constraints);
    mMainPanel.add(mObjectLabel);
    // mControllable
    constraints.gridx = constraints.gridx + 2;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mControllable, constraints);
    mMainPanel.add(mControllable);
    // mNonBlocking
    constraints.gridy++;
    constraints.gridx = 2;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mNonBlocking, constraints);
    mMainPanel.add(mNonBlocking);
    // mSupReductionLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mSupReductionLabel, constraints);
    mMainPanel.add(mSupReductionLabel);
    // mSupReductionType
    constraints.gridx = constraints.gridx + 2;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mSupReductionType, constraints);
    mMainPanel.add(mSupReductionType);
    // mLocalisedSupervisor
    constraints.gridx = 2;
    constraints.gridy++;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mLocalisedSupervisor, constraints);
    mMainPanel.add(mLocalisedSupervisor);

    // Finally, build the full dialog ...
    final Container contents = getContentPane();
    final GridBagLayout layout = new GridBagLayout();
    contents.setLayout(layout);
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    layout.setConstraints(mMainPanel, constraints);
    contents.add(mMainPanel);

    layout.setConstraints(mButtonsPanel, constraints);
    contents.add(mButtonsPanel);
    pack();
  }


  //#########################################################################
  //# Action Listeners
  /**
   * Commits the contents of this dialog to the model. This method is attached
   * to the action listener of the 'OK' button of the event editor dialog.
   */
  public void commitDialog()
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final String prefixName = mNamePrefix.getText();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy("synchronousForAnalyzer",
                                           mAutomatonList, factory);
    mSynthesizer = new MonolithicSynthesizer(des, factory);
    mSynthesizer.setOutputName(prefixName);
    final KindTranslator translator;
    if (mControllable.isSelected()) {
      translator = IdenticalKindTranslator.getInstance();
    } else {
      translator = ControllableSynthesisKindTranslator.getInstance();
    }
    mSynthesizer.setKindTranslator(translator);
    mSynthesizer.setNonblockingSupported(mNonBlocking.isSelected());
    final SupervisorReductionFactory reduction =
      (SupervisorReductionFactory) mSupReductionType.getSelectedItem();
    mSynthesizer.setSupervisorReductionFactory(reduction);
    mSynthesizer.setSupervisorLocalizationEnabled
      (mLocalisedSupervisor.isSelected());
    mSynthesizer.setModel(des);
    final Frame owner = (Frame) getOwner();
    final SynthesisPopUpDialog dialog =
      new SynthesisPopUpDialog(owner, des, mSynthesizer);
    dispose();
    dialog.setVisible(true);
  }

  private void reductionChanged()
  {
    final SupervisorReductionFactory reduction =
      (SupervisorReductionFactory) mSupReductionType.getSelectedItem();
    if (reduction == DefaultSupervisorReductionFactory.OFF) {
      mLocalisedSupervisor.setSelected(false);
      mLocalisedSupervisor.setEnabled(false);
    } else if (reduction.isSupervisedEventRequired()) {
      mLocalisedSupervisor.setSelected(true);
      mLocalisedSupervisor.setEnabled(false);
    } else {
      mLocalisedSupervisor.setEnabled(true);
    }
  }

  private void objectiveChanged(final JCheckBox checkbox)
  {
    if (checkbox.equals(mControllable)) {
      if (!mControllable.isSelected() && !mNonBlocking.isSelected()) {
        mNonBlocking.setSelected(true);
      }
    } else {
      if (!mControllable.isSelected() && !mNonBlocking.isSelected()) {
        mControllable.setSelected(true);
      }
    }
  }

  //#########################################################################
  //# Inner Class AnalyzerDialog
  private class SynthesisPopUpDialog extends WatersAnalyzeDialog
  {
    //#######################################################################
    //# Constructor
    public SynthesisPopUpDialog(final Frame owner,
                                final ProductDESProxy des,
                                final ModelAnalyzer Verifier)
    {
      super(owner, des, Verifier);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
    @Override
    public void succeed()
    {
      super.succeed();
      final MonolithicSynthesisResult result = mSynthesizer.getAnalysisResult();
      final Collection<AutomatonProxy> resultList = result.getComputedAutomata();
      final AutomataTableModel model = mAnalyzerPanel.getAutomataTableModel();
      model.insertRows(resultList);
    }

    @Override
    protected String getAnalysisName()
    {
      return "Supervisor synthesis";
    }

    @Override
    protected String getFailureText()
    {
      return "Synthesis failed. There is no solution to the control problem.";
    }

    @Override
    protected String getSuccessText()
    {
      final MonolithicSynthesisResult result = mSynthesizer.getAnalysisResult();
      final int size = result.getComputedAutomata().size();
      switch (size) {
      case 0:
        return "The system already satisfies all control objectives. " +
               "No supervisor is needed.";
      case 1:
        return "Successfully synthesised a supervisor.";
      default:
        return "Successfully synthesised " + size + " supervisor components.";
      }
    }

    @Override
    protected ModelAnalyzer createModelAnalyzer()
    {
      return mSynthesizer;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6159733639861131531L;
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final WatersAnalyzerPanel mAnalyzerPanel;
  private final List<AutomatonProxy> mAutomatonList;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNamePrefixLabel;
  private JTextField mNamePrefix;
  // private JPanel mErrorPanel;
  // private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  private JLabel mObjectLabel;
  private JCheckBox mControllable;
  private JCheckBox mNonBlocking;
  private JLabel mSupReductionLabel;
  private JComboBox<SupervisorReductionFactory> mSupReductionType;
  private JCheckBox mLocalisedSupervisor;

  // Analysis workers
  private MonolithicSynthesizer mSynthesizer;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6159733639861131531L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
