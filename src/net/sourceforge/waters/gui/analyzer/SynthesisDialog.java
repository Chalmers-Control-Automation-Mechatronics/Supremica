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

import java.util.Collection;

import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;

/**
 * @author George Hewlett, Robi Malik, Brandon Bassett
 */
public class SynthesisDialog extends AbstractAnalysisDialog
{

  //#########################################################################
  //# Constructor
  public SynthesisDialog(final WatersAnalyzerPanel panel)
  {
    super(panel);
    //super(panel, new MonolithicSynthesizer(ProductDESElementFactory.getInstance()));
    setTitle("Supervisor synthesis");
  }

//#######################################################################
  //# Overrides for net.sourceforge.waters.gui.dialog.AbstractAnalysisDialog

  @Override
  public void populateAlgorithmComboBox()
  {
    for (final ModelAnalyzerFactoryLoader dir : ModelAnalyzerFactoryLoader.values()) {
      try {
        final ModelAnalyzer s = dir.getModelAnalyzerFactory().createSupervisorSynthesizer(ProductDESElementFactory.getInstance());

        if (s != null){
          analyzerCombobox.addItem(dir);
          //Store new parameter in database
          for(final Parameter p : s.getParameters())
            AllParams.put(p.getID(),p);
        }
      } catch (NoClassDefFoundError | ClassNotFoundException | UnsatisfiedLinkError
        | AnalysisConfigurationException exception) {     }
    }
  }

  @Override
  public void generateAnalyser(final ModelAnalyzerFactoryLoader loader)
  {
    try {
      mAnalyzer = loader.getModelAnalyzerFactory()
        .createSupervisorSynthesizer(ProductDESElementFactory.getInstance());
    }
    catch (AnalysisConfigurationException | ClassNotFoundException exception) {
      final Logger logger = LogManager.getLogger();
      logger.error(exception.getMessage());
    }
  }

  @Override
  public void generateResultsDialog()
  {
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
      final ProductDESResult result = (ProductDESResult) mAnalyzer.getAnalysisResult();
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
      final ProductDESResult result = (ProductDESResult) mAnalyzer.getAnalysisResult();
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
      return mAnalyzer;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6159733639861131531L;
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6159733639861131531L;


}
