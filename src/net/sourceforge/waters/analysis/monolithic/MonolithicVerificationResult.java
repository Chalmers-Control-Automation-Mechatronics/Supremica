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

package net.sourceforge.waters.analysis.monolithic;

import java.io.PrintWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A result record that can returned by a monolithic verification algorithm.
 *
 * @author Robi Malik
 */

public class MonolithicVerificationResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public MonolithicVerificationResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public MonolithicVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mEncodingSize = -1;
    mNumExploredTransitions = 0;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the number of bits used to encode state tuples.
   */
  public double getEncodingSize()
  {
    return mEncodingSize;
  }

  /**
   * Gets the total number of transitions explored during analysis.
   * This is a runtime estimate. If transitions are processed more than
   * once, each time is counted separately.
   */
  public double getNumberOfExploredTransitions()
  {
    return mNumExploredTransitions;
  }


  //#########################################################################
  //# Providing Statistics
  public void setEncodingSize(final int value)
  {
    mEncodingSize = value;
  }

  public void setNumberOfExploredTransitions(final double value)
  {
    mNumExploredTransitions = value;
  }

  public void addExploredTransition()
  {
    mNumExploredTransitions++;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final MonolithicVerificationResult result = (MonolithicVerificationResult) other;
    mEncodingSize = Math.max(mEncodingSize, result.mEncodingSize);
    mNumExploredTransitions =
      mergeAdd(mNumExploredTransitions, result.mNumExploredTransitions);
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    if (mEncodingSize >= 0) {
      writer.print("Peak encoding size: ");
      writer.print(mEncodingSize);
      writer.println(" bits");
    }
    if (mNumExploredTransitions >= 0.0) {
      formatter.format("Total number of transitions explored: %.0f\n",
                       mNumExploredTransitions);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",EncodingSize");
    writer.print(",ExploredTrans");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mEncodingSize);
    writer.print(',');
    writer.print(mNumExploredTransitions);
  }


  //#########################################################################
  //# Data Members
  private int mEncodingSize;
  private double mNumExploredTransitions;

}
