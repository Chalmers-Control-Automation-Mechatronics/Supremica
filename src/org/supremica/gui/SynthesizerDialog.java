//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.algorithms.minimization.MinimizationPreselectingHeuristic;
import org.supremica.automata.algorithms.minimization.MinimizationSelectingHeuristic;


abstract class SynthesizerPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  public abstract void update(SynthesizerOptions s);

  public abstract void regain(SynthesizerOptions s);
}


public class SynthesizerDialog
  extends JDialog
  implements ActionListener
{

  /**
   * Creates modal dialog box for input of synthesis options.
   */
  public SynthesizerDialog(final Frame parentFrame,
                           final int numSelected,
                           final SynthesizerOptions synthesizerOptions)
  {
    super(parentFrame, true); // modal
    this.synthesizerOptions = synthesizerOptions;

    setTitle("Synthesizer options");

    final Container contentPane = getContentPane();

    standardPanel = new SynthesizerDialogStandardPanel(numSelected);
    advancedPanel = new SynthesizerDialogAdvancedPanel();
    mHeuristicPanel = new SynthesizerDialogHeuristicPanel();

    mTabbedPane = new JTabbedPane();
    mMinimizationOptions = new MinimizationOptions();

    mTabbedPane.addTab("Standard options", null, standardPanel,
                       "Standard options");
    mTabbedPane.addTab("Advanced options", null, advancedPanel,
                       "Advanced options");
    mTabbedPane.addTab("Heuristic options", null, mHeuristicPanel,
                       "Heuristic options");

    // buttonPanel
    final JPanel buttonPanel = new JPanel();

    okButton = addButton(buttonPanel, "OK");
    cancelButton = addButton(buttonPanel, "Cancel");

    contentPane.add("Center", mTabbedPane);
    contentPane.add("South", buttonPanel);
    Utility.setDefaultButton(this, okButton);

    setResizable(true);
    pack();
    setLocationRelativeTo(parentFrame);
    update();
  }

  /**
   * Updates the information in the dialog from what is recorded in
   * SynthesizerOptions.
   *
   * @see SynthesizerOptions
   */
  public void update()
  {
    standardPanel.update(synthesizerOptions);
    advancedPanel.update(synthesizerOptions);
    mHeuristicPanel.update(mMinimizationOptions);
  }

  private JButton addButton(final Container container, final String name)
  {
    final JButton button = new JButton(name);

    button.addActionListener(this);
    container.add(button);

    return button;
  }

  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final Object source = event.getSource();

    if (source == okButton) {
      standardPanel.regain(synthesizerOptions);
      advancedPanel.regain(synthesizerOptions);
      mHeuristicPanel.regain(mMinimizationOptions);

      if (synthesizerOptions.isValid()) {
        synthesizerOptions.saveOptions();
        mMinimizationOptions.saveOptions();
        synthesizerOptions.setDialogOK(true);

        setVisible(false);
        dispose();
      } else {
        JOptionPane
          .showMessageDialog(getOwner(),
                             "Invalid combination of type and algorithm",
                             "Alert", JOptionPane.ERROR_MESSAGE);
      }
    } else if (source == cancelButton) {
      synthesizerOptions.setDialogOK(false); // Already done...
      setVisible(false);
      dispose();
    }
  }


  //# Inner class SynthesizerDialogStandardPanel
  class SynthesizerDialogStandardPanel extends SynthesizerPanel implements
    ActionListener
  {
    private static final long serialVersionUID = 1L;
    private final SynthesisSelector typeSelector;
    private final AlgorithmSelector algorithmSelector;
    private final JCheckBox purgeBox;
	private final JCheckBox renameBox;
    private final JCheckBox removeUnecessarySupBox;
    private final NonblockNote nbNote;

    public AlgorithmSelector getAlgorithmSelector()
    {
      return algorithmSelector;
    }


    class NonblockNote extends JTextArea
    {
      private static final long serialVersionUID = 1L;
      private static final int transparent = 0;

      public NonblockNote()
      {
        super("Note:\n" + "Modular nonblocking synthesis results in a\n"
              + "compact representation of the monolithic\n"
              + "supervisor that Supremica can not currently\n"
              + "make use of.");

        super.setBackground(new Color(0, 0, 0, transparent));
      }
    }

    public SynthesizerDialogStandardPanel(final int num)
    {
      algorithmSelector = AlgorithmSelector.create(num);
      algorithmSelector.addActionListener(this);

      typeSelector = SynthesisSelector.create();
      typeSelector.addActionListener(this);

      purgeBox = new JCheckBox("Purge result");
      purgeBox.setToolTipText("Remove all forbidden states");

	  renameBox = new JCheckBox("Rename states");
	  renameBox.setToolTipText("Give states generic names (q0, q1, q2,...)");

      removeUnecessarySupBox =
        new JCheckBox("Remove unnecessary supervisors");
      removeUnecessarySupBox
        .setToolTipText("Remove supervisors that don't affect the controllability");

      nbNote = new NonblockNote();

      if (num == 1) {
        removeUnecessarySupBox.setEnabled(false);
        nbNote.setVisible(false);
      }

      // Create layout!
      setLayout(new GridBagLayout());
      final GridBagConstraints constraints = new GridBagConstraints();
      constraints.insets.top = 4;
      constraints.insets.bottom = 4;
      constraints.gridy = 0;
      constraints.weightx = 1.0;
      constraints.anchor = GridBagConstraints.EAST;
      add(new JLabel("Property: "), constraints);
      constraints.anchor = GridBagConstraints.WEST;
      add(typeSelector, constraints);
      constraints.gridy++;
      constraints.anchor = GridBagConstraints.EAST;
      add(new JLabel("Algorithm: "), constraints);
      constraints.anchor = GridBagConstraints.WEST;
      add(algorithmSelector, constraints);
      constraints.gridy++;
      constraints.gridwidth = 2;
      constraints.anchor = GridBagConstraints.CENTER;
      add(purgeBox, constraints);
      constraints.insets.top = 0;
      constraints.gridy++;
      add(removeUnecessarySupBox, constraints);
      constraints.gridy++;
      add(renameBox, constraints);
    }

    @Override
    public void update(final SynthesizerOptions synthesizerOptions)
    {
      typeSelector.setType(synthesizerOptions.getSynthesisType());
      algorithmSelector.setAlgorithm(synthesizerOptions
        .getSynthesisAlgorithm());
      purgeBox.setSelected(synthesizerOptions.doPurge());
	  renameBox.setSelected(synthesizerOptions.doRename());
      removeUnecessarySupBox.setSelected(synthesizerOptions
        .getRemoveUnecessarySupervisors());


      updatePanel();
    }

    public void updatePanel()
    {

      final int heuristicTabIndex = 2;
      mTabbedPane .setComponentAt(heuristicTabIndex, mHeuristicPanel);
      if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL_WATERS)
      {
        mTabbedPane.setEnabledAt(heuristicTabIndex,true);
      } else {
        mTabbedPane.setEnabledAt(heuristicTabIndex,false);
      }
      // Which algorithms should be enabled?
      // Remember current selection
      final SynthesisAlgorithm selected = algorithmSelector.getAlgorithm();
      // Clear, then add the ones that are implemented
      algorithmSelector.removeAllItems();
      // Which type of verification?
      if (typeSelector.getType() == SynthesisType.CONTROLLABLE) {
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC);
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC_WATERS);
        algorithmSelector.addSelectable(SynthesisAlgorithm.MODULAR);
        algorithmSelector.addSelectable(SynthesisAlgorithm.COMPOSITIONAL);
        algorithmSelector.addSelectable(SynthesisAlgorithm.COMPOSITIONAL_WATERS);
      } else if (typeSelector.getType() == SynthesisType.NONBLOCKING) {
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC);
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC_WATERS);
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHICBDD);
        algorithmSelector.addSelectable(SynthesisAlgorithm.COMPOSITIONAL);
        algorithmSelector.addSelectable(SynthesisAlgorithm.COMPOSITIONAL_WATERS);
      } else if (typeSelector.getType() == SynthesisType.NONBLOCKING_CONTROLLABLE) {
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC);
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC_WATERS);
        algorithmSelector.addSelectable(SynthesisAlgorithm.COMPOSITIONAL);
        algorithmSelector.addSelectable(SynthesisAlgorithm.COMPOSITIONAL_WATERS);
      } else if (typeSelector.getType() == SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL) {
        algorithmSelector.addSelectable(SynthesisAlgorithm.MONOLITHIC_WATERS);
      }
      if (algorithmSelector.getItemCount() > 0) {
        // Default selection
        algorithmSelector.setSelectedIndex(0);
        // Reselect previously selected item if possible
        algorithmSelector.setAlgorithm(selected);
      }
      if (advancedPanel != null) {
        if (selected == SynthesisAlgorithm.MONOLITHIC_WATERS) {
          advancedPanel.setLocalizeBoxEnabled(true);
        } else {
          advancedPanel.setLocalizeBoxEnabled(false);
        }
        final boolean watersSelected =
          algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MONOLITHIC_WATERS;
        final boolean reduceSelected = advancedPanel.isReduceBoxSelected();
        advancedPanel.setNoteVisible(!watersSelected && reduceSelected);
      }
    }

    @Override
    public void regain(final SynthesizerOptions synthesizerOptions)
    {
      synthesizerOptions.setSynthesisType(typeSelector.getType());
      synthesizerOptions.setSynthesisAlgorithm(algorithmSelector
        .getAlgorithm());
      synthesizerOptions.setPurge(purgeBox.isSelected());
	  synthesizerOptions.setRename(renameBox.isSelected());
      synthesizerOptions
        .setRemoveUnecessarySupervisors(removeUnecessarySupBox.isSelected());
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
      //X stands for "Should be setEnabled but setEnabled does not work as it should(?)."

      // Default
      purgeBox.setVisible(true); //X
      removeUnecessarySupBox.setVisible(true); //X
	  renameBox.setVisible(true);
      nbNote.setVisible(false);

      if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MONOLITHIC)
	  {
        removeUnecessarySupBox.setVisible(false); //X
      }
	  else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL)
	  {
        removeUnecessarySupBox.setVisible(false); //X
        purgeBox.setVisible(true); //X
		renameBox.setVisible(false);
      }
	  else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MODULAR)
	  {
 		renameBox.setVisible(false);

		if ((typeSelector.getType() == SynthesisType.NONBLOCKING)
            || (typeSelector.getType() == SynthesisType.NONBLOCKING_CONTROLLABLE))
		{
          purgeBox.setVisible(false); //X

          removeUnecessarySupBox.setVisible(false); //X
          nbNote.setVisible(true);
        }
      }
	  else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL_WATERS)
	  {
        removeUnecessarySupBox.setVisible(false); //X
        purgeBox.setVisible(false); //X
		renameBox.setVisible(false);
      }
      updatePanel();
    }
  }


  //# Inner class SynthesizerDialogAdvandedPanel
  class SynthesizerDialogAdvancedPanel extends SynthesizerPanel implements
    ActionListener
  {
    private static final long serialVersionUID = 1L;
    private final JCheckBox reduceSupervisorsBox;
    private final JCheckBox localizeSupervisorsBox;
    private final JCheckBox oneEventAtATimeBox;
    private final JCheckBox maximallyPermissiveBox;
    private final JCheckBox maximallyPermissiveIncrementalBox;
    private final JCheckBox maximallyPermissiveOnePlantAtATimeBox;
    private final JTextArea note;

    public void setNoteVisible(final boolean visible)
    {
      note.setVisible(visible);
    }

    public boolean isReduceBoxSelected()
    {
      return reduceSupervisorsBox.isSelected();
    }

    public void setLocalizeBoxEnabled(final boolean enable)
    {
      this.localizeSupervisorsBox.setEnabled(enable);
    }

    public SynthesizerDialogAdvancedPanel()
    {
      final Box advancedBox = Box.createVerticalBox();

      oneEventAtATimeBox =
        new JCheckBox("One event at a time (experimental)");
      oneEventAtATimeBox
        .setToolTipText("Synthesize with respect to one event at a time");

      maximallyPermissiveBox = new JCheckBox("Maximally permissive result");
      maximallyPermissiveBox
        .setToolTipText("Guarantee maximally permissive result");
      maximallyPermissiveBox.addActionListener(this);

      maximallyPermissiveIncrementalBox =
        new JCheckBox("Incremental algorithm");
      maximallyPermissiveIncrementalBox
        .setToolTipText("Use incremental algorithm for maximally permissive synthesis");
      maximallyPermissiveIncrementalBox.addActionListener(this);

      maximallyPermissiveOnePlantAtATimeBox =
        new JCheckBox("One plant at a time (experimental)");
      maximallyPermissiveOnePlantAtATimeBox
        .setToolTipText("Increment by one plant at a time");

      reduceSupervisorsBox =
        new JCheckBox("Supervisor Reduction");
      reduceSupervisorsBox
        .setToolTipText("Remove redundant states and events from "
                        + "synthesized supervisors");
      reduceSupervisorsBox.addActionListener(this);

      localizeSupervisorsBox =
        new JCheckBox("Supervisor Localization");
      localizeSupervisorsBox
        .setToolTipText("Apply localization to synthesized supervisors");
      localizeSupervisorsBox.addActionListener(this);

      advancedBox.add(oneEventAtATimeBox);
      advancedBox.add(maximallyPermissiveBox);
      advancedBox.add(maximallyPermissiveIncrementalBox);
      advancedBox.add(maximallyPermissiveOnePlantAtATimeBox);
      advancedBox.add(reduceSupervisorsBox);
      advancedBox.add(localizeSupervisorsBox);

      note =
        new JTextArea("Note:\n"
                      + "'Purge result' must be selected for supervisor\n"
                      + "reduction to work.\n");

      note.setBackground(new Color(0, 0, 0, 0));
      note.setVisible(false);
      this.add(advancedBox, BorderLayout.CENTER);
      this.add(note, BorderLayout.SOUTH);
    }

    @Override
    public void update(final SynthesizerOptions synthesizerOptions)
    {
      reduceSupervisorsBox.setSelected(synthesizerOptions
        .getReduceSupervisors());
      localizeSupervisorsBox.setSelected(synthesizerOptions
        .getLocalizeSupervisors());
      maximallyPermissiveBox.setSelected(synthesizerOptions
        .getMaximallyPermissive());
      maximallyPermissiveIncrementalBox.setSelected(synthesizerOptions
        .getMaximallyPermissiveIncremental());
      updatePanel();
    }

    private void updatePanel()
    {
      if (!maximallyPermissiveBox.isSelected())
        maximallyPermissiveIncrementalBox.setSelected(false);
      if (!maximallyPermissiveIncrementalBox.isSelected())
        maximallyPermissiveOnePlantAtATimeBox.setSelected(false);

      maximallyPermissiveIncrementalBox.setEnabled(maximallyPermissiveBox
        .isSelected());
      maximallyPermissiveOnePlantAtATimeBox
        .setEnabled(maximallyPermissiveIncrementalBox.isSelected()
                    && maximallyPermissiveIncrementalBox.isEnabled());

      final boolean watersSelected =
        standardPanel.getAlgorithmSelector().getAlgorithm() == SynthesisAlgorithm.MONOLITHIC_WATERS;
      final boolean reduceSelected = reduceSupervisorsBox.isSelected();
      note.setVisible(!watersSelected && reduceSelected);
    }

    @Override
    public void regain(final SynthesizerOptions options)
    {
      options.setReduceSupervisors(reduceSupervisorsBox.isSelected());
      options.setLocalizeSupervisors(localizeSupervisorsBox.isSelected());
      options.setMaximallyPermissive(maximallyPermissiveBox.isSelected());
      options
        .setMaximallyPermissiveIncremental(maximallyPermissiveIncrementalBox
          .isSelected());
      options.addOnePlantAtATime =
        maximallyPermissiveOnePlantAtATimeBox.isSelected();
      options.oneEventAtATime = oneEventAtATimeBox.isSelected();
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
      // Incremental box enabled?
      maximallyPermissiveIncrementalBox.setEnabled(maximallyPermissiveBox
        .isSelected());

      // Display note?

      updatePanel();
    }
  }







  //# Inner class SynthesizerDialogHeuristicPanel
  class SynthesizerDialogHeuristicPanel extends JPanel
  {
    private static final long serialVersionUID = 1L;
    JComboBox<Object> minimizationPreselectingHeuristic;
    JComboBox<Object> minimizationSelectingHeuristic;

    public SynthesizerDialogHeuristicPanel()
    {
        minimizationPreselectingHeuristic = new JComboBox<Object>(MinimizationPreselectingHeuristic.values());
        minimizationSelectingHeuristic = new JComboBox<Object>(MinimizationSelectingHeuristic.values());

        // Create layout!
        final Box mainBox = Box.createVerticalBox();

        JPanel panel = new JPanel();
        final Box strategyBox = Box.createHorizontalBox();
        strategyBox.add(new JLabel("Minimization strategy: "));
        strategyBox.add(minimizationPreselectingHeuristic);
        panel.add(strategyBox);
        mainBox.add(panel);

        panel = new JPanel();
        final Box heuristicBox = Box.createHorizontalBox();
        heuristicBox.add(new JLabel("Minimization heuristic: "));
        heuristicBox.add(minimizationSelectingHeuristic);
        panel.add(heuristicBox);
        mainBox.add(panel);

        // Add components
        this.add(mainBox);
    }

    public void update(final MinimizationOptions options)
    {
        minimizationPreselectingHeuristic.setSelectedItem(options.getMinimizationPreselctingHeuristic());
        minimizationSelectingHeuristic.setSelectedItem(options.getMinimizationSelctingHeuristic());
    }

    public void regain(final MinimizationOptions options)
    {
       options.setMinimizationPreselctingHeuristic((MinimizationPreselectingHeuristic) minimizationPreselectingHeuristic.getSelectedItem());
       options.setMinimizationSelctingHeuristic((MinimizationSelectingHeuristic) minimizationSelectingHeuristic.getSelectedItem());
    }
  }








  //# Inner class AlgorithmSelector
  static class AlgorithmSelector extends JComboBox<SynthesisAlgorithm>
  {
    private static final long serialVersionUID = 1L;

    private AlgorithmSelector()
    {
      super();
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

    private void addSelectable(final SynthesisAlgorithm algo)
    {
      if (algo.isLoadable()) {
        addItem(algo);
      }
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


  //# Inner class SynthesisSelector
  static class SynthesisSelector extends JComboBox<SynthesisType>
  {
    private static final long serialVersionUID = 1L;

    private SynthesisSelector()
    {
      super(SynthesisType.analyzerValues());
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
  //# Data Members
  private final JButton okButton;
  private final JButton cancelButton;
  private final SynthesizerOptions synthesizerOptions;
  private final SynthesizerDialogStandardPanel standardPanel;
  private final SynthesizerDialogAdvancedPanel advancedPanel;

  private final MinimizationOptions mMinimizationOptions;
  private final SynthesizerDialogHeuristicPanel mHeuristicPanel;
  private final JTabbedPane mTabbedPane;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 2867105691568327887L;

}
