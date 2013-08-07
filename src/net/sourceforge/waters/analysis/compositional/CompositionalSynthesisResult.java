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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A result returned by the compositional synthesis algorithms
 * ({@link CompositionalSynthesizer}). In addition to the common result data,
 * it includes a collection of automata representing the synthesised modular
 * supervisor.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CompositionalSynthesisResult
  extends CompositionalAnalysisResult
  implements ProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new synthesis result representing an incomplete run.
   */
  public CompositionalSynthesisResult()
  {
    mSupervisors = new ArrayList<AutomatonProxy>();
    mMaxUnrenamedSupervisorStates = -1;
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
  public Collection<AutomatonProxy> getComputedAutomata()
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
  /**
   * Adds the given automaton to the list of synthesised supervisors.
   */
  void addUnrenamedSupervisor(final ListBufferTransitionRelation sup)
  {
    mMaxUnrenamedSupervisorStates =
      Math.max(mMaxUnrenamedSupervisorStates, sup.getNumberOfReachableStates());
  }

  void addBackRenamedSupervisor(final AutomatonProxy sup)
  {
    mSupervisors.add(sup);
  }

  void setRenamingIsUsed(final int renaming)
  {
    mRenamingIsUsed = renaming;
  }

  int getRenamingIsUsed()
  {
    return mRenamingIsUsed;
  }

  void addSynchSize(final int size)
  {
    mSynchSize = mSynchSize + size;
  }

  int getSynchSize()
  {
    return mSynchSize;
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
    final CompositionalSynthesisResult result =
      (CompositionalSynthesisResult) other;
    final Collection<AutomatonProxy> sups = result.getComputedAutomata();
    mSupervisors.addAll(sups);
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(',');
    writer.print("SynchSize");
    writer.print(',');
    writer.print("RenamingIsUsed");
    writer.print(',');
    writer.print("NumberOfSupervisors");
    writer.print(',');
    writer.print("LargestUnrenamedSupervisor");
    writer.print(',');
    writer.print("LargestRenamedSupervisor");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(",");
    writer.print(getSynchSize());
    writer.print(",");
    writer.print(getRenamingIsUsed());
    writer.print(",");
    writer.print(mSupervisors.size());
    writer.print(",");
    writer.print(mMaxUnrenamedSupervisorStates);
    writer.print(",");
    int largest = 0;
    for (final AutomatonProxy sup : mSupervisors) {
      final int currentSupSize = sup.getStates().size();
      if (currentSupSize > largest) {
        largest = currentSupSize;
      }
    }
    writer.print(largest);
  }


  //#########################################################################
  //# Specific Access
  /**
   * Completes the result by constructing and storing a product DES consisting
   * of the synthesised supervisors.
   * @param  factory  Factory used to construct the product DES.
   * @param  name     Name to be given to the product DES.
   */
  void close(final ProductDESProxyFactory factory, String name)
  {
    if (isSatisfied()) {
      final Collection<EventProxy> events =
        Candidate.getOrderedEvents(mSupervisors);
      if (name == null) {
        name = Candidate.getCompositionName("", mSupervisors);
      }
      final ProductDESProxy des =
        factory.createProductDESProxy(name, events, mSupervisors);
      setComputedProductDES(des);
    }
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxy mProductDES;
  private final List<AutomatonProxy> mSupervisors;
  private int mRenamingIsUsed;
  private int mSynchSize;
  private int mMaxUnrenamedSupervisorStates;

}
