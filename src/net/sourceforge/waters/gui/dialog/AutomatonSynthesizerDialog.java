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

package net.sourceforge.waters.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import net.sourceforge.waters.analysis.abstraction.DefaultSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.compositional.AutomataSynthesisAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer;
import net.sourceforge.waters.analysis.compositional.CompositionalSelectionHeuristicFactory;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristicCreator;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterJScrollPane;
import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.automata.algorithms.ControllableSynthesisKindTranslator;


/**
 * @author George Hewlett, Robi Malik, Brandon Bassett
 */
public class AutomatonSynthesizerDialog extends JDialog
{

  //#######################################################################
  //# Constructor
  public AutomatonSynthesizerDialog(final WatersAnalyzerPanel panel)
  {
    super((Frame) panel.getTopLevelAncestor());
    setTitle("Supervisor synthesis");
    mAnalyzerPanel = panel;
    mAutomata = panel.getAutomataTable().getOperationArgument();
    generateGUI();
    //createComponents();
    //layoutComponents();
    setLocationRelativeTo(panel.getTopLevelAncestor());
    //mNamePrefix.requestFocusInWindow();
    setVisible(true);
   // setMinimumSize(getSize());
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

    final ActionListener RadioHandler = new ActionListener(){
      @Override
      public void actionPerformed(final ActionEvent event)
      {
         superviserChanged();
      }
    };
    //Synthesizer type
    mCompMonLabel = new JLabel("Choose Type");
    mMonRadio =  new JRadioButton("Monlithic", true);
    mCompRadio =  new JRadioButton("Compositional");

    final ButtonGroup superviserType = new ButtonGroup();
    superviserType.add(mCompRadio);
    superviserType.add(mMonRadio);

    mCompRadio.addActionListener(RadioHandler);
    mMonRadio.addActionListener(RadioHandler);

    //Controllable and Non-blocking
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

    //Supervisor Reduction
    mSupReductionLabel = new JLabel("Supervisor reduction: ");
    mSupReductionType = new JComboBox<> (DefaultSupervisorReductionFactory.class.getEnumConstants());
    mSupReductionType.setSelectedIndex(0);

    final ActionListener ReductionHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        reductionChanged();
      }
    };

    mSupReductionType.addActionListener(ReductionHandler);

    //Monolithic Supervisor Reduction
    mLocalisedSupervisor = new JCheckBox("Supervisor localization", false);
    mLocalisedSupervisor.setEnabled(false);

    //Composition heuristic
    mHeuristic = new JLabel("Selected Compositional Heuristic: ");

    final List<SelectionHeuristicCreator> list = CompositionalSelectionHeuristicFactory.getInstance().getEnumConstants();
    final Vector<SelectionHeuristicCreator> vector = new Vector<> (list);
    mSelectionHeuristic = new JComboBox<>(vector);
    mSelectionHeuristic.setEnabled(false);

    //Composition SelfloopOnlyEventsEnabled
    mCompLoopLabel = new JLabel("Compositional SelfloopOnlyEventsEnabled");
    mCompLoopEnabled = new JCheckBox("Enable");
    mCompLoopEnabled.setEnabled(false);

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

    //mCompMonLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mCompMonLabel, constraints);
    mMainPanel.add(mCompMonLabel);
    //mMonRadio
    constraints.gridx = constraints.gridx + 2;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mMonRadio, constraints);
    mMainPanel.add(mMonRadio);
    //mCompRadio
    constraints.gridy++;
    constraints.gridx = 2;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mCompRadio, constraints);
    mMainPanel.add(mCompRadio);

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

    // mHeuristicLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mHeuristic, constraints);
    mMainPanel.add(mHeuristic);
    // mSelectionHeuristic
    constraints.gridx = constraints.gridx + 2;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mSelectionHeuristic, constraints);
    mMainPanel.add(mSelectionHeuristic);

    // mCompLoopLabelLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mCompLoopLabel, constraints);
    mMainPanel.add(mCompLoopLabel);
    // mCompLoopEnabled
    constraints.gridx = constraints.gridx + 2;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mCompLoopEnabled, constraints);
    mMainPanel.add(mCompLoopEnabled);

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
  //#Using Parameter Classes

  public void generateGUI() {

    final JPanel mSuperviserPanel = new JPanel(new GridLayout(0,2));
    superviserCombobox = new JComboBox<>();
    final JLabel superviserComboboxLabel = new JLabel("Algorithms");
    AllParams = new HashMap<Integer,Parameter>();

    for (final ModelAnalyzerFactoryLoader dir : ModelAnalyzerFactoryLoader.values()) {
      try {
        final SupervisorSynthesizer s = dir.getModelAnalyzerFactory().createSupervisorSynthesizer(ProductDESElementFactory.getInstance());

        if (s != null){
          superviserCombobox.addItem(dir);
          //database of parameters
          for(final Parameter p : s.getParameters())
            AllParams.put(p.getID(),p);
        }
      } catch (NoClassDefFoundError | ClassNotFoundException | UnsatisfiedLinkError
        | AnalysisConfigurationException exception) {     }
    }


    final ActionListener Print = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        storeInDatabase();
        printMap();
      }
    };

    final JButton print = new JButton("Print Database");
    print.addActionListener(Print);

    mSuperviserPanel.add(superviserComboboxLabel);
    mSuperviserPanel.add(superviserCombobox);
    mSuperviserPanel.add(print);

    final ProductDESProxyFactory factory =  ProductDESElementFactory.getInstance();

    final ProductDESProxy des = AutomatonTools.createProductDESProxy("synchronousForAnalyzer",   mAutomata, factory);

    final ActionListener syntheisSuperviserChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {

        final ModelAnalyzerFactoryLoader tmp =
          (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

        try {

          final List<Parameter> newParams = tmp.getModelAnalyzerFactory()
            .createSupervisorSynthesizer(ProductDESElementFactory.getInstance()).getParameters();
          storeInDatabase();
          copyFromDatabase(newParams);
          mScrollParametersPanel.replaceView(newParams, des);
        } catch (AnalysisConfigurationException  | ClassNotFoundException exception) {

          exception.printStackTrace();
        }
        //re-packing causes the frame to shrink/increase to preferred size
         pack();
      }
    };

    superviserCombobox.addActionListener(syntheisSuperviserChanged);

    // superviserCombobox should have at least one item
    final ModelAnalyzerFactoryLoader first = (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

    try {
      mScrollParametersPanel = new ParameterJScrollPane(first.getModelAnalyzerFactory()
                                                        .createSupervisorSynthesizer(ProductDESElementFactory.getInstance()).getParameters(),des);
    } catch (AnalysisConfigurationException  | ClassNotFoundException exception) {
      exception.printStackTrace();
    }

    // Buttons panel ...

    final ActionListener commithandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
       ParameterCommitDialog();
      }
    };

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

    //Finally, build the full dialog ...
    add(mSuperviserPanel, BorderLayout.PAGE_START);
    add(mScrollParametersPanel, BorderLayout.CENTER);
    add(mButtonsPanel, BorderLayout.PAGE_END);
    pack();
    setVisible(true);
  }


  //Values stored in GUI Components are stored in corresponding parameter then added to the database
  public void storeInDatabase() {

    mScrollParametersPanel.commit();       //All ParameterPanels save their stored value in their corresponding parameter
    final List<Parameter> activeParameters =  mScrollParametersPanel.getParameters();

    for(final Parameter p: activeParameters) {  //overwrite stored parameters with new version
      AllParams.put(p.getID(), p);
    }
  }

  // updates the passed parameters to have same stored value as
  // corresponding one in database
  public void copyFromDatabase(final List<Parameter> newParams) {
    for(final Parameter current: newParams)
      current.updateFromParameter(AllParams.get(current.getID()));
  }

  public void printMap() {
    for (final Entry<Integer,Parameter> entry : AllParams.entrySet()) {
      entry.getValue().printValue();
    }
  }

  public void ParameterCommitDialog()
  {
    final Frame owner = (Frame) getOwner();;
    final SynthesisPopUpDialog dialog;
    final SupervisorSynthesizer synthesizer;

    final ProductDESProxyFactory factory =    ProductDESElementFactory.getInstance();
    final ProductDESProxy des =   AutomatonTools.createProductDESProxy("synchronousForAnalyzer", mAutomata, factory);
    final ModelAnalyzerFactoryLoader synth = (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

    try {

      //Generate desired synthesizer and set all its parameters to corresponding ones in database
      synthesizer = synth.getModelAnalyzerFactory()
                            .createSupervisorSynthesizer(ProductDESElementFactory.getInstance());

      final List<Parameter> parameters = synthesizer.getParameters();
      storeInDatabase();
      copyFromDatabase(parameters);

      //commit all of the values to the synthesizer
      for(final Parameter current: parameters)
        current.commitValue();

      //synthesizer.setKindTranslator(translator);

      mSynthesizer = synthesizer;

    } catch (AnalysisConfigurationException | ClassNotFoundException exception) { }

    dialog = new SynthesisPopUpDialog(owner, des);
    dispose();
    dialog.setVisible(true);
  }

  //#########################################################################
  //# Action Listeners
  /**
   * Commits the contents of this dialog to the model. This method is attached
   * to the action listener of the 'OK' button of the event editor dialog.
   */
  public void commitDialog()
  {
    final Frame owner = (Frame) getOwner();;
    final SynthesisPopUpDialog dialog;
    final SupervisorReductionFactory reduction;

    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final String prefixName = mNamePrefix.getText();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy("synchronousForAnalyzer",
                                           mAutomata, factory);
    final KindTranslator translator;
    if (mControllable.isSelected()) {
      translator = IdenticalKindTranslator.getInstance();
    } else {
      translator = ControllableSynthesisKindTranslator.getInstance();
    }

    if(mMonRadio.isSelected()) {
      final MonolithicSynthesizer synthesizer = new MonolithicSynthesizer(des, factory);
      synthesizer.setOutputName(prefixName);
      synthesizer.setKindTranslator(translator);
      synthesizer.setNonblockingSupported(mNonBlocking.isSelected());
      reduction = (SupervisorReductionFactory) mSupReductionType.getSelectedItem();
      synthesizer.setSupervisorReductionFactory(reduction);
      synthesizer.setSupervisorLocalizationEnabled
        (mLocalisedSupervisor.isSelected());
      mSynthesizer = synthesizer;
    }
    else {
      final CompositionalAutomataSynthesizer synthesizer = new CompositionalAutomataSynthesizer(des, factory, translator, AutomataSynthesisAbstractionProcedureFactory.WSOE);
      synthesizer.setOutputName(prefixName);
      synthesizer.setKindTranslator(translator);
      synthesizer.setSelfloopOnlyEventsEnabled(mCompLoopEnabled.isSelected());
      synthesizer.setSelectionHeuristic(mSelectionHeuristic.getItemAt(mSelectionHeuristic.getSelectedIndex()));
      reduction = (SupervisorReductionFactory) mSupReductionType.getSelectedItem();
      synthesizer.setSupervisorReductionFactory(reduction);
      mSynthesizer = synthesizer;
    }

    dialog = new SynthesisPopUpDialog(owner, des);
    dispose();
    dialog.setVisible(true);
  }

  private void reductionChanged()
  {
    if(mMonRadio.isSelected()) {
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
  }

  private void superviserChanged() {

    if(mCompRadio.isSelected()){
      mLocalisedSupervisor.setEnabled(false);
      mCompLoopEnabled.setEnabled(true);
      mSelectionHeuristic.setEnabled(true);
    }
    else {
      mCompLoopEnabled.setEnabled(false);
      mSelectionHeuristic.setEnabled(false);
      reductionChanged();
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
                                final ProductDESProxy des)
    {
      super(owner, des);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
    @Override
    public void succeed()
    {
      super.succeed();
        final ProductDESResult result = mSynthesizer.getAnalysisResult();
        final Collection<? extends AutomatonProxy> resultList = result.getComputedAutomata();
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
      final int size;

        final ProductDESResult result = mSynthesizer.getAnalysisResult();
        size = result.getComputedAutomata().size();
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
  private final List<AutomatonProxy> mAutomata;


  //Parameter Components
  ParameterJScrollPane mScrollParametersPanel;
  HashMap<Integer,Parameter> AllParams;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNamePrefixLabel;
  private JTextField mNamePrefix;
  private JLabel mCompMonLabel;
  private JRadioButton mCompRadio;
  private JRadioButton mMonRadio;

  // private JPanel mErrorPanel;
  // private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  private JLabel mObjectLabel;
  private JCheckBox mControllable;
  private JCheckBox mNonBlocking;
  private JLabel mSupReductionLabel;
  private JComboBox<SupervisorReductionFactory> mSupReductionType;
  private JCheckBox mLocalisedSupervisor;
  private JLabel mHeuristic;
  private JComboBox<SelectionHeuristicCreator> mSelectionHeuristic;
  private JLabel mCompLoopLabel;
  private JCheckBox mCompLoopEnabled;

  //Parameter Stuff
  private JComboBox<ModelAnalyzerFactoryLoader> superviserCombobox;

  // Analysis workers
  private SupervisorSynthesizer mSynthesizer;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6159733639861131531L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
}
