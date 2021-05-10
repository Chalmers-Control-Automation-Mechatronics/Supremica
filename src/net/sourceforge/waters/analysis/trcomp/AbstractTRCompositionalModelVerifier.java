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

package net.sourceforge.waters.analysis.trcomp;

import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.compositional.CompositionalVerificationResult;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A general compositional model verifier to be subclassed for different
 * algorithms. This class extends the abstract compositional model analyser
 * class ({@link AbstractTRCompositionalModelAnalyzer} to implement the
 * {@link ModelVerifier} interface and provide counterexample support.
 *
 * @author Robi Malik
 */

public abstract class AbstractTRCompositionalModelVerifier
  extends AbstractTRCompositionalModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractTRCompositionalModelVerifier(final ProductDESProxy model,
                                              final KindTranslator translator,
                                              final ModelVerifier mono)
  {
    super(model, translator, mono);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyzer
  @Override
  public CompositionalVerificationResult getAnalysisResult()
  {
    return (CompositionalVerificationResult) super.getAnalysisResult();
  }

  @Override
  public CompositionalVerificationResult createAnalysisResult()
  {
    return new CompositionalVerificationResult(getClass());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelVerifier
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
  public boolean isSatisfied()
  {
    final AnalysisResult result = getAnalysisResult();
    return result.isSatisfied();
  }


  //#########################################################################
  //# Configuration
  public ModelVerifier getMonolithicVerifier()
  {
    return (ModelVerifier) getMonolithicAnalyzer();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, TRCompositionalModelAnalyzerFactory.
                        OPTION_AbstractTRCompositionalModelVerifier_OutputCheckingEnabled);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ModelVerifier_ShortCounterExampleRequested);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ModelVerifier_DetailedOutputEnabled);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_ModelVerifier_DetailedOutputEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setDetailedOutputEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelVerifier_OutputCheckingEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setOutputCheckingEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ModelVerifier mono = getMonolithicVerifier();
    mono.setCounterExampleEnabled(isCounterExampleEnabled());
  }


  //#########################################################################
  //# Hooks
  protected abstract TRTraceProxy createEmptyTrace(ProductDESProxy des);

  protected void checkIntermediateCounterExample(final TRTraceProxy trace)
    throws AnalysisException
  {
  }

  /**
   * Stores a verification result indicating that the property checked
   * is not satisfied and marks the run as completed.
   * @param  counterexample The counterexample obtained by verification.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final CounterExampleProxy counterexample)
  {
    final VerificationResult result = getAnalysisResult();
    result.setCounterExample(counterexample);
    return setBooleanResult(false);
  }

  @Override
  protected TRTraceProxy computeCounterExample() throws AnalysisException
  {
    final CompositionalVerificationResult result = getAnalysisResult();
    if (!result.isSatisfied() && isCounterExampleEnabled()) {
      final Logger logger = LogManager.getLogger();
      logger.debug("Starting trace expansion ...");
      final long start = System.currentTimeMillis();
      getSpecialEventsListener().setEnabled(true);
      final ProductDESProxy des = getModel();
      final TRTraceProxy trace = createEmptyTrace(des);
      final List<TRAbstractionStep> seq = getAbstractionSequence();
      final int end = seq.size();
      final ListIterator<TRAbstractionStep> iter = seq.listIterator(end);
      while (iter.hasPrevious()) {
        checkAbort();
        final TRAbstractionStep step = iter.previous();
        step.reportExpansion();
        step.expandTrace(trace, this);
        if (isOutputCheckingEnabled()) {
          checkIntermediateCounterExample(trace);
        }
        iter.remove();
      }
      final long stop = System.currentTimeMillis();
      result.setCounterExample(trace);
      result.setCounterExampleTime(stop - start);
      return trace;
    } else {
      return null;
    }
  }

}
