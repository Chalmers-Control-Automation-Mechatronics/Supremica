//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.waters;

import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.PropertySuppressionKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.AutomataSynchronizerHelperStatistics;
import org.supremica.automata.algorithms.AutomataVerifier;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.VerificationOptions;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;


/**
 * <P>A wrapper to invoke Supremica's monolithic verification algorithms
 * implemented in {@link AutomataVerifier} through the {@link ModelVerifier}
 * interface of Waters.</P>
 *
 * @author Robi Malik
 */

public  class SupremicaMonolithicVerifier
  extends SupremicaModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public SupremicaMonolithicVerifier(final ProductDESProxy model,
                                     final ProductDESProxyFactory factory,
                                     final VerificationType type,
                                     final boolean ensuringUncontrollablesInPlant)
  {
    super(model, factory,
          PropertySuppressionKindTranslator.getInstance(),
          ensuringUncontrollablesInPlant);
    mVerificationOptions.setVerificationType(type);
    mVerificationOptions.setAlgorithmType(VerificationAlgorithm.MONOLITHIC);
    mVerificationOptions.setShowBadTrace(isDetailedOutputEnabled());
    mVerificationOptions.setReachabilityStateLimit(getNodeLimit());
  }


  //#########################################################################
  //# Overrides for
  //# org.supremica.automata.waters.SupremicaModelAnalyzer
  @Override
  public void setSynchronisingOnUnobservableEvents(final boolean sync)
  {
    mSynchronizationOptions.setUnobsEventsSynch(sync);
  }

  @Override
  public boolean isSynchronisingOnUnobservableEvents()
  {
    return mSynchronizationOptions.getUnobsEventsSynch();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelVerifier
  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    return isDetailedOutputEnabled();
  }

  @Override
  public void setShortCounterExampleRequested(final boolean req)
  {
  }

  @Override
  public boolean isShortCounterExampleRequested()
  {
    return false;
  }

  @Override
  public boolean isSatisfied()
  {
    final VerificationResult result = getAnalysisResult();
    return result.isSatisfied();
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }

  @Override
  public VerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult(this);
  }

  @Override
  public CounterExampleProxy getCounterExample()
  {
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelAnalyzer
  @Override
  public void setDetailedOutputEnabled(final boolean enable)
  {
    super.setDetailedOutputEnabled(enable);
    mVerificationOptions.setShowBadTrace(enable);
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    super.setNodeLimit(limit);
    mVerificationOptions.setReachabilityStateLimit(limit);
  }

  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    final ListIterator<Parameter> iter = list.listIterator();
    while (iter.hasNext()) {
      final Parameter param = iter.next();
      if (param.getID() == ParameterIDs.ModelAnalyzer_DetailedOutputEnabled) {
        param.setName("Print counterexample");
        param.setDescription("Show trace to bad state as info in log.");
        break;
      }
    }
    return list;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer
  /**
   * Returns whether or not this model analyser supports nondeterministic
   * automata.
   * @return <CODE>false</CODE> as Supremica's verification does not support
   *         nondeterminism.
   */
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final Automata automata = getSupremicaAutomata();
      final AutomataVerifier verifier =
        new AutomataVerifier(automata, mVerificationOptions,
                             mSynchronizationOptions, mMinimizationOptions);
      setSupremicaTask(verifier);
      final boolean satisfied = verifier.verify();
      final AnalysisResult result = getAnalysisResult();
      result.setSatisfied(satisfied);
      final AutomataSynchronizerHelperStatistics stats =
        verifier.getHelper().getHelperData();
      result.setNumberOfStates(stats.getNumberOfReachableStates());
      result.setNumberOfTransitions(stats.getNumberOfExaminedTransitions());
      return satisfied;
    } catch (final OutOfMemoryError error) {
      System.gc();
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Data Members
  private final VerificationOptions mVerificationOptions =
    new VerificationOptions();
  private final SynchronizationOptions mSynchronizationOptions =
    SynchronizationOptions.getDefaultVerificationOptions();
  private final MinimizationOptions mMinimizationOptions =
    MinimizationOptions.getDefaultVerificationOptions();

}
