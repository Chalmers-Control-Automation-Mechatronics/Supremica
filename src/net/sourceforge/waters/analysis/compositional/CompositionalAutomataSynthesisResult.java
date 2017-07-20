//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.AutomatonProxy;
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

public class CompositionalAutomataSynthesisResult
  extends CompositionalSynthesisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new synthesis result representing an incomplete run.
   */
  public CompositionalAutomataSynthesisResult(final ModelAnalyzer analyzer)
  {
    super(analyzer);
    mNumberOfSupervisors = 0;
    mSupervisors = new ArrayList<>();
    mMaxUnrenamedSupervisorStates = -1;
    mTotalUnrenamedSupervisorStates = -1;
    mMaxUnrenamedSupervisorTransitions = -1;
    mTotalUnrenamedSupervisorTransitions = -1;
    mMaxRenamedSupervisorStates = -1;
    mTotalRenamedSupervisorStates = -1;
    mMaxRenamedSupervisorTransitions = -1;
    mTotalRenamedSupervisorTransitions = -1;
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
  void addUnrenamedSupervisor(final ListBufferTransitionRelation sup,
                              final int defaultMarking)
  {
    mNumberOfSupervisors++;
    final int numberOfStates =
      getNumberOfSupervisorStates(sup, defaultMarking);
    mMaxUnrenamedSupervisorStates =
      Math.max(mMaxUnrenamedSupervisorStates, numberOfStates);
    mTotalUnrenamedSupervisorStates =
      mergeAdd(mTotalUnrenamedSupervisorStates, numberOfStates);
    final int numberOfTrans =
      getNumberOfSupervisorTransitions(sup, defaultMarking);
    mMaxUnrenamedSupervisorTransitions =
      Math.max(mMaxUnrenamedSupervisorTransitions, numberOfTrans);
    mTotalUnrenamedSupervisorTransitions =
      mergeAdd(mTotalUnrenamedSupervisorTransitions, numberOfTrans);
  }

  void addBackRenamedSupervisor(final AutomatonProxy sup)
  {
    final int numberOfStates = sup.getStates().size();
    mMaxRenamedSupervisorStates =
      Math.max(mMaxRenamedSupervisorStates, numberOfStates);
    mTotalRenamedSupervisorStates =
      mergeAdd(mTotalRenamedSupervisorStates, numberOfStates);
    final int numberOfTrans = sup.getTransitions().size();
    mMaxRenamedSupervisorTransitions =
      Math.max(mMaxRenamedSupervisorTransitions, numberOfTrans);
    mTotalRenamedSupervisorTransitions =
      mergeAdd(mTotalRenamedSupervisorTransitions, numberOfTrans);
    mSupervisors.add(sup);
  }

  void increaseNumberOfSupervisors(final int num)
  {
    mNumberOfSupervisors += num;
  }

  void setNumberOfRenamings(final int renaming)
  {
    mNumberOfRenamings = renaming;
  }

  int getNumberOfRenamings()
  {
    return mNumberOfRenamings;
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
    final CompositionalAutomataSynthesisResult result =
      (CompositionalAutomataSynthesisResult) other;
    final Collection<AutomatonProxy> sups = result.getComputedAutomata();
    mNumberOfSupervisors =
      mergeAdd(mNumberOfSupervisors, result.mNumberOfSupervisors);
    mSupervisors.addAll(sups);
    mNumberOfRenamings = mergeAdd(mNumberOfRenamings, result.mNumberOfRenamings);
    mMaxUnrenamedSupervisorStates = Math.max(mMaxUnrenamedSupervisorStates,
                                             result.mMaxUnrenamedSupervisorStates);
    mTotalUnrenamedSupervisorStates = mergeAdd(mTotalUnrenamedSupervisorStates,
                                               result.mTotalUnrenamedSupervisorStates);
    mMaxUnrenamedSupervisorTransitions = Math.max(mMaxUnrenamedSupervisorTransitions,
                                                  result.mMaxUnrenamedSupervisorTransitions);
    mTotalUnrenamedSupervisorTransitions = mergeAdd(mTotalUnrenamedSupervisorTransitions,
                                                    result.mTotalUnrenamedSupervisorTransitions);
    mMaxRenamedSupervisorStates = Math.max(mMaxRenamedSupervisorStates,
                                           result.mMaxRenamedSupervisorStates);
    mTotalRenamedSupervisorStates = mergeAdd(mTotalRenamedSupervisorStates,
                                             result.mTotalRenamedSupervisorStates);
    mMaxRenamedSupervisorTransitions = Math.max(mMaxRenamedSupervisorTransitions,
                                                result.mMaxRenamedSupervisorTransitions);
    mTotalRenamedSupervisorTransitions = mergeAdd(mTotalRenamedSupervisorTransitions,
                                                  result.mTotalRenamedSupervisorTransitions);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.print("Number of renamings: ");
    writer.println(mNumberOfRenamings);
    writer.print("Number of supervisors: ");
    writer.println(mNumberOfSupervisors);
    if (mMaxUnrenamedSupervisorStates >= 0) {
      writer.print("Maximum number of unrenamed supervisor states: ");
      writer.println(mMaxUnrenamedSupervisorStates);
      writer.print("Total number of unrenamed supervisor states: ");
      writer.println(mTotalUnrenamedSupervisorStates);
      writer.print("Maximum number of unrenamed supervisor transitions: ");
      writer.println(mMaxUnrenamedSupervisorTransitions);
      writer.print("Total number of unrenamed supervisor transitions: ");
      writer.println(mTotalUnrenamedSupervisorTransitions);
      writer.print("Memory estimate for unrenamed supervisor: ");
      writer.print(getMemoryEstimate(mTotalUnrenamedSupervisorStates,
                                     mTotalUnrenamedSupervisorTransitions));
      writer.println(" bytes");
    }
    if (mMaxRenamedSupervisorStates >= 0) {
      writer.print("Maximum number of renamed supervisor states: ");
      writer.println(mMaxRenamedSupervisorStates);
      writer.print("Total number of renamed supervisor states: ");
      writer.println(mTotalRenamedSupervisorStates);
      writer.print("Maximum number of renamed supervisor transitions: ");
      writer.println(mMaxRenamedSupervisorTransitions);
      writer.print("Total number of renamed supervisor transitions: ");
      writer.println(mTotalRenamedSupervisorTransitions);
      writer.print("Memory estimate for renamed supervisor: ");
      writer.print(getMemoryEstimate(mTotalRenamedSupervisorStates,
                                     mTotalRenamedSupervisorTransitions));
      writer.println(" bytes");
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(',');
    writer.print("NumberOfRenamings");
    writer.print(',');
    writer.print("NumberOfSupervisors");
    writer.print(',');
    writer.print("MaxUnrenamedSupervisorStates");
    writer.print(',');
    writer.print("TotalUnrenamedSupervisorStates");
    writer.print(',');
    writer.print("MaxUnrenamedSupervisorTransitions");
    writer.print(',');
    writer.print("TotalUnrenamedSupervisorTransitions");
    writer.print(',');
    writer.print("UnrenamedSupervisorMemoryEstimate");
    writer.print(',');
    writer.print("MaxRenamedSupervisorStates");
    writer.print(',');
    writer.print("TotalRenamedSupervisorStates");
    writer.print(',');
    writer.print("MaxRenamedSupervisorTransitions");
    writer.print(',');
    writer.print("TotalRenamedSupervisorTransitions");
    writer.print(',');
    writer.print("RenamedSupervisorMemoryEstimate");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(",");
    writer.print(getNumberOfRenamings());
    writer.print(",");
    writer.print(mNumberOfSupervisors);
    writer.print(",");
    writer.print(mMaxUnrenamedSupervisorStates);
    writer.print(",");
    writer.print(mTotalUnrenamedSupervisorStates);
    writer.print(",");
    writer.print(mMaxUnrenamedSupervisorTransitions);
    writer.print(",");
    writer.print(mTotalUnrenamedSupervisorTransitions);
    writer.print(",");
    writer.print(getMemoryEstimate(mTotalUnrenamedSupervisorStates,
                                   mTotalUnrenamedSupervisorTransitions));
    writer.print(",");
    writer.print(mMaxRenamedSupervisorStates);
    writer.print(",");
    writer.print(mTotalRenamedSupervisorStates);
    writer.print(",");
    writer.print(mMaxRenamedSupervisorTransitions);
    writer.print(",");
    writer.print(mTotalRenamedSupervisorTransitions);
    writer.print(",");
    writer.print(getMemoryEstimate(mTotalRenamedSupervisorStates,
                                   mTotalRenamedSupervisorTransitions));
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
  //# Auxiliary Methods
  private static int getNumberOfSupervisorStates
    (final ListBufferTransitionRelation sup, final int defaultMarking)
  {
    if (defaultMarking < 0 || !sup.isPropositionUsed(defaultMarking)) {
      return sup.getNumberOfReachableStates();
    } else {
      int numStates = 0;
      for (int s = 0; s < sup.getNumberOfStates(); s++) {
        if (sup.isReachable(s) && !sup.isDeadlockState(s, defaultMarking)) {
          numStates++;
        }
      }
      return numStates;
    }
  }

  private static int getNumberOfSupervisorTransitions
    (final ListBufferTransitionRelation sup, final int defaultMarking)
  {
    if (defaultMarking < 0 || !sup.isPropositionUsed(defaultMarking)) {
      return sup.getNumberOfTransitions();
    } else {
      int numTrans = 0;
      final TransitionIterator iter = sup.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int t = iter.getCurrentTargetState();
        if (!sup.isDeadlockState(t, defaultMarking)) {
          numTrans++;
        }
      }
      return numTrans;
    }
  }

  private static int getMemoryEstimate(final int states, final int transitions)
  {
    return states + 2*transitions;
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxy mProductDES;
  private int mNumberOfSupervisors;
  private final List<AutomatonProxy> mSupervisors;
  private int mNumberOfRenamings;
  private int mMaxUnrenamedSupervisorStates;
  private int mTotalUnrenamedSupervisorStates;
  private int mMaxUnrenamedSupervisorTransitions;
  private int mTotalUnrenamedSupervisorTransitions;
  private int mMaxRenamedSupervisorStates;
  private int mTotalRenamedSupervisorStates;
  private int mMaxRenamedSupervisorTransitions;
  private int mTotalRenamedSupervisorTransitions;

}
