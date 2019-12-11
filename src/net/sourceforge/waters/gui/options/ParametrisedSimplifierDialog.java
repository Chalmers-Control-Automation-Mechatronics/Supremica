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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.StepSimplifierFactory;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionEditor;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.trcomp.ChainSimplifierFactory;
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

import org.supremica.automata.waters.SupremicaSimplifierFactory;
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

    mOptionDB = OptionMap.Simplifier;
    mCurrentParameterPanels = new LinkedList<>();

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    //Family selector combo box
    final JLabel familyComboboxLabel = new JLabel("Family");
    mFamilyComboBox = new JComboBox<>();

    mFamilyComboBox.addItem(ChainSimplifierFactory.getInstance());
    mFamilyComboBox.addItem(StepSimplifierFactory.getInstance());
    mFamilyComboBox.addItem(SupremicaSimplifierFactory.getInstance());

    final ActionListener familyChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        updateSimplifierList();
        pack();
      }
    };

    mFamilyComboBox.addActionListener(familyChanged);

    // Algorithm selector combo box
    final JLabel algorithmComboboxLabel = new JLabel("Simplifier");
    mAnalyzerComboBox = new JComboBox<>();

    final ActionListener algorithmChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        showAlgorithmParameters();
        pack();
      }
    };
    mAnalyzerComboBox.addActionListener(algorithmChanged);
    mAnalyzerComboBox.setRenderer(new ComboboxToolTipRenderer(300));

    //Description
    mDescriptionTextPane = new JTextPane();
    mDescriptionTextPane.setContentType("text/html");
    mDescriptionTextPane.setBackground(getBackground());
    //Prevent selection
    for (final MouseListener l : mDescriptionTextPane
      .getListeners(MouseListener.class)) {
      mDescriptionTextPane.removeMouseListener(l);
    }

    //Selection panel
    final JPanel selectionPanel = new RaisedDialogPanel();
    selectionPanel.setLayout(new GridBagLayout());
    constraints.weighty = 1.0f;
    constraints.fill = GridBagConstraints.BOTH;
    add(selectionPanel, constraints);
    final GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.insets.right = 10;
    c.weightx = 0.0f;
    c.weightx = 0;
    selectionPanel.add(familyComboboxLabel, c);
    c.gridx++;
    c.insets.right = 0;
    c.weightx = 1.0f;
    selectionPanel.add(mFamilyComboBox, c);
    c.gridx = 0;
    c.gridy++;
    c.insets.right = 10;
    c.weightx = 0.0f;
    selectionPanel.add(algorithmComboboxLabel, c);
    c.gridx++;
    c.insets.right = 0;
    c.weightx = 1.0f;
    selectionPanel.add(mAnalyzerComboBox, c);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    c.insets.top = 5;
    c.weightx = 1.0f;
    c.weighty = 1.0f;
    c.fill = GridBagConstraints.BOTH;
    final JScrollPane scrollDescription = new JScrollPane(mDescriptionTextPane) {
      @Override
      public Dimension getPreferredSize()
      {
        final Dimension d = super.getPreferredSize();
        d.width = 0;
        return d;
      }
      private static final long serialVersionUID = -7065386236668370127L;
    };
    selectionPanel.add(scrollDescription, c);


    // Parameter list
    mParameterListPanel = new JPanel();
    mParameterListPanel.setLayout(new GridBagLayout());
    final JScrollPane scroll = new JScrollPane(mParameterListPanel);
    final JPanel scrollPanel = new RaisedDialogPanel(0);
    scrollPanel.setLayout(new GridBagLayout());
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weighty = 4.0;
    scrollPanel.add(scroll, constraints);
    add(scrollPanel, constraints);

    updateSimplifierList();

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
  //# Updating Algorithm
  private void showAlgorithmParameters()
  {
    final int index = mAnalyzerComboBox.getSelectedIndex();
    if (index == -1) return;
    final AutomatonSimplifierCreator creator = mAnalyzerComboBox.getItemAt(index);
    mCurrentAnalyzer = creator.createBuilder(mContext.getProductDESProxyFactory());
    final List<Option<?>> params = mCurrentAnalyzer.getOptions(mOptionDB);
    params.addAll(creator.getOptions(mOptionDB));
    mKeepOriginalOption = new BooleanOption
      (AutomatonSimplifierFactory.OPTION_AutomatonSimplifierFactory_KeepOriginal,
       "Keep Original",
       "Do not remove the input automaton from the analyzer " +
       "after this operation.",
       "-keep",
       true);
    params.add(mKeepOriginalOption);

    final String text = "<body style='text-align:justify'>"
      + creator.getDescription() + "</body>";
    mDescriptionTextPane.setText(text);
    mDescriptionTextPane.setCaretPosition(0);

    updateParameterList(params);
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

  private void updateSimplifierList() {
    mAnalyzerComboBox.removeAllItems();
    final AutomatonSimplifierFactory factory =
      (AutomatonSimplifierFactory) mFamilyComboBox.getSelectedItem();
    for (final AutomatonSimplifierCreator creator :
         factory.getSimplifierCreators()) {
      mAnalyzerComboBox.addItem(creator);
    }
    showAlgorithmParameters();
  }

  //#########################################################################
  //# Simple Access
  public GUIOptionContext getContext()
  {
    return mContext;
  }

  //#########################################################################
  //# Hooks

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

      final ProductDESProxyFactory factory = mContext.getProductDESProxyFactory();
      final AutomatonSimplifierCreator creator =
        (AutomatonSimplifierCreator) mAnalyzerComboBox.getSelectedItem();

      final FocusTracker tracker = ide.getFocusTracker();
      if (tracker.shouldYieldFocus(this)) {
        for (final OptionPanel<?> optionPanel : mCurrentParameterPanels) {
          optionPanel.commitValue();
          final Option<?> option = optionPanel.getOption();
          creator.setOption(option);
        }

        final AutomatonBuilder builder = creator.createBuilder(factory);
        builder.setModel(aut);

        final boolean keepOriginal = mKeepOriginalOption.getBooleanValue();
        if (keepOriginal) {
          final String newName = model.getUnusedName(aut.getName());
          builder.setOutputName(newName);
        }
        else builder.setOutputName(aut.getName());

        for (final OptionPanel<?> optionPanel : mCurrentParameterPanels) {
          optionPanel.commitValue();
          final Option<?> option = optionPanel.getOption();
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

  public class ComboboxToolTipRenderer extends DefaultListCellRenderer {

    public ComboboxToolTipRenderer(final int toolTipWidth)
    {
      super();
      mToolTipWidth = toolTipWidth;
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus) {
      if (value != null) {
        final AutomatonSimplifierCreator creator = (AutomatonSimplifierCreator) value;
        final String text = creator.getDescription();

        if (text.length() != 0) {
          final String htmlText = "<html><p width=" + mToolTipWidth + ">"
            + text + "</p></html>";

          list.setToolTipText(htmlText);
        }
        else list.setToolTipText(null);
      }
      return super.getListCellRendererComponent(list, value, index,
                                                isSelected, cellHasFocus);
    }

    private final int mToolTipWidth;

    private static final long serialVersionUID = -3041815919444247332L;
  }

  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final JComboBox<AutomatonSimplifierFactory> mFamilyComboBox;
  private final JComboBox<AutomatonSimplifierCreator> mAnalyzerComboBox;
  private final JTextPane mDescriptionTextPane;
  private final JPanel mParameterListPanel;
  private final OptionMap mOptionDB;

  private BooleanOption mKeepOriginalOption;

  private AutomatonBuilder mCurrentAnalyzer;
  private final List<OptionPanel<?>> mCurrentParameterPanels;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
