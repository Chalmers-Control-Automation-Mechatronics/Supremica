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
import net.sourceforge.waters.gui.dialog.WatersVerifyDialog;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;

/**
 * @author Brandon Bassett
 */
public class ConflictCheckDialog extends JDialog
{

  //#########################################################################
  //# Constructor
  public ConflictCheckDialog(final WatersAnalyzerPanel panel)
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

    final JPanel mConflictCheckPanel = new JPanel(new GridLayout(0,2));
    conflictCheckCombobox = new JComboBox<>();
    final JLabel conflictCheckComboboxLabel = new JLabel("Algorithms");
    AllParams = new HashMap<Integer,Parameter>();

    for (final ModelAnalyzerFactoryLoader dir : ModelAnalyzerFactoryLoader.values()) {
      try {
        final ConflictChecker s = dir.getModelAnalyzerFactory().createConflictChecker(ProductDESElementFactory.getInstance());

        if (s != null){
          conflictCheckCombobox.addItem(dir);
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

    mConflictCheckPanel.add(conflictCheckComboboxLabel);
    mConflictCheckPanel.add(conflictCheckCombobox);
    //mConflictPanel.add(print);

    final ProductDESProxyFactory factory =  ProductDESElementFactory.getInstance();
    final ProductDESProxy des = AutomatonTools.createProductDESProxy("synchronousForAnalyzer",   mAutomata, factory);

    final ActionListener conflictCheckChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final ModelAnalyzerFactoryLoader tmp =
          (ModelAnalyzerFactoryLoader) conflictCheckCombobox.getSelectedItem();

        try {
          mConflictCheck = tmp.getModelAnalyzerFactory()
            .createConflictChecker(ProductDESElementFactory.getInstance());
          final List<Parameter> newParams = mConflictCheck.getParameters();

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

    conflictCheckCombobox.addActionListener(conflictCheckChanged);

    // superviserCombobox should have at least one item
    final ModelAnalyzerFactoryLoader first = (ModelAnalyzerFactoryLoader) conflictCheckCombobox.getSelectedItem();

    try {
      mConflictCheck = first.getModelAnalyzerFactory()
        .createConflictChecker(ProductDESElementFactory.getInstance());
      mScrollParametersPanel = new ParameterJScrollPane(mConflictCheck.getParameters(),des);
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
    add(mConflictCheckPanel, BorderLayout.PAGE_START);
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
    final List<Parameter> parameters = mConflictCheck.getParameters();
    storeInDatabase();
    copyFromDatabase(parameters);

    //commit all of the values to the synthesizer
    for(final Parameter current: parameters)
      current.commitValue();

    final IDE ide = mAnalyzerPanel.getModuleContainer().getIDE();
    final ProductDESProxyFactory factory =  ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(mAnalyzerPanel.getModuleContainer().getName(), mAutomata, factory);

    final ConflictCheckPopUpDialog dialog = new ConflictCheckPopUpDialog(ide, des);
    dispose();
    dialog.setVisible(true);
  }

  //#########################################################################
  //# Inner Class AnalyzerDialog
  private class ConflictCheckPopUpDialog extends WatersVerifyDialog
  {
    //#######################################################################
    //# Constructor
    public ConflictCheckPopUpDialog(final IDE owner,
                                final ProductDESProxy des)
    {
      super(owner, des);
    }

    @Override
    protected String getFailureDescription()
    {
      return "is blocking";
    }

    @Override
    protected String getSuccessDescription()
    {
      return "is non-blocking";
    }

    @Override
    protected String getAnalysisName()
    {
      return "Conflict Check";
    }

    @Override
    protected ModelAnalyzer createModelAnalyzer()
    {
      return mConflictCheck;
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
  private JComboBox<ModelAnalyzerFactoryLoader> conflictCheckCombobox;

  // Analysis workers
  private ConflictChecker mConflictCheck;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -4771975182146634793L;

}
