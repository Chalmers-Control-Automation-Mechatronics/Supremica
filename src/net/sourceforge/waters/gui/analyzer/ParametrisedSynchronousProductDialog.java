//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import net.sourceforge.waters.gui.options.GUIOptionContext;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;


/**
 * The dialog to launch a synchronous product builder from the Waters
 * analyser.
 *
 * @author George Hewlett, Brandon Bassett
 */
public class ParametrisedSynchronousProductDialog
  extends ParametrisedAnalysisDialog
{

  //#######################################################################
  //# Constructor
  public ParametrisedSynchronousProductDialog(final WatersAnalyzerPanel panel)
  {
    super(panel, AnalysisOperation.SYNCHRONOUS_PRODUCT);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog
  @Override
  protected SynchronousProductBuilder getAnalyzer()
  {
    return (SynchronousProductBuilder) super.getAnalyzer();
  }

  @Override
  protected AnalysisOperationProgressDialog createAnalyzeDialog
    (final IDE ide,
     final ProductDESProxy des,
     final ModelAnalyzerFactoryLoader loader)
  {
    final SynchronousProductBuilder builder = getAnalyzer();
    builder.setModel(des);
    return new SynchronousProductPopUpDialog(ide, builder);
  }


  //#########################################################################
  //# Inner Class SynchronousProductPopUpDialog
  private class SynchronousProductPopUpDialog extends AnalysisOperationProgressDialog
  {
    //#######################################################################
    //# Constructor
    public SynchronousProductPopUpDialog(final IDE owner,
                                         final SynchronousProductBuilder builder)
    {
      super(owner, AnalysisOperation.SYNCHRONOUS_PRODUCT, builder);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
    @Override
    protected String getFailureText()
    {
      //Failure occurs when result is null, check if running for statistics
      if (!getAnalyzer().isDetailedOutputEnabled()) {
        return getSuccessText();
      } else {
        return "Synchronous Product has failed.";
      }
    }

    @Override
    protected String getSuccessText()
    {
      final AutomatonResult result = getAnalyzer().getAnalysisResult();
      final AutomatonProxy aut = result.getComputedProxy();
      if (aut == null) {
        // Statistics run
        final Logger logger = LogManager.getFormatterLogger();
        if (result.getPeakNumberOfTransitions() >= 0) {
          logger.info("Synchronous product has %.0f transitions.",
                      result.getPeakNumberOfTransitions());
        }
        if (result.getPeakNumberOfStates() >= 0) {
          logger.info("Synchronous product has %.0f states.",
                      result.getPeakNumberOfStates());
        }
        return "Successfully produced synchronous product. Printing statistics to log.";
      } else {
        final GUIOptionContext context = getContext();
        final WatersAnalyzerPanel panel = context.getWatersAnalyzerPanel();
        final AutomataTable table = panel.getAutomataTable();
        table.insertAndSelect(aut);
        return "Successfully produced synchronous product.";
      }
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -4410961155882957875L;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5945541495761539710L;

}
