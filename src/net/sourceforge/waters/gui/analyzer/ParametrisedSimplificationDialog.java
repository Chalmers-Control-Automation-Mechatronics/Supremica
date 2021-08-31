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

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.gui.dialog.AnalysisProgressDialog;
import net.sourceforge.waters.gui.options.GUIOptionContext;
import net.sourceforge.waters.gui.options.ParametrisedInvocationDialog;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.SimplifierOptionPage;
import net.sourceforge.waters.model.options.WatersOptionPages;

import org.supremica.gui.ide.IDE;


/**
 * Abstract class that auto-generates a dialog to configure and
 * invoke a transition relation simplifier from the Waters Analyser.
 *
 * @author Benjamin Wheeler, Robi Malik
 */

public class ParametrisedSimplificationDialog
  extends ParametrisedInvocationDialog<AutomatonSimplifierCreator>
{
  //#########################################################################
  //# Constructor
  public ParametrisedSimplificationDialog(final WatersAnalyzerPanel panel,
                                          final AutomatonProxy aut)
  {
    super(panel, WatersOptionPages.SIMPLIFICATION);
    setTitle("Simplifying " + aut.getName() + " ...");
    mAutomaton = aut;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.gui.options.ParametrisedInvocationDialog
  @Override
  protected SimplifierOptionPage getOptionPage()
  {
    return WatersOptionPages.SIMPLIFICATION;
  }

  @Override
  protected AutomatonBuilder createModelAnalyzer
    (final AutomatonSimplifierCreator creator)
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final SimplifierOptionPage page = getOptionPage();
    for (final Option<?> option : creator.getOptions(page)) {
      creator.setOption(option);
    }
    return creator.createBuilder(factory);
  }

  @Override
  protected void configureModelAnalyzer(final ModelAnalyzer analyzer)
  {
    final AutomatonBuilder builder = (AutomatonBuilder) analyzer;
    builder.setModel(mAutomaton);
    final SimplifierOptionPage page = getOptionPage();
    final BooleanOption keepOriginalOption = (BooleanOption) page.get
      (AutomatonSimplifierFactory.
       OPTION_AutomatonSimplifierFactory_KeepOriginal);
    mKeepingOriginal = keepOriginalOption.getBooleanValue();
    if (mKeepingOriginal) {
      final GUIOptionContext context = getContext();
      final WatersAnalyzerPanel panel = context.getWatersAnalyzerPanel();
      final AutomataTableModel model = panel.getAutomataTableModel();
      final String newName = model.getUniqueAutomatonName(mAutomaton.getName());
      builder.setOutputName(newName);
    } else {
      builder.setOutputName(mAutomaton.getName());
    }
    super.configureModelAnalyzer(builder);
  }

  @Override
  protected SimplificationProgressDialog createAnalyzeDialog
    (final IDE ide,
     final ProductDESProxy des,
     final AutomatonSimplifierCreator creator)
  {
    final ModelAnalyzer analyzer = getAnalyzer();
    return new SimplificationProgressDialog(ide, analyzer, creator.getName());
  }


  //#########################################################################
  //# Inner Class SimplificationProgressDialog
  private class SimplificationProgressDialog extends AnalysisProgressDialog
  {
    //#######################################################################
    //# Constructor
    protected SimplificationProgressDialog(final IDE owner,
                                           final ModelAnalyzer analyzer,
                                           final String title)
    {
      super(owner, analyzer, title);
      mTitle = title;
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.AnalysisProgressDialog
     @Override
    protected String getWindowTitle()
    {
      return mTitle;
    }

    @Override
    public void succeed()
    {
      final GUIOptionContext context = getContext();
      final WatersAnalyzerPanel panel = context.getWatersAnalyzerPanel();
      final AutomataTable table = panel.getAutomataTable();
      final AutomataTableModel model = panel.getAutomataTableModel();
      final AutomatonBuilder builder = (AutomatonBuilder) getAnalyzer();
      final AutomatonProxy result = builder.getComputedAutomaton();
      final List<AutomatonProxy> list = Collections.singletonList(result);
      if (mKeepingOriginal) {
        model.insertRow(result);
        table.clearSelection();
        table.addToSelection(list);
      } else {
        model.replaceAutomaton(mAutomaton, result);
      }
      table.scrollToVisible(list);
      dispose();
    }

    //#######################################################################
    //# Data Members
    private final String mTitle;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6095616288815919868L;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private boolean mKeepingOriginal;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
