
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

        private final JComboBox minimizationTypeBox;
        private final JCheckBox alsoTransitions;
        private final JCheckBox keepOriginal;
        private final JCheckBox ignoreMarking;

        private final JTextArea note;

        public MinimizationDialogStandardPanel()
        {
            minimizationTypeBox =
              new JComboBox(EquivalenceRelation.enabledValues());
            minimizationTypeBox.addActionListener(this);

            // Disable bisimulation equivalence if library is missing!
            if (!BisimulationEquivalenceMinimizer.libraryLoaded())
            {
                logger.warn("Library BisimulationEquivalence not in path, using Java implementation.");
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
        private final JComboBox minimizationStrategy;
        private final JComboBox minimizationHeuristic;
        private final Alphabet unionAlphabet;
        private Alphabet targetAlphabet;
        private final JList targetAlphabetSelector;

        //private JTextArea note;

        public MinimizationDialogAdvancedPanel(final Automata automata)
        {
            unionAlphabet = automata.getUnionAlphabet();
            targetAlphabet = null;

            // Strategy
            minimizationStrategy = new JComboBox(MinimizationStrategy.values());

            // Strategy
            minimizationHeuristic = new JComboBox(MinimizationHeuristic.values());

            // Compositional?
            compositionalMinimization = new JCheckBox("Compositional minimization (keep selected events)");
            compositionalMinimization.setToolTipText("Minimizes selected automata compositionally, keeps the events that have been selected from the list below");
            compositionalMinimization.addActionListener(this);

            // Alphabetselector
            final DefaultListModel list = new DefaultListModel();
            for (final Iterator<LabeledEvent> evIt = unionAlphabet.iterator(); evIt.hasNext(); )
            {
                final LabeledEvent event = evIt.next();

                // Only non-epsilon events!
                if (event.isObservable())
                {
                    list.addElement(event.getLabel());
                }
            }
            targetAlphabetSelector = new JList(list);
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
                final ListModel list = targetAlphabetSelector.getModel();
                for (int i=0; i<selected.length; i++)
                {
                    //targetAlphabet.addEvent(unionAlphabet.getEvent(targetAlphabetSelector.getItem(selected[i])));
                    //System.err.println("Seleceted: " + (String) selected[i]);
                    //targetAlphabet.addEvent(unionAlphabet.getEvent(selected[i]));
                    final String label = (String) list.getElementAt(selected[i]);
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
