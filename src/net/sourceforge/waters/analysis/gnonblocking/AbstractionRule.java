//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AbstractionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;


/**
 * @author Rachel Francis
 */

abstract class AbstractionRule
{
  // #######################################################################
  // # Constructor
  AbstractionRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  AbstractionRule(final ProductDESProxyFactory factory,
                  final Collection<EventProxy> propositions)
  {
    mFactory = factory;
    mPropositions = propositions;
  }

  // #######################################################################
  // # Configuration
  ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }

  void setPropositions(final Collection<EventProxy> props)
  {
    mPropositions = props;
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Returns a statistics class containing the statistics for the performance of
   * this rule, usually over multiple runs.
   */
  public AbstractionRuleStatistics getStatistics()
  {
    final AbstractionRuleStatistics stats =
        new AbstractionRuleStatistics(this.toString());
    stats.setApplicationCount(mAppliedCount);
    stats.setInputStates(mInputStates);
    stats.setInputTransitions(mInputTransitions);
    stats.setOutputStates(mOutputStates);
    stats.setOutputTransitions(mOutputTransitions);
    stats.setReductionCount(mReductionCount);
    stats.setRunTime(mRunTime);
    stats.setUnchangedStates(mUnchangedStates);
    stats.setUnchangedTransitions(mUnchangedTransitions);
    stats.setRunTime(mRunTime);
    return stats;
  }

  // #######################################################################
  // # Invocation
  CompositionalGeneralisedConflictChecker.Step applyRuleAndCreateStep(
                                                                      final CompositionalGeneralisedConflictChecker checker,
                                                                      final AutomatonProxy autToAbstract,
                                                                      final EventProxy tau)
      throws AnalysisException
  {
    final AutomatonProxy abstractedAut =
        applyRuleToAutomaton(autToAbstract, tau);
    if (abstractedAut != autToAbstract) {
      return createStep(checker, abstractedAut);
    } else {
      return null;
    }
  }

  // #######################################################################
  // # Rule Application
  /**
   * Applies this rule to the given automaton. Also accumulates statistics for
   * successive runs.
   */
  public AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                                  final EventProxy tau)
      throws AnalysisException
  {
    final long startTime = System.currentTimeMillis();

    mAppliedCount++;
    final AutomatonProxy abstractedAut =
        applyRuleToAutomaton(autToAbstract, tau);
    if (autToAbstract != abstractedAut) {
      mReductionCount++;
      mInputStates += autToAbstract.getStates().size();
      mOutputStates += abstractedAut.getStates().size();
      mInputTransitions += autToAbstract.getTransitions().size();
      mOutputTransitions += abstractedAut.getTransitions().size();
    } else {
      mUnchangedStates += autToAbstract.getStates().size();
      mUnchangedTransitions += autToAbstract.getTransitions().size();
    }
    mRunTime += (System.currentTimeMillis() - startTime);
    return abstractedAut;
  }

  abstract AutomatonProxy applyRuleToAutomaton(
                                               final AutomatonProxy autToAbstract,
                                               final EventProxy tau)
      throws AnalysisException;

  abstract CompositionalGeneralisedConflictChecker.Step createStep(
                                                                   final CompositionalGeneralisedConflictChecker checker,
                                                                   final AutomatonProxy abstractedAut);

  // #########################################################################
  // # Logging
  Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }

  // #######################################################################
  // # Data Members
  private final ProductDESProxyFactory mFactory;
  private Collection<EventProxy> mPropositions;
  private long mRunTime;
  private int mAppliedCount;
  private int mReductionCount;
  private int mInputStates;
  private int mOutputStates;
  private int mInputTransitions;
  private int mOutputTransitions;
  private int mUnchangedStates;
  private int mUnchangedTransitions;
}
