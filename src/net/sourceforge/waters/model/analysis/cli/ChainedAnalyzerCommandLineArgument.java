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

package net.sourceforge.waters.model.analysis.cli;

import java.util.ListIterator;

import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.options.ChainedAnalyzerOptionPage;


/**
 * A command line handler for <CODE>-chain</CODE> options.
 * In addition to selecting the model analyser factory loader from the
 * option argument, this handler registers to options for the appropriate
 * model analyser with the command line context, so further options are
 * passed to the chained analyser.
 *
 * @author Robi Malik
 */

public class ChainedAnalyzerCommandLineArgument
  extends EnumCommandLineArgument<ModelAnalyzerFactoryLoader>
{

  //#######################################################################
  //# Constructor
  public ChainedAnalyzerCommandLineArgument(final ChainedAnalyzerOption option)
  {
    super(option);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.options.OptionEditor<ModelAnalyzerFactoryLoader>
  @Override
  public ChainedAnalyzerOption getOption()
  {
    return (ChainedAnalyzerOption) super.getOption();
  }


  //#########################################################################
  //# Parsing
  @Override
  public void parse(final CommandLineOptionContext context,
                    final ListIterator<String> iter)
    throws AnalysisException
  {
    super.parse(context, iter);
    final ChainedAnalyzerOption option = getOption();
    final ChainedAnalyzerOptionPage page = option.getOptionPage();
    final ModelAnalyzer analyzer = option.createUnconfiguredAnalyzer();
    context.registerArguments(page, analyzer);
  }

}
