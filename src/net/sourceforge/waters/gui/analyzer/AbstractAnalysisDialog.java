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
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;


/**
 * Abstract class that auto-generates a GUI that is based on the provided algorithm(s) getParameters method
 * where one is provided on creation or populateAlgorithmComboBox() uses a class specific
 * list of algorithms
 *
 * @author Brandon Bassett
 */
public abstract class AbstractAnalysisDialog extends JDialog
{
  //#########################################################################
  //# Constructor
  /**
   * Used when using the JComboBox to switch between multiple algorithms,
   * generateAnalyzerCombobox() populates JComboBox.
   */
  public AbstractAnalysisDialog(final WatersAnalyzerPanel panel)
  {
    super((Frame) panel.getTopLevelAncestor());
    mDESContext = new AnalyzerProductDESContext(panel);
    mAnalyzerPanel = panel;
    mAutomata = mAnalyzerPanel.getAutomataTable().getOperationArgument();
    mParameterDB = new HashMap<Integer,Parameter>();
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    generateAnalyzerCombobox();
    generateGUI();
    setLocationRelativeTo(mAnalyzerPanel.getTopLevelAncestor());
    setVisible(true);
  }

  /**
   * Used when only using one algorithm, generateAnalyzerCombobox() not used.
   */
  public AbstractAnalysisDialog(final WatersAnalyzerPanel panel,
                                final ModelAnalyzer analyzer)
  {
    super((Frame) panel.getTopLevelAncestor());
    mDESContext = new AnalyzerProductDESContext(panel);
    mAnalyzerPanel = panel;
    mAutomata = mAnalyzerPanel.getAutomataTable().getOperationArgument();
    mParameterDB = new HashMap<Integer,Parameter>();
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    mAnalyzer = analyzer;
    generateGUI();
    setLocationRelativeTo(mAnalyzerPanel.getTopLevelAncestor());
    setVisible(true);
  }


  //#########################################################################
  //# Simple Access
  ProductDESProxyFactory getProductDESProxyFactory()
  {
    return ProductDESElementFactory.getInstance();
  }


  //#########################################################################
  //# Using Parameter Classes
  /**
   * Generates the JComboBox that is at the top of the frame, stores the list
   * of all available algorithms only if more than one algorithms are to be used
   */
  public void generateAnalyzerCombobox()
  {
    final JPanel mAlgorithmPanel = new JPanel(new GridLayout(0, 2));
    final JLabel algorithmComboboxLabel = new JLabel("Algorithm");
    mAnalyzerComboBox = new JComboBox<>();

    for (final ModelAnalyzerFactoryLoader loader : ModelAnalyzerFactoryLoader.values()) {
      final ModelAnalyzer analyzer = createAnalyzer(loader);
      if (analyzer != null) {
        mAnalyzerComboBox.addItem(loader);
        //Store new parameter in database
        for (final Parameter param : analyzer.getParameters()) {
          mParameterDB.put(param.getID(), param);
        }
      }
    }

    //Testing
    /*
     * final ActionListener Print = new ActionListener() {
     *
     * @Override public void actionPerformed(final ActionEvent event) {
     * storeInDatabase(); printMap(); } };
     *
     * final JButton print = new JButton("Print Database");
     * print.addActionListener(Print);
     */

    final ActionListener analyzerChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        analysisChanged();
      }
    };

    mAnalyzerComboBox.addActionListener(analyzerChanged);

    mAlgorithmPanel.add(algorithmComboboxLabel);
    mAlgorithmPanel.add(mAnalyzerComboBox);
    add(mAlgorithmPanel, BorderLayout.PAGE_START);
  }

  public void analysisChanged()
  {
    final int index = mAnalyzerComboBox.getSelectedIndex();
    final ModelAnalyzerFactoryLoader loader = mAnalyzerComboBox.getItemAt(index);
    mAnalyzer = createAnalyzer(loader);
    final List<Parameter> newParams = mAnalyzer.getParameters();
    storeInDatabase();
    copyFromDatabase(newParams);
    mScrollParametersPanel.replaceView(newParams, mDESContext);
    //re-packing causes the frame to shrink/increase to preferred size
    pack();
  }

  public void generateGUI()
  {
    final int index = mAnalyzerComboBox.getSelectedIndex();
    final ModelAnalyzerFactoryLoader loader = mAnalyzerComboBox.getItemAt(index);
    mAnalyzer = createAnalyzer(loader);
    mScrollParametersPanel = new ParameterJScrollPane(mAnalyzer.getParameters(), mDESContext);

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
    add(mScrollParametersPanel, BorderLayout.CENTER);
    add(mButtonsPanel, BorderLayout.PAGE_END);
    pack();
    setVisible(true);
  }

  /**
   * Values stored in GUI Components are stored in corresponding parameter
   * then added to the database
   */
  public void storeInDatabase()
  {
    mScrollParametersPanel.commit(); //All ParameterPanels save their stored value in their corresponding parameter
    final List<Parameter> activeParameters =
      mScrollParametersPanel.getParameters();

    for (final Parameter p : activeParameters) { //overwrite stored parameters with new version
      mParameterDB.put(p.getID(), p);
    }
  }

  /**
   * updates the passed parameters to have same stored value as corresponding
   * one in database
   *
   * @param parametersToStore
   *          the list of parameters that are to be updated using
   *          the corresponding parameter in the database
   */
  public void copyFromDatabase(final List<Parameter> parametersToStore)
  {
    for (final Parameter current : parametersToStore)
      current.updateFromParameter(mParameterDB.get(current.getID()));
  }

  public void printMap()
  {
    for (final Entry<Integer,Parameter> entry : mParameterDB.entrySet()) {
      System.out.println(entry.getValue().toString());
    }
  }

  /**
   * Class-specific way to generate a model analyser.
   */
  protected abstract ModelAnalyzer createAnalyzer(ModelAnalyzerFactory factory)
    throws AnalysisConfigurationException;

  private ModelAnalyzer createAnalyzer(final ModelAnalyzerFactoryLoader loader)
  {
    try {
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      return createAnalyzer(factory);
    } catch (NoClassDefFoundError |
             ClassNotFoundException |
             UnsatisfiedLinkError |
             AnalysisConfigurationException exception) {
      return null;
    }
  }


  protected ModelAnalyzer getAnalyzer()
  {
    return mAnalyzer;
  }

  protected WatersAnalyzerPanel getWatersAnalyzerPanel()
  {
    return mAnalyzerPanel;
  }

  public void ParameterCommitDialog()
  {
    final List<Parameter> parameters = mAnalyzer.getParameters();
    storeInDatabase();
    copyFromDatabase(parameters);

    // commit all of the values to the model analyser
    for (final Parameter current : parameters) {
      current.commitValue();
    }
    final IDE ide = mAnalyzerPanel.getModuleContainer().getIDE();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(mAnalyzerPanel.getModuleContainer().getName(),
                                           mAutomata, mProductDESProxyFactory);
    final WatersAnalyzeDialog dialog = createAnalyzeDialog(ide, des);
    dispose();
    if (dialog != null) {
      dialog.setVisible(true);
    }
  }

  /**
   * Generates the pop up dialog that shows the result of using the analyser.
   */
  protected abstract WatersAnalyzeDialog createAnalyzeDialog(IDE ide,
                                                             ProductDESProxy des);


  //#########################################################################
  //# Data Members
  private ParameterJScrollPane mScrollParametersPanel;
  private final HashMap<Integer,Parameter> mParameterDB;
  private JPanel mButtonsPanel;
  private JComboBox<ModelAnalyzerFactoryLoader> mAnalyzerComboBox;
  private final List<AutomatonProxy> mAutomata;
  private final WatersAnalyzerPanel mAnalyzerPanel;
  private final ProductDESProxyFactory mProductDESProxyFactory;
  private ModelAnalyzer mAnalyzer;
  private final AnalyzerProductDESContext mDESContext;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
