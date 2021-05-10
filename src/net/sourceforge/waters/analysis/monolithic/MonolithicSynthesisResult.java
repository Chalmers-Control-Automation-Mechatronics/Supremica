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

package net.sourceforge.waters.analysis.monolithic;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.DefaultProductDESResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A synthesis product result record returned by the monolithic synthesis
 * algorithm. This is a product DES result augmented with simplifier
 * statistics obtained from supervisor reduction or supervisor localisation.
 *
 * @author Robi Malik
 */

public class MonolithicSynthesisResult
  extends DefaultProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a synthesis result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public MonolithicSynthesisResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a synthesis result representing an incomplete run.
   * @param  clazz    The class of the model analyser creating this result.
   */
  public MonolithicSynthesisResult(final Class<?> clazz)
  {
    super(clazz);
    mSimplifierStatistics = null;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the statistics for the individual abstraction rules used
   * during supervisor reduction or supervisor localisation.
   * @return List of statistics records, or <CODE>null</CODE>.
   */
  public List<TRSimplifierStatistics> getSimplifierStatistics()
  {
    return mSimplifierStatistics;
  }


  //#########################################################################
  //# Providing Statistics
  public void addSimplifierStatistics
    (final TransitionRelationSimplifier simplifier)
  {
    if (mSimplifierStatistics == null) {
      mSimplifierStatistics = new LinkedList<TRSimplifierStatistics>();
    }
    simplifier.collectStatistics(mSimplifierStatistics);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final MonolithicSynthesisResult result =
      (MonolithicSynthesisResult) other;
    if (mSimplifierStatistics != null && result.mSimplifierStatistics != null) {
      final Iterator<TRSimplifierStatistics> iter1 =
        mSimplifierStatistics.iterator();
      final Iterator<TRSimplifierStatistics> iter2 =
        result.mSimplifierStatistics.iterator();
      while (iter1.hasNext() && iter2.hasNext()) {
        final TRSimplifierStatistics stats1 = iter1.next();
        final TRSimplifierStatistics stats2 = iter2.next();
        stats1.merge(stats2);
      }
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        writer.println("--------------------------------------------------");
        ruleStats.print(writer);
      }
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontalHeadings(writer);
      }
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontal(writer);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private List<TRSimplifierStatistics> mSimplifierStatistics;

}
