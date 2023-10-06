//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.gui.dialog.AnalysisOperationProgressDialog;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.StateCounter;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;


/**
 * The dialog to launch a state counter from the Waters analyser.
 *
 * @author Brandon Bassett
 */
public class ParametrisedStateCounterDialog extends ParametrisedAnalysisDialog
{

  //#######################################################################
  //# Constructor
  public ParametrisedStateCounterDialog(final WatersAnalyzerPanel panel)
  {
    super(panel, AnalysisOperation.STATE_COUNT);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog
  @Override
  protected StateCounter getAnalyzer()
  {
    return (StateCounter) super.getAnalyzer();
  }

  @Override
  protected AnalysisOperationProgressDialog createAnalyzeDialog
    (final IDE ide,
     final ProductDESProxy des,
     final ModelAnalyzerFactoryLoader loader)
  {
    final StateCounter counter = getAnalyzer();
    counter.setModel(des);
    return new StateCounterPopUpDialog(ide, counter);
  }


  //#########################################################################
  //# Inner Class StateCounterPopUpDialog
  private class StateCounterPopUpDialog extends AnalysisOperationProgressDialog
  {
    //#######################################################################
    //# Constructor
    public StateCounterPopUpDialog(final IDE owner,
                                   final StateCounter counter)
    {
      super(owner, AnalysisOperation.STATE_COUNT, counter);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
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

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -8288851080599242814L;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1069804247073793761L;

}
