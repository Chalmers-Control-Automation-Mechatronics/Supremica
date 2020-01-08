//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import net.sourceforge.waters.analysis.compositional.CompositionalVerificationResult;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A general delegating compositional model verifier to be subclassed for
 * different algorithms. This class extends the abstract delegating
 * compositional model analyser class ({@link AbstractTRDelegatingAnalyzer}
 * to implement the {@link ModelVerifier} interface and provide counterexample
 * support.
 *
 * @author Robi Malik
 */

public abstract class AbstractTRDelegatingVerifier
  extends AbstractTRDelegatingAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractTRDelegatingVerifier(final ProductDESProxy model,
                                      final KindTranslator translator,
                                      final AbstractTRAnalyzer delegate)
  {
    super(model, translator, delegate);
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
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelVerifier_ShortCounterExampleRequested)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setShortCounterExampleRequested(boolOption.getBooleanValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelVerifier_OutputCheckingEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setOutputCheckingEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
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
  public void setShortCounterExampleRequested(final boolean req)
  {
    final ModelVerifier delegate = (ModelVerifier) getDelegate();
    delegate.setShortCounterExampleRequested(req);
  }

  @Override
  public boolean isShortCounterExampleRequested()
  {
    final ModelVerifier delegate = (ModelVerifier) getDelegate();
    return delegate.isShortCounterExampleRequested();
  }

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
  //# Auxiliary Methods
  /**
   * Stores a verification result indicating that the property checked
   * is satisfied and marks the run as completed.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    return setBooleanResult(true);
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

}
