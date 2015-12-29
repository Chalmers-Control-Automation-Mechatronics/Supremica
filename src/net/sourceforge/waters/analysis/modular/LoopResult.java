//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


public class LoopResult extends DefaultVerificationResult
{

  LoopResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  LoopResult(final Class<?> clazz)
  {
    super(clazz);
    mPeakNumberOfAutomata = -1;
    mNumberOfCompositions = -1;
  }

  /**
   * Gets the maximum number of automata which are composed by the analysis.
   * The peak number of automata should identify the largest number of
   * automata in an automata group. For monolithic algorithms, it will
   * be equal to the total number of automata, but for compositional algorithms
   * it may be different
   *
   * @return The peak number of states, or <CODE>-1</CODE> if unknown
   */

  public int getPeakNumberOfAutomata()
  {
    return mPeakNumberOfAutomata;
  }

  /**
   * Gets the number of Compositions used in the entire model. For monolithic
   * algorithms, this will be 0. For compositional algorithms, it will always
   * be less than the number of automata in the model, and it will always be
   * equal to or greater than one less than the peak number of automata
   *
   * @return The number of compositions, or <CODE>-1</CODE> if unknown
   */

  public int getNumberOfCompositions()
  {
    return mNumberOfCompositions;
  }

  /**
   * Specifies a value for both the peak number of automata and the total
   * number of automata constructed by the analysis.
   */
  @Override
  public void setNumberOfAutomata(final int numaut)
  {
    setTotalNumberOfAutomata(numaut);
    setPeakNumberOfAutomata(numaut);
  }

  /**
   * Specifies a value for the total number of automata constructed by the
   * analysis.
   */
  public void setTotalNumberOfAutomata(final int numaut)
  {
    super.setNumberOfAutomata(numaut);
  }

  /**
   * Specifies a value for the peak number of automata constructed by the
   * analysis.
   */
  public void setPeakNumberOfAutomata(final int numaut)
  {
    mPeakNumberOfAutomata = numaut;
  }

  /**
   * Specifies a value for the peak number of automata composed by the
   * analysis.
   */
  public void setNumberOfCompositions(final int numcomp)
  {
    mNumberOfCompositions = numcomp;
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mPeakNumberOfAutomata >= 0) {
      writer.println("Peak number of automata: " + mPeakNumberOfAutomata);
    }
    if (mNumberOfCompositions >= 0) {
      writer.println("Number of Compositions: " + mNumberOfCompositions);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print("," + mPeakNumberOfAutomata);
    writer.print("," + mNumberOfCompositions);
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Peak aut");
    writer.print(",Compositions");
  }


  private int mPeakNumberOfAutomata;
  private int mNumberOfCompositions;

}
