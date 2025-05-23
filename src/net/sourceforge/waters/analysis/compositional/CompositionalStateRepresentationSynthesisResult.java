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

package net.sourceforge.waters.analysis.compositional;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A result returned by the compositional synthesis algorithms
 * ({@link CompositionalAutomataSynthesizer}). In addition to the common result data,
 * it includes a collection of automata representing the synthesised modular
 * supervisor.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CompositionalStateRepresentationSynthesisResult
  extends CompositionalSynthesisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new synthesis result representing an incomplete run.
   */
  public CompositionalStateRepresentationSynthesisResult
    (final AbstractCompositionalModelAnalyzer analyzer)
  {
    super(analyzer);
    mSupervisors = new ArrayList<SynthesisStateSpace>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProxyResult
  @Override
  public ProductDESProxy getComputedProxy()
  {
    return mProductDES;
  }

  @Override
  public void setComputedProxy(final ProductDESProxy des)
  {
    setSatisfied(des != null);
    mProductDES = des;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESResult
  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  @Override
  public Collection<SynthesisStateSpace> getComputedAutomata()
  {
    return mSupervisors;
  }

  @Override
  public void setComputedProductDES(final ProductDESProxy des)
  {
    setComputedProxy(des);
  }


  //#########################################################################
  //# Specific Access
  void addSynthesisStateSpace(final SynthesisStateSpace sup)
  {
    mSupervisors.add(sup);
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.DefaultAnalysisResult
  @Override
  public void setSatisfied(final boolean sat)
  {
    super.setSatisfied(sat);
    if (!sat) {
      mSupervisors.clear();
    }
  }

  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final CompositionalStateRepresentationSynthesisResult result =
      (CompositionalStateRepresentationSynthesisResult) other;
    final Collection<SynthesisStateSpace> sups = result.getComputedAutomata();
    mSupervisors.addAll(sups);
  }

  @Override
  protected void printPart1(final PrintWriter writer)
  {
    super.printPart1(writer);
    writer.print("Number of maps: ");
    writer.println(getNumberOfMaps());
    writer.print("Memory estimate for supervisor maps: ");
    writer.print(getMemoryEstimate());
    writer.println(" bytes");
  }

  @Override
  protected void printCSVHorizontalHeadingsPart1(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadingsPart1(writer);
    writer.print(",NumberOfMaps");
    writer.print(",MemoryEstimate");
  }

  @Override
  protected void printCSVHorizontalPart1(final PrintWriter writer)
  {
    super.printCSVHorizontalPart1(writer);
    writer.print(',');
    writer.print(getNumberOfMaps());
    writer.print(',');
    writer.print(getMemoryEstimate());
  }


  //#########################################################################
  //# Specific Access
  /**
   * Completes the result by constructing and storing a product DES consisting
   * of the synthesised supervisors.
   * @param  factory  Factory used to construct the product DES.
   * @param  name     Name to be given to the product DES.
   */
  void close(final ProductDESProxyFactory factory,
             final Collection<EventProxy> events,
             String name)
  {
    if (isSatisfied()) {
      if (name == null) {
        name = Candidate.getCompositionName("", mSupervisors);
      }
      final ProductDESProxy des =
        factory.createProductDESProxy(name, events, mSupervisors);
      setComputedProductDES(des);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getMemoryEstimate()
  {
    int mem = 0;
    for (final SynthesisStateSpace sup : mSupervisors) {
      mem += sup.getMemoryEstimate();
    }
    return mem;
  }

  private int getNumberOfMaps()
  {
    int maps = 0;
    for (final SynthesisStateSpace sup : mSupervisors) {
      maps += sup.getNumberOfMaps();
    }
    return maps;
  }

  //#########################################################################
  //# Data Members
  private ProductDESProxy mProductDES;
  private final List<SynthesisStateSpace> mSupervisors;

}
