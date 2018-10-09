//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.EditorSynthesizerOptions.ExpressionType;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.properties.Config;

//###########################################################################
//# Abstract class
abstract class EditorSynthesizerPanel extends JPanel
{
  public abstract void update(EditorSynthesizerOptions s);

  public abstract void regain(EditorSynthesizerOptions s);

  private static final long serialVersionUID = 1L;
}


public class EditorSynthesizerDialogStandardPanel
  extends EditorSynthesizerPanel implements ActionListener
{
  //#########################################################################
  //# Data members
  private static final long serialVersionUID = 1L;
  private final SynthesisSelector typeSelector;
  private final AlgorithmSelector algorithmSelector;
  private final JCheckBox reachableBox;
  private final JCheckBox peakBDDBox;
  private final JPanel guardBox;
  private final Box genGuardComputeSupBox;

  private final JCheckBox printGuardBox;
  private final JCheckBox addGuardsBox;
  private final JCheckBox saveEventGuardInFileBox;
  private final JCheckBox saveIDDInFileBox;

  private final JPanel tumPanel;
  private final JCheckBox genPLCCodeTUMBox;
  private final Box typePLCCodeTUMBox;
  private final JComboBox<String> typePLCCodeTUMSelector; // Later, this could be defined as a specific class.
  private final JCheckBox plcCodeTUMefaBox;
  private final JCheckBox savPLCCodeTUMBox;
  private final JCheckBox pouDefaultNameBox;
  private final JTextField pouNameField;
  private final Box pouNameFieldBox;

  private final static String defaultTcPOUNameField = "main_control"; // TUM TwinCAT parameter

  private final JCheckBox timeOptBox;

  private final JCheckBox complementHeuristicBox;
  private final JCheckBox independentHeuristicBox;

  private final JTextField globalClockDomainField;

  private final JRadioButton fromAllowedStatesButton;
  private final JRadioButton fromForbiddenStatesButton;

  private final JRadioButton minVarButton;
  private final JRadioButton maxVarButton;

  private final JRadioButton optimalButton;
  private final JComboBox<String> eventList;
  private final JComboBox<String> variablesList;


  //#########################################################################
  //# Internal classes
  static class AlgorithmSelector extends JComboBox<SynthesisAlgorithm>
  {
    private static final long serialVersionUID = 1L;

    private AlgorithmSelector()
    {
      super();
    }

    private AlgorithmSelector(final SynthesisAlgorithm[] array)
    {
      super(array);
    }

    private AlgorithmSelector(final SynthesisAlgorithm algo)
    {
      addItem(algo);
    }

    public SynthesisAlgorithm getAlgorithm()
    {
      return (SynthesisAlgorithm) getSelectedItem();
    }

    public void setAlgorithm(final SynthesisAlgorithm algo)
    {
      setSelectedItem(algo);
    }

    public static AlgorithmSelector create(final int num)
    {
      if (num == 1) {
        final AlgorithmSelector selector = new AlgorithmSelector();
        for (final SynthesisAlgorithm algo : SynthesisAlgorithm.values()) {
          if (!algo.prefersModular()) {
            selector.addItem(algo);
          }
        }
        return selector;
      } else {
        final AlgorithmSelector selector = new AlgorithmSelector();
        for (final SynthesisAlgorithm algo : SynthesisAlgorithm.values()) {
          selector.addItem(algo);
        }
        return selector;
      }
    }
  }


  static class SynthesisSelector extends JComboBox<SynthesisType>
  {
    private static final long serialVersionUID = 1L;

    private SynthesisSelector()
    {
      super(SynthesisType.editorValues());
    }

    public SynthesisType getType()
    {
      return (SynthesisType) getSelectedItem();
    }

    public void setType(final SynthesisType type)
    {
      setSelectedItem(type);
    }

    public static SynthesisSelector create()
    {
      return new SynthesisSelector();
    }
  }

  //#########################################################################
  //# Constructor
  public EditorSynthesizerDialogStandardPanel(final int num,
                                              final Vector<String> events,
                                              final Vector<String> variables)
  {

    algorithmSelector = AlgorithmSelector.create(num);
    algorithmSelector.addActionListener(this);

    typeSelector = SynthesisSelector.create();
    typeSelector.addActionListener(this);

    // Supervisor options
    final Box mainBox = Box.createVerticalBox();
    final JPanel supervisorPanel = new JPanel();
    supervisorPanel.setBorder(BorderFactory.createTitledBorder("Supervisor"));
    supervisorPanel.setLayout(new GridLayout(4, 1));

    JPanel panel = new JPanel();
    Box box = Box.createHorizontalBox();
    box.add(new JLabel("Property:"));
    box.add(typeSelector);
    panel.add(box);
    supervisorPanel.add(panel);

    panel = new JPanel();
    box = Box.createHorizontalBox();
    box.add(new JLabel("Algorithm: "));
    //Until now it is only possible to perform the synthesis via BDDs
    algorithmSelector.setEnabled(true);
    box.add(algorithmSelector);
    panel.add(box);
    supervisorPanel.add(panel);

    panel = new JPanel();
    box = Box.createHorizontalBox();
    reachableBox = new JCheckBox("Remove the unreachable states");
    reachableBox.setEnabled(true);
    reachableBox.setVisible(true);
    box.add(reachableBox);
    // reachableBox.addActionListener(this); // Not needed?
    panel.add(box);
    supervisorPanel.add(panel);

    panel = new JPanel();
    box = Box.createHorizontalBox();
    peakBDDBox = new JCheckBox("Compute peak BDD nodes");
    peakBDDBox.setEnabled(true);
    peakBDDBox.setVisible(true);
    box.add(peakBDDBox);
    // peakBDDBox.addActionListener(this); // Not needed?
    panel.add(box);
    supervisorPanel.add(panel);

    mainBox.add(supervisorPanel);

    // Guard computation options
    GridBagLayout layout = new GridBagLayout();
    guardBox = new JPanel(layout);
    guardBox.setBorder(BorderFactory.createTitledBorder("Guard computation"));
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    optimalButton = new JRadioButton("Adaptive solution");
    optimalButton.setToolTipText("Generate the guard from the state set "
                                 + "that yields the best result");
    optimalButton.setSelected(true);
    fromAllowedStatesButton = new JRadioButton("From allowed states");
    fromAllowedStatesButton
      .setToolTipText("Generate the guard from the "
                      + "states where the event is " + "allowed to occur");
    fromForbiddenStatesButton = new JRadioButton("From forbidden states");
    fromForbiddenStatesButton
      .setToolTipText("Generate the guard from the "
                      + "states where the event " + "is forbidden to occur");
    ButtonGroup group = new ButtonGroup();
    group.add(optimalButton);
    group.add(fromAllowedStatesButton);
    group.add(fromForbiddenStatesButton);

    complementHeuristicBox = new JCheckBox("Complement Heuristic");
    complementHeuristicBox
      .setToolTipText("Apply the 'Complement Heuristic'.");
    complementHeuristicBox.setEnabled(true);
    complementHeuristicBox.setSelected(true);
    independentHeuristicBox = new JCheckBox("Independent Heuristic");
    independentHeuristicBox
      .setToolTipText("Apply the 'Independent Heuristic'.");
    independentHeuristicBox.setEnabled(true);
    independentHeuristicBox.setSelected(true);

    guardBox.add(optimalButton, constraints);
    guardBox.add(complementHeuristicBox, constraints);
    constraints.gridy++;
    guardBox.add(fromAllowedStatesButton, constraints);
    guardBox.add(independentHeuristicBox, constraints);
    constraints.gridy++;
    guardBox.add(fromForbiddenStatesButton, constraints);
    constraints.gridy++;

    eventList = new JComboBox<String>(events);
    constraints.gridwidth = 2;
    guardBox.add(eventList, constraints);
    constraints.gridwidth = 1;

    // Guard Representation options
    genGuardComputeSupBox = Box.createVerticalBox();

    printGuardBox = new JCheckBox("Print the computed guards");
    printGuardBox.setToolTipText("Compute and print the guards.");
    // printGuardBox.addActionListener(this); // Not needed?

    addGuardsBox = new JCheckBox("Add guards to the model");
    addGuardsBox.setToolTipText("Add computed guards to the model; "
                                + "variables of automata are created and "
                                + "updates are added.");
    addGuardsBox.addActionListener(this); // needed to enable/disable genPLCCodeTUMBox selection

    saveEventGuardInFileBox = new JCheckBox("Save the result in a file");
    saveEventGuardInFileBox
      .setToolTipText("Compute and write the event-guard pairs in a file.");
    // saveEventGuardInFileBox.addActionListener(this); // Not needed?

    saveIDDInFileBox = new JCheckBox("Save the result as an IDD in a file");
    saveIDDInFileBox
      .setToolTipText("For each event an IDD (expression "
                      + "graph) is generated representing the "
                      + "guard. The file is saved in .ps format.");
    // saveIDDInFileBox.addActionListener(this); // Not needed?

    final JPanel representationPanel = new JPanel();
    representationPanel
      .setBorder(BorderFactory.createTitledBorder("Guard representation"));
    representationPanel.setLayout(new GridLayout(4, 1));
    representationPanel.add(printGuardBox);
    representationPanel.add(addGuardsBox);
    representationPanel.add(saveEventGuardInFileBox);
    representationPanel.add(saveIDDInFileBox);

    genGuardComputeSupBox.add(representationPanel);

    // Optimisation options
    layout = new GridBagLayout();
    final JPanel optimizationPanel = new JPanel(layout);
    constraints.gridy = 0;
    optimizationPanel
      .setBorder(BorderFactory.createTitledBorder("Optimization"));

    timeOptBox = new JCheckBox("Compute optimal time");
    timeOptBox.setToolTipText("Compute the supervisor that will restrict "
                              + "the plant to reach a marked state with the "
                              + "minimum time.");
    timeOptBox.addActionListener(this);
    constraints.gridwidth = 2;
    optimizationPanel.add(timeOptBox, constraints);

    constraints.gridwidth = 1;
    constraints.gridy++;
    optimizationPanel.add(new JLabel("Global time domain:"), constraints);
    globalClockDomainField = new JTextField("0");
    globalClockDomainField
      .setToolTipText("Guess a reasonably large amount "
                      + "of time to reach a marked state");
    globalClockDomainField.setColumns(10);
    globalClockDomainField.setEnabled(false);
    optimizationPanel.add(globalClockDomainField, constraints);

    constraints.gridy++;
    final JLabel miniSupLabel = new JLabel("Optimize variable:");
    miniSupLabel.setToolTipText("Compute the supervisor that ensures the "
                                + "variable will have its optimal value "
                                + "among the reachable markes states.");
    optimizationPanel.add(miniSupLabel, constraints);
    variablesList = new JComboBox<String>(variables);
    optimizationPanel.add(variablesList, constraints);

    constraints.gridy++;
    minVarButton = new JRadioButton("Minimize");
    minVarButton.setSelected(true);
    maxVarButton = new JRadioButton("Maximize");
    group = new ButtonGroup();
    group.add(minVarButton);
    group.add(maxVarButton);
    optimizationPanel.add(minVarButton, constraints);
    optimizationPanel.add(maxVarButton, constraints);


    // TUM external toolbox for ST code generation
    if (Config.TUM_EXTERNAL_ON.isTrue()) {
      tumPanel = new JPanel();
      tumPanel.setBorder(BorderFactory.createTitledBorder(
          "PLC code generation"));
      tumPanel.setLayout(new GridLayout(6, 1)); // Change this number according to the number of boxes

      genPLCCodeTUMBox = new JCheckBox("Generate PLC code (external)");
      genPLCCodeTUMBox.setToolTipText(
          "Call the TUM external toolbox to generate PLC code. "
        + "More options in this toolbox. "
        + "NOTA: Guards must be added to the model first!");
      genPLCCodeTUMBox.setSelected(false); // This option is deactivated by default
      genPLCCodeTUMBox.setEnabled(false);
      genPLCCodeTUMBox.addActionListener(this); // needed to check if addGuardsBox is selected

      typePLCCodeTUMBox = Box.createHorizontalBox();
      typePLCCodeTUMBox.add(new JLabel("Output format: "));
      final String[] _typeList = {"standalone", "TwinCAT"};
      typePLCCodeTUMSelector = new JComboBox<String>(_typeList);
      typePLCCodeTUMSelector.setSelectedItem("TwinCAT"); // Default selection
      typePLCCodeTUMSelector.addActionListener(this); // needed to (de)activate the pouDefaultNameBox
      typePLCCodeTUMBox.setEnabled(false);
      typePLCCodeTUMSelector.setEnabled(false);
      typePLCCodeTUMBox.add(typePLCCodeTUMSelector);

      plcCodeTUMefaBox = new JCheckBox("EFA extension (experimental)");
      plcCodeTUMefaBox.setToolTipText(
          "Use the EFA extension. "
        + "NOTA: Experimental");
      plcCodeTUMefaBox.setSelected(false); // This option is deactivated by default
      plcCodeTUMefaBox.setEnabled(false);

      savPLCCodeTUMBox = new JCheckBox("Save in the same folder");
      savPLCCodeTUMBox.setToolTipText(
          "Save the output file in the same folder as the current module. / "
        + "Search for TwinCAT projects in the same folder as the current module.");
      savPLCCodeTUMBox.setSelected(true); // This option is activated by default
      savPLCCodeTUMBox.setEnabled(false);

      pouDefaultNameBox = new JCheckBox("Use default TwinCAT POU's name");
      pouDefaultNameBox.setToolTipText("Use the default TwinCAT POU's name (main_control).");
      pouDefaultNameBox.setSelected(true); // This option is deactivated by default
      pouDefaultNameBox.setEnabled(true);
      pouDefaultNameBox.addActionListener(this); // needed to (de)activate the pouNameField

      pouNameField = new JTextField(defaultTcPOUNameField);
      pouNameField.setToolTipText("Enter the desired POU's name (without extension)");
      pouNameField.setColumns(10);
      pouNameField.setEnabled(false);
      pouNameFieldBox = Box.createHorizontalBox();
      pouNameFieldBox.add(new JLabel("POU's name: "));
      pouNameFieldBox.add(pouNameField);

      // Add tumPanel's elements
      tumPanel.add(genPLCCodeTUMBox);
      tumPanel.add(typePLCCodeTUMBox);
      tumPanel.add(plcCodeTUMefaBox);
      tumPanel.add(savPLCCodeTUMBox);
      tumPanel.add(pouDefaultNameBox);
      tumPanel.add(pouNameFieldBox);
    } else {
      tumPanel = null;
      genPLCCodeTUMBox = null;
      typePLCCodeTUMBox = null;
      typePLCCodeTUMSelector = null; // Later, this could be defined as a specific class.
      plcCodeTUMefaBox = null;
      savPLCCodeTUMBox = null;
      pouDefaultNameBox = null;
      pouNameField = null;
      pouNameFieldBox = null;
    }
    // END TUM external toolbox for ST code generation


    // Create layout!
    layout = new GridBagLayout();
    setLayout(layout);
    constraints.gridy = 0;
    add(mainBox, constraints);
    add(guardBox, constraints);
    constraints.gridy++;
    add(genGuardComputeSupBox, constraints);
    add(optimizationPanel, constraints);
    if (Config.TUM_EXTERNAL_ON.isTrue()) {
      constraints.gridy++;
      add(tumPanel, constraints);
    }
    updatePanel();
  }

  private void updatePanel()
  {
    globalClockDomainField.setEnabled(timeOptBox.isSelected());
    //At present, it is only possible to perform the synthesis via BDDs
    final SynthesisAlgorithm selected = algorithmSelector.getAlgorithm();
    // Clear, then add the ones that are implemented
    algorithmSelector.removeAllItems();
    if (typeSelector.getType() == SynthesisType.CONTROLLABLE) {
      algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
      algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
    } else if (typeSelector.getType() == SynthesisType.NONBLOCKING) {
      algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
      algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
    } else if (typeSelector
      .getType() == SynthesisType.NONBLOCKING_CONTROLLABLE) {
      algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
      algorithmSelector.addItem(SynthesisAlgorithm.PARTITIONBDD);
    } else if (typeSelector.getType() == SynthesisType.UNSAFETY) {
      algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_C);
      algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_M);
      algorithmSelector.addItem(SynthesisAlgorithm.MINIMALITY_P);
    }

    // Default selection
    algorithmSelector.setSelectedIndex(0);
    // Reselect previously selected item if possible
    algorithmSelector.setAlgorithm(selected);

    // TUM external toolbox for ST code generation
    if (Config.TUM_EXTERNAL_ON.isTrue()) {
      if (!addGuardsBox.isSelected()) {
        genPLCCodeTUMBox.setSelected(false);
        genPLCCodeTUMBox.setEnabled(false);
      } else {
        genPLCCodeTUMBox.setEnabled(true);
      }
      if (genPLCCodeTUMBox.isSelected()) {
        typePLCCodeTUMBox.setEnabled(true);
        typePLCCodeTUMSelector.setEnabled(true);
        plcCodeTUMefaBox.setEnabled(true);
        savPLCCodeTUMBox.setEnabled(true);
      } else {
        typePLCCodeTUMBox.setEnabled(false);
        typePLCCodeTUMSelector.setEnabled(false);
        plcCodeTUMefaBox.setEnabled(false);
        savPLCCodeTUMBox.setEnabled(false);
      }
      if (typePLCCodeTUMSelector.isEnabled() &&
        typePLCCodeTUMSelector.getSelectedItem().equals("TwinCAT")) {
        pouDefaultNameBox.setEnabled(true);
      } else {
        pouDefaultNameBox.setEnabled(false);
      }
      if (pouDefaultNameBox.isEnabled() &&
          !pouDefaultNameBox.isSelected()) {
        pouNameFieldBox.setEnabled(true);
        pouNameField.setEnabled(true);
      } else {
        pouNameFieldBox.setEnabled(false);
        pouNameField.setEnabled(false);
        pouNameField.setText(defaultTcPOUNameField);
      }
    }
  }

  //#########################################################################
  //# Overridden methods from EditorSynthesizerPanel
  //TODO: this update function should modify the same parameters as the ones used in regain
  @Override
  public void update(final EditorSynthesizerOptions synthesizerOptions)
  {
    typeSelector.setType(synthesizerOptions.getSynthesisType());
    final SynthesisAlgorithm synthesisAlgo =
      synthesizerOptions.getSynthesisAlgorithm();
    algorithmSelector.setAlgorithm(synthesisAlgo);
    printGuardBox.setSelected(synthesizerOptions.getPrintGuard());
    addGuardsBox.setSelected(synthesizerOptions.getAddGuards());
    saveEventGuardInFileBox.setSelected(synthesizerOptions.getSaveInFile());
    saveIDDInFileBox.setSelected(synthesizerOptions.getSaveIDDInFile());
    reachableBox.setSelected(synthesizerOptions.getReachability());
    peakBDDBox.setSelected(synthesizerOptions.getPeakBDD());
    complementHeuristicBox.setSelected(synthesizerOptions.getCompHeuristic());
    independentHeuristicBox.setSelected(synthesizerOptions.getIndpHeuristic());
    globalClockDomainField.setEnabled(timeOptBox.isSelected());

    if (Config.TUM_EXTERNAL_ON.isTrue()) {
      genPLCCodeTUMBox.setEnabled(synthesizerOptions.getAddGuards());
//      typePLCCodeTUMSelector.setSelectedItem(synthesizerOptions.getTypePLCCodeTUM());
//      plcCodeTUMefaBox.setSelected(synthesizerOptions.getPLCCodeTUMefaBox());
//      savPLCCodeTUMBox.setSelected(synthesizerOptions.getSavPLCCodeTUMBox());
//      pouNameField.setText(synthesizerOptions.getPouNameField());
    }
  }

  @Override
  public void regain(final EditorSynthesizerOptions synthesizerOptions)
  {
    synthesizerOptions.setSynthesisType(typeSelector.getType());
    final SynthesisAlgorithm synthesisAlgo = algorithmSelector.getAlgorithm();
    synthesizerOptions.setSynthesisAlgorithm(synthesisAlgo);
    synthesizerOptions.setPrintGuard(printGuardBox.isSelected());
    synthesizerOptions.setAddGuards(addGuardsBox.isSelected());
    synthesizerOptions.setSaveInFile(saveEventGuardInFileBox.isSelected());
    synthesizerOptions.setSaveIDDInFile(saveIDDInFileBox.isSelected());
    synthesizerOptions.setReachability(reachableBox.isSelected());
    synthesizerOptions.setPeakBDD(peakBDDBox.isSelected());
    synthesizerOptions.setCompHeuristic(complementHeuristicBox.isSelected());
    synthesizerOptions.setIndpHeuristic(independentHeuristicBox.isSelected());
    synthesizerOptions.setOptimization(timeOptBox.isSelected());
    synthesizerOptions
      .setGlobalClockDomain(Long.parseLong(globalClockDomainField.getText()));
    if (Config.TUM_EXTERNAL_ON.isTrue()) {
      synthesizerOptions.setGenPLCCodeTUMBox(genPLCCodeTUMBox.isSelected());
      synthesizerOptions.setTypePLCCodeTUM((String) typePLCCodeTUMSelector.getSelectedItem());
      synthesizerOptions.setPLCCodeTUMefaBox(plcCodeTUMefaBox.isSelected());
      synthesizerOptions.setSavPLCCodeTUMBox(savPLCCodeTUMBox.isSelected());
      synthesizerOptions.setPouDefaultNameBox(pouDefaultNameBox.isSelected());
      synthesizerOptions.setPouNameField(pouNameField.getText());
    }

    if (fromForbiddenStatesButton.isSelected()) {
      synthesizerOptions.setExpressionType(ExpressionType.FORBIDDEN);
    }
    if (fromAllowedStatesButton.isSelected()) {
      synthesizerOptions.setExpressionType(ExpressionType.ALLOWED);
    }
    if (optimalButton.isSelected()) {
      synthesizerOptions.setExpressionType(ExpressionType.ADAPTIVE);
    }

    if (eventList.getSelectedIndex() == 0)
      synthesizerOptions.setEvent("");
    else
      synthesizerOptions.setEvent((String) eventList.getSelectedItem());

    if (variablesList.getSelectedIndex() == 0)
      synthesizerOptions.setOptVariable("");
    else
      synthesizerOptions
        .setOptVariable((String) variablesList.getSelectedItem());

    if (minVarButton.isSelected())
      synthesizerOptions.setTypeOfVarOpt(true);

    if (maxVarButton.isSelected())
      synthesizerOptions.setTypeOfVarOpt(false);
  }

  //#########################################################################
  //# Overridden methods from ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    // Default

    if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MONOLITHIC) {
      ;// Will be completed later
    } else if (algorithmSelector
      .getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL) {
      ;// Will be completed later
    } else if (algorithmSelector
      .getAlgorithm() == SynthesisAlgorithm.MODULAR) {
      ;// Will be completed later
    }
    updatePanel();
  }
}
