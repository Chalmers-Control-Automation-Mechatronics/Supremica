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

import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterJScrollPane;
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.util.DialogCancelAction;
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
   * Used when using the JComboBOx to switch between multiple algorithms,
   * generateAnalyzerCombobox() populates JComboBox
   */
  public AbstractAnalysisDialog(final WatersAnalyzerPanel panel, final AnalyzerProductDESContext model)
  {
    super((Frame) panel.getTopLevelAncestor());

    DESContext = model;
    mAnalyzerPanel = panel;
    mAutomata = mAnalyzerPanel.getAutomataTable().getOperationArgument();

    AllParams = new HashMap<Integer,Parameter>();
    factory = ProductDESElementFactory.getInstance();
    des = AutomatonTools.createProductDESProxy("synchronousForAnalyzer", mAutomata, factory);
    generateAnalyzerCombobox();
    generateGUI();
    setLocationRelativeTo(mAnalyzerPanel.getTopLevelAncestor());
    setVisible(true);
  }

  /**
   * Used when only using one algorithm, generateAnalyzerCombobox() not used
   */
  public AbstractAnalysisDialog(final WatersAnalyzerPanel panel, final ModelAnalyzer analyzer, final AnalyzerProductDESContext model)
  {
    super((Frame) panel.getTopLevelAncestor());

    DESContext = model;
    mAnalyzerPanel = panel;
    mAutomata = mAnalyzerPanel.getAutomataTable().getOperationArgument();

    AllParams = new HashMap<Integer,Parameter>();
    factory = ProductDESElementFactory.getInstance();
    des = AutomatonTools.createProductDESProxy(mAnalyzerPanel.getModuleContainer().getName(), mAutomata, factory);

    mAnalyzer = analyzer;
    generateGUI();
    setLocationRelativeTo(mAnalyzerPanel.getTopLevelAncestor());
    setVisible(true);
  }

  //#########################################################################
  //# Using Parameter Classes

  /**
   * Generates the JComboBox that is at the top of the frame, stores the list
   * of all available algorithms only if more than one algorithms are to be used
   */
  public void generateAnalyzerCombobox()
  {
    final JPanel mSuperviserPanel = new JPanel(new GridLayout(0, 2));
    final JLabel superviserComboboxLabel = new JLabel("Algorithms");
    analyzerCombobox = new JComboBox<>();

    for (final ModelAnalyzerFactoryLoader dir : ModelAnalyzerFactoryLoader.values()) {
      try {
        final ModelAnalyzer s = createAnalyzer(dir.getModelAnalyzerFactory(), factory);

        if (s != null){
          analyzerCombobox.addItem(dir);
          //Store new parameter in database
          for(final Parameter p : s.getParameters())
            AllParams.put(p.getID(),p);
        }
      } catch (NoClassDefFoundError | ClassNotFoundException | UnsatisfiedLinkError exception) {     }
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

    analyzerCombobox.addActionListener(analyzerChanged);

    mSuperviserPanel.add(superviserComboboxLabel);
    mSuperviserPanel.add(analyzerCombobox);
    add(mSuperviserPanel, BorderLayout.PAGE_START);
  }

  public void analysisChanged()
  {
    try {
      mAnalyzer =
        createAnalyzer(((ModelAnalyzerFactoryLoader) analyzerCombobox
          .getSelectedItem()).getModelAnalyzerFactory(), factory);
    } catch (final ClassNotFoundException exception) {
      exception.printStackTrace();
    }

    final List<Parameter> newParams = mAnalyzer.getParameters();

    storeInDatabase();
    copyFromDatabase(newParams);
    mScrollParametersPanel.replaceView(newParams, DESContext);

    //re-packing causes the frame to shrink/increase to preferred size
    pack();
  }

  public void generateGUI()
  {
    //ModelAnalyzer not supplied on construction, JComboBox being used
    if (mAnalyzer == null) {
      try {
        mAnalyzer =
          createAnalyzer(((ModelAnalyzerFactoryLoader) analyzerCombobox
            .getSelectedItem()).getModelAnalyzerFactory(), factory);
      } catch (final ClassNotFoundException exception) {      }
    }

    mScrollParametersPanel = new ParameterJScrollPane(mAnalyzer.getParameters(), DESContext);

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
      AllParams.put(p.getID(), p);
    }
  }

  /**
   * updates the passed parameters to have same stored value as corresponding
   * one in database
   *
   * @param parametersToStore
   *          the list of parameters that are to be stored in the database of
   *          all parameters
   */
  public void copyFromDatabase(final List<Parameter> parametersToStore)
  {
    for (final Parameter current : parametersToStore)
      current.updateFromParameter(AllParams.get(current.getID()));
  }

  public void printMap()
  {
    for (final Entry<Integer,Parameter> entry : AllParams.entrySet()) {
      entry.getValue().printValue();
    }
  }

  /**
   * Class specific way to generate a modelAnalyzer
   */
  protected abstract ModelAnalyzer createAnalyzer(ModelAnalyzerFactory analyzerFactory,
                                                        ProductDESProxyFactory desFactory);


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

    //commit all of the values to the synthesizer
    for (final Parameter current : parameters)
      current.commitValue();


    if(mAnalyzer instanceof CompositionalConflictChecker)
      System.out.println(((CompositionalConflictChecker)mAnalyzer).isDetailedOutputEnabled());

    final IDE ide = mAnalyzerPanel.getModuleContainer().getIDE();
    final WatersAnalyzeDialog dialog = createAnalyzeDialog(ide, des);
    dispose();
    if(dialog != null)
      dialog.setVisible(true);
  }

  /**
   * Generates the pop up dialog that shows the result of using the analyzer
   */
  protected abstract WatersAnalyzeDialog createAnalyzeDialog(IDE ide, ProductDESProxy des);

  //#########################################################################
  //# Data Members
  private ParameterJScrollPane mScrollParametersPanel;
  private final HashMap<Integer,Parameter> AllParams;
  private JPanel mButtonsPanel;
  private JComboBox<ModelAnalyzerFactoryLoader> analyzerCombobox;
  private final List<AutomatonProxy> mAutomata;
  private final WatersAnalyzerPanel mAnalyzerPanel;
  private final ProductDESProxyFactory factory;
  private final ProductDESProxy des;
  private ModelAnalyzer mAnalyzer;
  private final AnalyzerProductDESContext DESContext;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
