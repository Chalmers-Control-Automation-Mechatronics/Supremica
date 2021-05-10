//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Rachel Francis
 */

public abstract class AbstractionRule
  implements Abortable
{
  //#######################################################################
  //# Constructor
  AbstractionRule(final ProductDESProxyFactory factory,
                  final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  AbstractionRule(final ProductDESProxyFactory factory,
                  final KindTranslator translator,
                  final Collection<EventProxy> propositions)
  {
    mFactory = factory;
    mKindTranslator = translator;
    mPropositions = propositions;
  }


  //#######################################################################
  //# Configuration
  ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }

  void setPropositions(final Collection<EventProxy> props)
  {
    mPropositions = props;
  }

  @Override
  public String toString()
  {
    return ProxyTools.getShortClassName(this);
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Returns a statistics object containing the statistics for the performance
   * of this rule, usually over multiple runs.
   */
  public AbstractionRuleStatistics getStatistics()
  {
    final AbstractionRuleStatistics stats =
        new AbstractionRuleStatistics(getClass());
    stats.setApplicationCount(mAppliedCount);
    stats.setInputStates(mInputStates);
    stats.setInputTransitions(mInputTransitions);
    stats.setOutputStates(mOutputStates);
    stats.setOutputTransitions(mOutputTransitions);
    stats.setReductionCount(mReductionCount);
    stats.setUnchangedStates(mUnchangedStates);
    stats.setUnchangedTransitions(mUnchangedTransitions);
    stats.setRunTime(mRunTime);
    return stats;
  }


  //#######################################################################
  //# Invocation
  CompositionalGeneralisedConflictChecker.Step applyRuleAndCreateStep(
                                                                      final CompositionalGeneralisedConflictChecker checker,
                                                                      final AutomatonProxy autToAbstract,
                                                                      final EventProxy tau)
      throws AnalysisException
  {
    final AutomatonProxy abstractedAut = applyRule(autToAbstract, tau);
    if (abstractedAut != autToAbstract) {
      return createStep(checker, abstractedAut);
    } else {
      return null;
    }
  }


  //#######################################################################
  //# Rule Application
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

  public abstract void cleanup();


  //########################################################################
  //# Logging
  Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return LogManager.getLogger(clazz);
  }


  //#######################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final KindTranslator mKindTranslator;
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
