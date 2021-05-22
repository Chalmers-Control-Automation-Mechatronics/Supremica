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

import java.util.Collection;

import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.options.GUIOptionContext;
import net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.supremica.gui.ide.IDE;


/**
 * The dialog to launch a supervisor synthesiser from the Waters analyser.
 *
 * @author Brandon Bassett, Robi Malik
 */

public class ParametrisedSynthesisDialog extends ParametrisedAnalysisDialog
{

  //#########################################################################
  //# Constructor
  public ParametrisedSynthesisDialog(final WatersAnalyzerPanel panel)
  {
    super(panel, AnalysisOperation.SYNTHESIS);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog
  @Override
  protected SupervisorSynthesizer getAnalyzer()
  {
    return (SupervisorSynthesizer) super.getAnalyzer();
  }

  @Override
  protected WatersAnalyzeDialog createAnalyzeDialog
    (final IDE ide, final ProductDESProxy des)
  {
    final SupervisorSynthesizer synthesizer = getAnalyzer();
    synthesizer.setModel(des);
    return new SynthesisPopUpDialog(ide, synthesizer);
  }


  //#########################################################################
  //# Inner Class SynthesisPopUpDialog
  private class SynthesisPopUpDialog extends WatersAnalyzeDialog
  {
    //#######################################################################
    //# Constructor
    public SynthesisPopUpDialog(final IDE owner,
                                final SupervisorSynthesizer synthesizer)
    {
      super(owner, AnalysisOperation.SYNTHESIS, synthesizer);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
    @Override
    public void succeed()
    {
      super.succeed();
      final ProductDESResult result = getAnalyzer().getAnalysisResult();
      final Collection<? extends AutomatonProxy> supervisors =
        result.getComputedAutomata();
      if (supervisors != null) {
        final GUIOptionContext context = getContext();
        final WatersAnalyzerPanel panel = context.getWatersAnalyzerPanel();
        final AutomataTable table = panel.getAutomataTable();
        table.insertAndSelect(supervisors);
      }
    }

    @Override
    protected String getFailureText()
    {
      return "Synthesis failed. There is no solution to the control problem.";
    }

    @Override
    protected String getSuccessText()
    {
      final ProductDESResult result =  getAnalyzer().getAnalysisResult();
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

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6159733639861131531L;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -622825450495392984L;

}
