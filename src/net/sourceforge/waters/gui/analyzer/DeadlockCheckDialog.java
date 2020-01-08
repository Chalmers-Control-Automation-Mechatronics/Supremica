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

import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.gui.dialog.WatersVerificationDialog;
import net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.DeadlockChecker;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.gui.ide.IDE;

/**
 * @author Brandon Bassett
 */
public class DeadlockCheckDialog extends ParametrisedAnalysisDialog
{

  //#########################################################################
  //# Constructor
  public DeadlockCheckDialog(final WatersAnalyzerPanel panel)
  {
    super(panel);
    setTitle(TITLE);
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.dialog.AbstractAnalysisDialog
  @Override
  protected DeadlockChecker createAnalyzer(final ModelAnalyzerFactory factory)
    throws AnalysisConfigurationException
  {
    final ProductDESProxyFactory desFactory = getProductDESProxyFactory();
    return factory.createDeadlockChecker(desFactory);
  }

  @Override
  protected WatersAnalyzeDialog createAnalyzeDialog(final IDE ide,
                                                    final ProductDESProxy des)
  {
    return new DeadlockCheckPopUpDialog(ide, des);
  }

  @Override
  protected DeadlockChecker getAnalyzer()
  {
    return (DeadlockChecker) super.getAnalyzer();
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.ParametrisedAnalysisDialog
  @Override
  protected OptionPage getOptionMap()
  {
    return OptionPage.DeadlockCheck;
  }


  //#########################################################################
  //# Inner Class DeadlockCheckPopUpDialog
  private class DeadlockCheckPopUpDialog extends WatersVerificationDialog
  {
    //#######################################################################
    //# Constructor
    public DeadlockCheckPopUpDialog(final IDE owner,
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
      return "has a deadlock";
    }

    @Override
    protected String getSuccessDescription()
    {
      return "is deadlock-free";
    }

    @Override
    protected ModelAnalyzer createModelAnalyzer()
    {
      return getAnalyzer();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6159733639861131531L;
  }


  //#########################################################################
  //# Class Constants
  private static final String TITLE = "Deadlock Check";

  private static final long serialVersionUID = 7587116260533051091L;

}
