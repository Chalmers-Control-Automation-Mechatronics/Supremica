//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.automata.algorithms.minimization.MinimizationHeuristic;
import org.supremica.automata.algorithms.minimization.BisimulationEquivalenceMinimizer;
import org.supremica.automata.algorithms.EquivalenceRelation;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.*;
import org.supremica.log.*;
import java.util.Iterator;
public class MinimizationDialog
    implements ActionListener
{
    private static Logger logger = LoggerFactory.createLogger(MinimizationDialog.class);

    private final JButton okButton;
    private final JButton cancelButton;
    private final MinimizationOptions options;
    private final MinimizationDialogStandardPanel standardPanel;
    private final MinimizationDialogAdvancedPanel advancedPanel;
    private final JDialog dialog;

    /**
     * Creates modal dialog box for input of verification options.
     */
    public MinimizationDialog(final Frame parentFrame, final MinimizationOptions options, final Automata automata)
    {
        dialog = new JDialog(parentFrame, true);    // modal
        this.options = options;

        dialog.setTitle("Minimization options");
        dialog.setSize(new Dimension(400, 300));

        // dialog.setResizable(false);
        final Container contentPane = dialog.getContentPane();

        standardPanel = new MinimizationDialogStandardPanel();
        advancedPanel = new MinimizationDialogAdvancedPanel(automata);

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");
        if (automata.size() > 1)
        {
            tabbedPane.addTab("Advanced options", null, advancedPanel, "Advanced options");
            //tabbedPane.setSelectedComponent(advancedPanel);
        }

        // buttonPanel;
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

    /**
     * Updates the information in the dialog from what is recorded in MinimizationOptions.
     * @see MinimizationOptions
     */
    public void update()
    {
        advancedPanel.update(options);
        standardPanel.update(options);
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

    public void actionPerformed(final ActionEvent event)
    {
        final Object source = event.getSource();

        if (source == okButton)
        {
            // Remember the selections
            standardPanel.regain(options);
            advancedPanel.regain(options);
            options.saveOptions();
            options.setDialogOK(true);
            dialog.setVisible(false);
            dialog.dispose();
        }
        else if (source == cancelButton)
        {
            // Cancel
            options.setDialogOK(false);    // Already done...
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    abstract class MinimizationPanel
        extends JPanel
    {
        private static final long serialVersionUID = 1L;

        public abstract void update(MinimizationOptions options);

        public abstract void regain(MinimizationOptions options);
    }

    class MinimizationDialogStandardPanel
        extends MinimizationPanel
        implements ActionListener
    {
        private static final long serialVersionUID = 1L;

        private final JComboBox<EquivalenceRelation> minimizationTypeBox;
        private final JCheckBox alsoTransitions;
        private final JCheckBox keepOriginal;
        private final JCheckBox ignoreMarking;

        private final JTextArea note;

        public MinimizationDialogStandardPanel()
        {
            minimizationTypeBox =
              new JComboBox<EquivalenceRelation>(EquivalenceRelation.enabledValues());
            minimizationTypeBox.addActionListener(this);

            // Disable bisimulation equivalence if library is missing!
            if (!BisimulationEquivalenceMinimizer.libraryLoaded())
            {
                logger.warn("LibraryBisimulationEquivalence not found (not in path? 32/64-bit problem?), using Java implementation.");
                minimizationTypeBox.removeItem(EquivalenceRelation.BISIMULATIONEQUIVALENCE);
            }

            alsoTransitions = new JCheckBox("Also minimize number of transitions");
            alsoTransitions.setToolTipText("Make sure that the number of the transitions is the minimal number (with respect to observation equivalence))");
            keepOriginal = new JCheckBox("Keep original");
            keepOriginal.setToolTipText("If unchecked, the selected automata are thrown away after completed execution");
            ignoreMarking = new JCheckBox("Ignore marking of states");
            ignoreMarking.setToolTipText("If checked, the marking is ignored, i.e. a marked state is not considered to be different from a nonmarked state with respect to equivalence");

            note = new JTextArea("Note:\n" + "I have nothing to say.");
            note.setBackground(this.getBackground());

            final Box standardBox = Box.createHorizontalBox();
            standardBox.add(new JLabel("         ")); // Ugly fix to get stuff centered
            standardBox.add(minimizationTypeBox);
            standardBox.add(new JLabel("         ")); // Ugly fix to get stuff centered
            final Box anotherBox = Box.createVerticalBox();
            anotherBox.add(alsoTransitions);
            anotherBox.add(keepOriginal);
            anotherBox.add(ignoreMarking);

            // NEW TRY
            this.setLayout(new GridLayout(2, 1));

            final JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new FlowLayout());
            choicePanel.add(standardBox);
            choicePanel.add(anotherBox);
            this.add(choicePanel);

            final JPanel notePanel = new JPanel();
            notePanel.setLayout(new FlowLayout());
            notePanel.add(note);
            note.setVisible(false);
            this.add(notePanel);
        }

        public void update(final MinimizationOptions options)
        {
            minimizationTypeBox.setSelectedItem(options.getMinimizationType());
            alsoTransitions.setSelected(options.getAlsoTransitions());
            keepOriginal.setSelected(options.getKeepOriginal());
            ignoreMarking.setSelected(options.getIgnoreMarking());
        }

        public void regain(final MinimizationOptions options)
        {
            options.setMinimizationType((EquivalenceRelation) minimizationTypeBox.getSelectedItem());
            options.setAlsoTransitions(alsoTransitions.isSelected());
            options.setKeepOriginal(keepOriginal.isSelected());
            options.setIgnoreMarking(ignoreMarking.isSelected());
        }

        public void actionPerformed(final ActionEvent e)
        {
          alsoTransitions.setEnabled(true);
          ignoreMarking.setEnabled(true);
          final EquivalenceRelation rel =
            (EquivalenceRelation) minimizationTypeBox.getSelectedItem();
          switch (rel) {
          case LANGUAGEEQUIVALENCE:
            alsoTransitions.setSelected(true);
            alsoTransitions.setEnabled(false);
            note.setText("Note:\n" +
                         "Returns a deterministic automaton representing the same\n" +
                         "language using a minimal number of states and transitions.\n" +
                         "  If the automaton is nondeterministic, it is first made\n" +
                         "deterministic.");
            note.setVisible(true);
            break;
          case OP:
            alsoTransitions.setSelected(true);
            alsoTransitions.setEnabled(false);
            ignoreMarking.setSelected(false);
            ignoreMarking.setEnabled(false);
            note.setText("Note:\n" +
                         "Returns a deterministic automaton that is\n" +
                         "conflict equivalent to the input, using the\n" +
                         "OP-Search algorithm by P. Pena et.al. (2010).");
            note.setVisible(true);
            break;
          case CONFLICTEQUIVALENCE:
            ignoreMarking.setSelected(false);
            ignoreMarking.setEnabled(false);
            note.setText("Note:\n" +
                         "This minimization algorithm is experimental! The\n" +
                         "result may not be minimal but should at least be\n" +
                         "conflict equivalent to the input.");
            note.setVisible(true);
            break;
          case SUPERVISIONEQUIVALENCE:
            note.setText("Note:\n" +
                         "This minimization algorithm is experimental! The\n" +
                         "result may not be minimal but should at least be\n" +
                         "supervision equivalent to the input.");
            note.setVisible(true);
          default:
            note.setVisible(false);
          }
        }

    }


    class MinimizationDialogAdvancedPanel
        extends MinimizationPanel
        implements ActionListener
    {
        private static final long serialVersionUID = 1L;

        private final JCheckBox compositionalMinimization;
        private final JComboBox<MinimizationStrategy> minimizationStrategy;
        private final JComboBox<MinimizationHeuristic> minimizationHeuristic;
        private final Alphabet unionAlphabet;
        private Alphabet targetAlphabet;
        private final JList<String> targetAlphabetSelector;

        //private JTextArea note;

        public MinimizationDialogAdvancedPanel(final Automata automata)
        {
            unionAlphabet = automata.getUnionAlphabet();
            targetAlphabet = null;

            // Strategy
            minimizationStrategy = new JComboBox<MinimizationStrategy>(MinimizationStrategy.values());

            // Strategy
            minimizationHeuristic = new JComboBox<MinimizationHeuristic>(MinimizationHeuristic.values());

            // Compositional?
            compositionalMinimization = new JCheckBox("Compositional minimization (keep selected events)");
            compositionalMinimization.setToolTipText("Minimizes selected automata compositionally, keeps the events that have been selected from the list below");
            compositionalMinimization.addActionListener(this);

            // Alphabetselector
            final DefaultListModel<String> list = new DefaultListModel<String>();
            for (final Iterator<LabeledEvent> evIt = unionAlphabet.iterator(); evIt.hasNext(); )
            {
                final LabeledEvent event = evIt.next();

                // Only non-epsilon events!
                if (event.isObservable())
                {
                    list.addElement(event.getLabel());
                }
            }
            targetAlphabetSelector = new JList<String>(list);
            targetAlphabetSelector.setVisibleRowCount(6);
            targetAlphabetSelector.setPrototypeCellValue("AT LEAST THIS WIDE");
            final JScrollPane alphaPane = new JScrollPane(targetAlphabetSelector);

            // Create layout!
            final Box mainBox = Box.createVerticalBox();

            JPanel panel = new JPanel();
            panel.add(compositionalMinimization);
            mainBox.add(panel);

            panel = new JPanel();
            final Box strategyBox = Box.createHorizontalBox();
            strategyBox.add(new JLabel("Strategy: "));
            strategyBox.add(minimizationStrategy);
            panel.add(strategyBox);
            mainBox.add(panel);

            panel = new JPanel();
            final Box heuristicBox = Box.createHorizontalBox();
            heuristicBox.add(new JLabel("Heuristic: "));
            heuristicBox.add(minimizationHeuristic);
            panel.add(heuristicBox);
            mainBox.add(panel);

            mainBox.add(alphaPane);

            // Add components
            this.add(mainBox);
        }

        public void update(final MinimizationOptions options)
        {
            // compositionalMinimization.setSelected(options.getCompositionalMinimization());
            minimizationStrategy.setSelectedItem(options.getMinimizationStrategy());
            minimizationHeuristic.setSelectedItem(options.getMinimizationHeuristic());
            updatePanel();
        }

        /**
         * Options that are not related to the current MinimizationOptions.
         */
        public void updatePanel()
        {
            targetAlphabetSelector.setEnabled(compositionalMinimization.isSelected());
            minimizationStrategy.setEnabled(compositionalMinimization.isSelected());
            minimizationHeuristic.setEnabled(compositionalMinimization.isSelected());
            this.repaint();
        }

        public void regain(final MinimizationOptions options)
        {
            // Update targetAlphabet
            if (compositionalMinimization.isSelected())
            {
                targetAlphabet = new Alphabet();
                //int[] selected = targetAlphabetSelector.getSelectedIndexes();
                final int[] selected = targetAlphabetSelector.getSelectedIndices();
                //String[] selected = (String[]) targetAlphabetSelector.getSelectedValues();
                //String[] selected = targetAlphabetSelector.getModel();
                final ListModel<String> list = targetAlphabetSelector.getModel();
                for (int i=0; i<selected.length; i++)
                {
                    //targetAlphabet.addEvent(unionAlphabet.getEvent(targetAlphabetSelector.getItem(selected[i])));
                    //System.err.println("Seleceted: " + (String) selected[i]);
                    //targetAlphabet.addEvent(unionAlphabet.getEvent(selected[i]));
                    final String label = list.getElementAt(selected[i]);
                    targetAlphabet.addEvent(unionAlphabet.getEvent(label));
                }
            }
            else
            {
                targetAlphabet = null;
            }

            options.setCompositionalMinimization(compositionalMinimization.isSelected());
            options.setMinimizationStrategy((MinimizationStrategy) minimizationStrategy.getSelectedItem());
            options.setMinimizationHeuristic((MinimizationHeuristic) minimizationHeuristic.getSelectedItem());
            options.setTargetAlphabet(targetAlphabet);
        }

        public void actionPerformed(final ActionEvent e)
        {
            updatePanel();

                        /*
                          if (compositionalMinimization.isSelected())
                          {
                          targetAlphabetSelector.setEnabled(true);
                          //targetAlphabetSelector.setVisible(true);
                          //note.setText("Note:\n" + "This minimization algorithm is experimental!");
                          //note.setVisible(true);
                          }
                          else
                          {
                          targetAlphabetSelector.setEnabled(false);
                          //targetAlphabetSelector.setVisible(false);
                          //note.setVisible(false);
                          }
                         */
        }
    }
}





