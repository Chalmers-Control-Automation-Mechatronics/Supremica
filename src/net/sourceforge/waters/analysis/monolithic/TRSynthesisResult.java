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
import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.DefaultProductDESResult;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A synthesis result record returned by monolithic synthesis algorithms.
 *
 * @author Robi Malik
 */
//TODO Merge this class with MonolithicSynthesisResult
public class TRSynthesisResult
  extends MonolithicAnalysisResult
  implements ProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a synchronous product result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public TRSynthesisResult(final SupervisorSynthesizer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a synchronous product result representing an incomplete run.
   * @param  clazz    The class of the model analyser creating this result.
   */
  public TRSynthesisResult(final Class<?> clazz)
  {
    super(clazz);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESResult
  @Override
  public ProductDESProxy getComputedProxy()
  {
    return mComputedDES;
  }

  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  @Override
  public Collection<AutomatonProxy> getComputedAutomata()
  {
    final ProductDESProxy des = getComputedProductDES();
    if (des == null) {
      return null;
    } else {
      return des.getAutomata();
    }
  }

  @Override
  public void setComputedProxy(final ProductDESProxy des)
  {
    mComputedDES = des;
    setSatisfied(des != null);
  }

  @Override
  public void setComputedProductDES(final ProductDESProxy des)
  {
    setComputedProxy(des);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public String getResultDescription()
  {
    return "supervisor";
  }

  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    mComputedDES = null;
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    DefaultProductDESResult.printStats(writer, this);
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    DefaultProductDESResult.printCSVHorizontalHeadings(writer, this);
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    DefaultProductDESResult.printCSVHorizontal(writer, this);
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxy mComputedDES;

}
