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

package net.sourceforge.waters.cpp.analysis;

import java.io.PrintWriter;

import net.sourceforge.waters.analysis.monolithic.MonolithicVerificationResult;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A result record that can returned by a native verification algorithm.
 *
 * @author Robi Malik
 */

public class NativeVerificationResult
  extends MonolithicVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public NativeVerificationResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public NativeVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mTarjanComponentCount = -1;
    mTarjanControlStackHeight = -1;
    mTarjanComponentStackHeight = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the number of strongly connected components detected by
   * Tarjan's algorithm.
   */
  public int getTarjanComponentCount()
  {
    return mTarjanComponentCount;
  }

  /**
   * Gets the maximum height of the control stack when Tarjan's algorithm
   * is used.
   */
  public int getTarjanControlStackHeight()
  {
    return mTarjanControlStackHeight;
  }

  /**
   * Gets the maximum height of the component stack when Tarjan's algorithm
   * is used.
   */
  public int getTarjanComponentStackHeight()
  {
    return mTarjanComponentStackHeight;
  }


  //#########################################################################
  //# Providing Statistics
  public void setTarjanComponentCount(final int value)
  {
    mTarjanComponentCount = value;
  }

  public void setTarjanControlStackHeight(final int value)
  {
    mTarjanControlStackHeight = value;
  }

  public void setTarjanComponentStackHeight(final int value)
  {
    mTarjanComponentStackHeight = value;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final NativeVerificationResult result = (NativeVerificationResult) other;
    mTarjanComponentCount =
      mergeAdd(mTarjanComponentCount, result.mTarjanComponentCount);
    mTarjanControlStackHeight =
      Math.max(mTarjanControlStackHeight, result.mTarjanControlStackHeight);
    mTarjanComponentStackHeight =
      Math.max(mTarjanComponentStackHeight, result.mTarjanComponentStackHeight);
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mTarjanComponentCount >= 0) {
      writer.print("Number of strongly connected components: ");
      writer.println(mTarjanComponentCount);
    }
    if (mTarjanControlStackHeight >= 0) {
      writer.print("Maximum height of Tarjan control stack: ");
      writer.println(mTarjanControlStackHeight);
    }
    if (mTarjanComponentStackHeight >= 0) {
      writer.print("Maximum height of Tarjan component stack: ");
      writer.println(mTarjanComponentStackHeight);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",TarjanSCCs");
    writer.print(",TarjanControlStackHeight");
    writer.print(",TarjanComponentStackHeight");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mTarjanComponentCount);
    writer.print(',');
    writer.print(mTarjanControlStackHeight);
    writer.print(',');
    writer.print(mTarjanComponentStackHeight);
  }


  //#########################################################################
  //# Data Members
  private int mTarjanComponentCount;
  private int mTarjanControlStackHeight;
  private int mTarjanComponentStackHeight;

}
