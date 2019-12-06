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

import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.StateCounter;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;


/**
 * @author Brandon Bassett
 */
public class StateCounterDialog extends ParametrisedAnalysisDialog
{

  //#######################################################################
  //# Constructor
  public StateCounterDialog(final WatersAnalyzerPanel panel)
  {
    super(panel);
    setTitle(TITLE);
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.dialog.AbstractAnalysisDialog
  @Override
  protected StateCounter createAnalyzer(final ModelAnalyzerFactory factory)
    throws AnalysisConfigurationException
  {
    final ProductDESProxyFactory desFactory = getProductDESProxyFactory();
    return factory.createStateCounter(desFactory);
  }

  @Override
  protected StateCounter getAnalyzer()
  {
    return (StateCounter) super.getAnalyzer();
  }

  @Override
  protected WatersAnalyzeDialog createAnalyzeDialog(final IDE ide,
                                                    final ProductDESProxy des)
  {
    return new StateCounterPopUpDialog(ide, des);
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog
  @Override
  protected String getOptionPrefix()
  {
    return OPTION_PREFIX;
  }

  //#########################################################################
  //# Inner Class StateCounterPopUpDialog
  private class StateCounterPopUpDialog extends WatersAnalyzeDialog
  {
    //#######################################################################
    //# Constructor
    public StateCounterPopUpDialog(final IDE owner,
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
    }

    @Override
    protected String getAnalysisName()
    {
      return TITLE;
    }

    @Override
    protected String getFailureText()
    {
      return "State Counter has failed.";
    }

    @Override
    protected String getSuccessText()
    {
      final AnalysisResult result = getAnalyzer().getAnalysisResult();
      final double numStates = result.getTotalNumberOfStates();
      final double numTrans = result.getTotalNumberOfTransitions();
      final Logger logger = LogManager.getFormatterLogger();
      if (numTrans >= 0) {
        logger.info("Synchronous product has %.0f states and %.0f transitions.",
                    numStates, numTrans);
      } else {
        logger.info("Synchronous product has %.0f states.", numStates);
      }

      return "State Counter has succeded";
    }

    @Override
    protected ModelAnalyzer createModelAnalyzer()
    {
      return getAnalyzer();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -8288851080599242814L;

  }

  //#########################################################################
  //# Class Constants
  private static final String TITLE = "Count States";
  private static final String OPTION_PREFIX = "analysis.statecounter";

  private static final long serialVersionUID = 1069804247073793761L;

}
