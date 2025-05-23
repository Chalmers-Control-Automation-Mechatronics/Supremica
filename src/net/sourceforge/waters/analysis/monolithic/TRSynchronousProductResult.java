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

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * A synchronous product result record returned by transition-relation
 * based synchronous product builders.
 *
 * @see AbstractTRSynchronousProductBuilder
 * @author Robi Malik
 */

public class TRSynchronousProductResult
  extends MonolithicAnalysisResult
  implements SynchronousProductResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a synchronous product result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public TRSynchronousProductResult(final SynchronousProductBuilder analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a synchronous product result representing an incomplete run.
   * @param  clazz    The class of the model analyser creating this result.
   */
  public TRSynchronousProductResult(final Class<?> clazz)
  {
    super(clazz);
    mReducedDiamondsCount = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the number of states that were reduced by means of reducing
   * synchronous composition.
   * @see TRReducingSynchronousProductBuilder
   */
  public int getReducedDiamondsCount()
  {
    return mReducedDiamondsCount;
  }


  //#########################################################################
  //# Providing Statistics
  public void setReducedDiamondsCount(final int count)
  {
    mReducedDiamondsCount = count;
  }

  public void addReducedDiamond()
  {
    mReducedDiamondsCount++;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonResult
  @Override
  public AutomatonProxy getComputedProxy()
  {
    return mComputedAutomaton;
  }

  @Override
  public TRAutomatonProxy getComputedAutomaton()
  {
    return mComputedAutomaton;
  }

  @Override
  public void setComputedProxy(final AutomatonProxy aut)
  {
    setSatisfied(aut != null);
    mComputedAutomaton = (TRAutomatonProxy) aut;
  }

  @Override
  public void setComputedAutomaton(final AutomatonProxy aut)
  {
    setComputedProxy(aut);
    setSatisfied(aut != null);
  }

  @Override
  public String getResultDescription()
  {
    return "synchronous product";
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.SynchronousProductResult
  @Override
  public TRSynchronousProductStateMap getStateMap()
  {
    return mStateMap;
  }

  @Override
  public void setStateMap(final SynchronousProductStateMap map)
  {
    mStateMap = (TRSynchronousProductStateMap) map;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final TRSynchronousProductResult result =
      (TRSynchronousProductResult) other;
    mReducedDiamondsCount =
      mergeAdd(mReducedDiamondsCount, result.mReducedDiamondsCount);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mReducedDiamondsCount >= 0) {
      writer.print("Number of reduced diamonds: ");
      writer.println(mReducedDiamondsCount);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",ReducedDiamonds");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mReducedDiamondsCount);
  }


  //#########################################################################
  //# Data Members
  private TRAutomatonProxy mComputedAutomaton;
  private TRSynchronousProductStateMap mStateMap;
  private int mReducedDiamondsCount;

}
