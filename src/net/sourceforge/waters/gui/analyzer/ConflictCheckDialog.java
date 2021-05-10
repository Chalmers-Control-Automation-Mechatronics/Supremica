//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import net.sourceforge.waters.analysis.options.AnalysisOptionPage;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.dialog.WatersVerificationDialog;
import net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.supremica.gui.ide.IDE;

/**
 * The dialog to launch a conflict check from the Waters analyser.
 *
 * @author Brandon Bassett
 */
public class ConflictCheckDialog extends ParametrisedAnalysisDialog
{

  //#########################################################################
  //# Constructor
  public ConflictCheckDialog(final WatersAnalyzerPanel panel)
  {
    super(panel);
    setTitle(TITLE);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog
  @Override
  protected WatersAnalyzeDialog createAnalyzeDialog(final IDE ide,
                                                    final ProductDESProxy des)
  {
    return new ConflictCheckPopUpDialog(ide, des);
  }

  @Override
  protected AnalysisOptionPage getOptionPage()
  {
    return OptionPage.ConflictCheck;
  }


  //#########################################################################
  //# Inner Class ConflictCheckPopUpDialog
  private class ConflictCheckPopUpDialog extends WatersVerificationDialog
  {
    //#######################################################################
    //# Constructor
    public ConflictCheckPopUpDialog(final IDE owner,
                                    final ProductDESProxy des)
    {
      super(owner, des);
    }

    @Override
    protected String getAnalysisName()
    {
      return TITLE;
    }

    @Override
    protected String getFailureDescription()
    {
      return "is blocking";
    }

    @Override
    protected String getSuccessDescription()
    {
      return "is nonblocking";
    }

    @Override
    protected ModelAnalyzer createAndConfigureModelAnalyzer()
    {
      return getAnalyzer();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6159733639861131531L;
  }


  //#########################################################################
  //# Class Constants
  private static final String TITLE = "Conflict Check";

  private static final long serialVersionUID = -4771975182146634793L;
}
