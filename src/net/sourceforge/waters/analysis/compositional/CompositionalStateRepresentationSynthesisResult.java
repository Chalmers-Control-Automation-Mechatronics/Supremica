//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesisResult
//###########################################################################
//# $Id$
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
  public CompositionalStateRepresentationSynthesisResult()
  {
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
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.print("Number of maps: ");
    writer.println(getNumberOfMaps());
    writer.print("Memory estimate for supervisor maps: ");
    writer.print(getMemoryEstimate());
    writer.println(" bytes");
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(',');
    writer.print("NumberOfMaps");
    writer.print(',');
    writer.print("MemoryEstimate");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(",");
    writer.print(getNumberOfMaps());
    writer.print(",");
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
