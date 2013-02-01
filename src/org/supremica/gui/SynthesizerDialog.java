/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

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


abstract class SynthesizerPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  public abstract void update(SynthesizerOptions s);

  public abstract void regain(SynthesizerOptions s);
}


public class SynthesizerDialog implements ActionListener
{
  private final JButton okButton;
  private final JButton cancelButton;
  private final SynthesizerOptions synthesizerOptions;
  SynthesizerDialogStandardPanel standardPanel;
  SynthesizerDialogAdvancedPanel advancedPanel;

  private final JDialog dialog;
  private final Frame parentFrame;

  /**
   * Creates modal dialog box for input of synthesizer options.
   */
  public SynthesizerDialog(final Frame parentFrame, final int numSelected,
                           final SynthesizerOptions synthesizerOptions)
  {
    dialog = new JDialog(parentFrame, true); // modal
    this.parentFrame = parentFrame;
    this.synthesizerOptions = synthesizerOptions;

    dialog.setTitle("Synthesizer options");
    dialog.setSize(new Dimension(400, 300));

    final Container contentPane = dialog.getContentPane();

    standardPanel = new SynthesizerDialogStandardPanel(numSelected);
    advancedPanel = new SynthesizerDialogAdvancedPanel();

    final JTabbedPane tabbedPane = new JTabbedPane();

    tabbedPane.addTab("Standard options", null, standardPanel,
                      "Standard options");
    tabbedPane.addTab("Advanced options", null, advancedPanel,
                      "Advanced options");
    //        tabbedPane.addTab("Guard options", null, guardPanel, "Guard options");

    // buttonPanel
    final JPanel buttonPanel = new JPanel();

    okButton = addButton(buttonPanel, "OK");
    cancelButton = addButton(buttonPanel, "Cancel");

    contentPane.add("Center", tabbedPane);
    contentPane.add("South", buttonPanel);
    Utility.setDefaultButton(dialog, okButton);

    // ** MF ** Fix to get the frigging thing centered
    final Dimension dim = dialog.getMinimumSize();

    dialog.setLocation(Utility.getPosForCenter(dim));
    dialog.setResizable(false);
    update();
  }

  public SynthesizerDialog(final Frame parentFrame, final int numSelected,
                           final SynthesizerOptions synthesizerOptions,
                           final Vector<?> controllableEvents)
  {
    this(parentFrame, numSelected, synthesizerOptions);
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
  }

  private JButton addButton(final Container container, final String name)
  {
    final JButton button = new JButton(name);

    button.addActionListener(this);
    container.add(button);

    return button;
  }

  public void show()
  {
    dialog.setVisible(true);
  }

  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final Object source = event.getSource();

    if (source == okButton) {
      standardPanel.regain(synthesizerOptions);
      advancedPanel.regain(synthesizerOptions);

      if (synthesizerOptions.isValid()) {
        synthesizerOptions.saveOptions();
        synthesizerOptions.setDialogOK(true);

        dialog.setVisible(false);
        dialog.dispose();
      } else {
        JOptionPane
          .showMessageDialog(parentFrame,
                             "Invalid combination of type and algorithm",
                             "Alert", JOptionPane.ERROR_MESSAGE);
      }
      //////////////

      //////////////
    } else if (source == cancelButton) {
      synthesizerOptions.setDialogOK(false); // Already done...
      dialog.setVisible(false);
      dialog.dispose();
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
      final Box mainBox = Box.createVerticalBox();

      JPanel panel = new JPanel();
      Box box = Box.createHorizontalBox();
      box.add(new JLabel("Property:"));
      box.add(typeSelector);
      panel.add(box);
      mainBox.add(panel);

      panel = new JPanel();
      box = Box.createHorizontalBox();
      box.add(new JLabel("Algorithm: "));
      box.add(algorithmSelector);
      panel.add(box);
      mainBox.add(panel);

      panel = new JPanel();
      box = Box.createHorizontalBox();
      box.add(purgeBox);
      box.add(removeUnecessarySupBox);
      panel.add(box);
      mainBox.add(panel);

      panel = new JPanel();
      panel.add(nbNote);
      mainBox.add(panel);

      // Add components
      this.add(mainBox);

      updatePanel();
    }

    @Override
    public void update(final SynthesizerOptions synthesizerOptions)
    {
      typeSelector.setType(synthesizerOptions.getSynthesisType());
      algorithmSelector.setAlgorithm(synthesizerOptions
        .getSynthesisAlgorithm());
      purgeBox.setSelected(synthesizerOptions.doPurge());
      removeUnecessarySupBox.setSelected(synthesizerOptions
        .getRemoveUnecessarySupervisors());
    }

    public void updatePanel()
    {
      // Which algorithms should be enabled?
      // Remember current selection
      final SynthesisAlgorithm selected = algorithmSelector.getAlgorithm();
      // Clear, then add the ones that are implemented
      algorithmSelector.removeAllItems();
      // Which type of verification?
      if (typeSelector.getType() == SynthesisType.CONTROLLABLE) {
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC_WATERS);
        algorithmSelector.addItem(SynthesisAlgorithm.MODULAR);
        algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
        algorithmSelector.addItem(SynthesisAlgorithm.BDD);
        algorithmSelector.addItem(SynthesisAlgorithm.SYNTHESISA);
        algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL_WATERS);
      } else if (typeSelector.getType() == SynthesisType.NONBLOCKING) {
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC_WATERS);
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHICBDD);
        algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
        algorithmSelector.addItem(SynthesisAlgorithm.BDD);
        algorithmSelector.addItem(SynthesisAlgorithm.SYNTHESISA);
        algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL_WATERS);
      } else if (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLE) {
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC_WATERS);
        algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL);
        algorithmSelector.addItem(SynthesisAlgorithm.BDD);
        algorithmSelector.addItem(SynthesisAlgorithm.SYNTHESISA);
        algorithmSelector.addItem(SynthesisAlgorithm.COMPOSITIONAL_WATERS);
      } else if (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLEOBSERVABLE) {
        algorithmSelector.addItem(SynthesisAlgorithm.MONOLITHIC);
      }
      // Default selection
      algorithmSelector.setSelectedIndex(0);
      // Reselect previously selected item if possible
      algorithmSelector.setAlgorithm(selected);
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
      nbNote.setVisible(false);

      if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MONOLITHIC) {
        removeUnecessarySupBox.setVisible(false); //X
      } else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL
                 || algorithmSelector.getAlgorithm() == SynthesisAlgorithm.SYNTHESISA) {
        removeUnecessarySupBox.setVisible(false); //X
        purgeBox.setVisible(true); //X
      } else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.MODULAR) {
        if ((typeSelector.getType() == SynthesisType.NONBLOCKING)
            || (typeSelector.getType() == SynthesisType.NONBLOCKINGCONTROLLABLE)) {
          purgeBox.setVisible(false); //X
          removeUnecessarySupBox.setVisible(false); //X
          nbNote.setVisible(true);
        }
      } else if (algorithmSelector.getAlgorithm() == SynthesisAlgorithm.COMPOSITIONAL_WATERS) {
        removeUnecessarySupBox.setVisible(false); //X
        purgeBox.setVisible(false); //X
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
        new JCheckBox("Reduce supervisors (experimental)");
      reduceSupervisorsBox
        .setToolTipText("Remove redundant states and events from "
                        + "synthesized supervisors");
      reduceSupervisorsBox.addActionListener(this);

      localizeSupervisorsBox =
        new JCheckBox("localize supervisors (experimental)");
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
      if (reduceSupervisorsBox.isSelected()
          && standardPanel.getAlgorithmSelector().getAlgorithm() == SynthesisAlgorithm.MONOLITHIC_WATERS) {
        localizeSupervisorsBox.setEnabled(true);
      } else {
        localizeSupervisorsBox.setEnabled(false);
      }

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
}
