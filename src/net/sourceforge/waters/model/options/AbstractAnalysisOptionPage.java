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

package net.sourceforge.waters.model.options;

import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;


/**
 * <P>Abstract base class for options page to configure an analysis algorithm.</P>
 *
 * <P>The analysis option page is associated with a given analysis task,
 * e.g., conflict check. Subclassed further for main algorithm selection
 * ({@link AnalysisOptionPage}) an chained analyser selection
 * ({@link ChainedAnalyzerOptionPage}).</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractAnalysisOptionPage
  extends SelectorLeafOptionPage<ModelAnalyzerFactoryLoader>
{

  //#########################################################################
  //# Constructors
  public AbstractAnalysisOptionPage(final AnalysisOperation operation)
  {
    this(operation,
         operation.getOptionPagePrefix(),
         operation.getShortWindowTitle());
  }

  public AbstractAnalysisOptionPage(final AnalysisOperation operation,
                                    final String prefix,
                                    final String title)
  {
    super(prefix, title);
    mOperation = operation;
  }


  //#########################################################################
  //# Simple Access
  public AnalysisOperation getAnalysisOperation()
  {
    return mOperation;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.LeafOptionPage
  @Override
  public String getShortDescription()
  {
    return mOperation.getShortWindowTitle();
  }


  //#########################################################################
  //# Data Members
  private final AnalysisOperation mOperation;

}
