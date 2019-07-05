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

package net.sourceforge.waters.gui.analyzer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterJScrollPane;
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;

/**
 * @author George Hewlett, Robi Malik, Brandon Bassett
 */
public class SynthesisDialog extends JDialog
{

  //#########################################################################
  //# Constructor
  public SynthesisDialog(final WatersAnalyzerPanel panel)
  {
    super((Frame) panel.getTopLevelAncestor());

    setTitle("Supervisor synthesis");
    mAnalyzerPanel = panel;
    mAutomata = panel.getAutomataTable().getOperationArgument();
    generateGUI();
    setLocationRelativeTo(panel.getTopLevelAncestor());
    setVisible(true);
  }


  //#########################################################################
  //# Using Parameter Classes
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
    //mSuperviserPanel.add(print);

    final ProductDESProxyFactory factory =  ProductDESElementFactory.getInstance();

    final ProductDESProxy des = AutomatonTools.createProductDESProxy("synchronousForAnalyzer",   mAutomata, factory);

    final ActionListener syntheisSuperviserChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final ModelAnalyzerFactoryLoader tmp =
          (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

        try {
          mSynthesizer = tmp.getModelAnalyzerFactory()
            .createSupervisorSynthesizer(ProductDESElementFactory.getInstance());
          final List<Parameter> newParams = mSynthesizer.getParameters();

          storeInDatabase();
          copyFromDatabase(newParams);
          mScrollParametersPanel.replaceView(newParams, des);
        } catch (AnalysisConfigurationException |
                 ClassNotFoundException exception) {
          final Logger logger = LogManager.getLogger();
          logger.error(exception.getMessage());
        }
        //re-packing causes the frame to shrink/increase to preferred size
        pack();
      }
    };

    superviserCombobox.addActionListener(syntheisSuperviserChanged);

    // superviserCombobox should have at least one item
    final ModelAnalyzerFactoryLoader first = (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

    try {
      mSynthesizer = first.getModelAnalyzerFactory()
        .createSupervisorSynthesizer(ProductDESElementFactory.getInstance());
      mScrollParametersPanel = new ParameterJScrollPane(mSynthesizer.getParameters(),des);
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
    final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy("synchronousForAnalyzer", mAutomata, factory);

    final List<Parameter> parameters = mSynthesizer.getParameters();
    storeInDatabase();
    copyFromDatabase(parameters);

    //commit all of the values to the synthesizer
    for(final Parameter current: parameters)
      current.commitValue();

    final IDE ide = mAnalyzerPanel.getModuleContainer().getIDE();
    final SynthesisPopUpDialog dialog = new SynthesisPopUpDialog(ide, des);
    dispose();
    dialog.setVisible(true);
  }


  //#########################################################################
  //# Inner Class AnalyzerDialog
  private class SynthesisPopUpDialog extends WatersAnalyzeDialog
  {
    //#######################################################################
    //# Constructor
    public SynthesisPopUpDialog(final IDE owner,
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
      final Collection<? extends AutomatonProxy> supervisors =
        result.getComputedAutomata();
      if (supervisors != null) {
        final AutomataTableModel model = mAnalyzerPanel.getAutomataTableModel();
        model.insertRows(supervisors);
      }
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
      final ProductDESResult result = mSynthesizer.getAnalysisResult();
      final Collection<? extends AutomatonProxy> supervisors =
        result.getComputedAutomata();
      if (supervisors == null) {
        return "Synthesis successful. " +
               "A supervisor exists, but it has not been constructed.";
      } else {
        final int size = supervisors.size();
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
  private ParameterJScrollPane mScrollParametersPanel;
  private HashMap<Integer,Parameter> AllParams;
  private JPanel mButtonsPanel;
  private JComboBox<ModelAnalyzerFactoryLoader> superviserCombobox;

  // Analysis workers
  private SupervisorSynthesizer mSynthesizer;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6159733639861131531L;
}
