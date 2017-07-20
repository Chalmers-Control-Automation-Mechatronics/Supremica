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

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * <P>A model verifier to check SD Controllability (iii.1) Property.</P>
 *
 * <P>This wrapper can be used to check whether a model satisfies SD Property
 * (iii.1) The check is done by creating a test automata and modifying Plant
 * automata for each prohibitable event in the model, and passing these models
 * to a modular language inclusion checker.</P>
 *
 * @see SDPropertyBuilder
 * @see net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker
 *      ModularLanguageInclusionChecker
 *
 * @author Mahvash Baloch, Robi Malik
 */

public class SDThreeOneVerifier extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDThreeOneVerifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SDThreeOneVerifier(final LanguageInclusionChecker checker,
                            final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SDThreeOneVerifier(final LanguageInclusionChecker checker,
                            final ProductDESProxy model,
                            final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();
      final Collection<AutomatonProxy> oldAutomata = model.getAutomata();
      final int numaut = oldAutomata.size();
      if (numaut == 0) {
        return setSatisfiedResult();
      }
      final LanguageInclusionChecker cChecker = getLanguageInclusionChecker();
      final SDCThree1AVerifier verifier1 =
        new SDCThree1AVerifier(cChecker, model, getFactory());
      final VerificationResult result1;
      try {
        verifier1.run();
      } finally {
        result1 = verifier1.getAnalysisResult();
      }
      if (!result1.isSatisfied()) {
        final SafetyTraceProxy counterexample = verifier1.getCounterExample();
        return setFailedResult(counterexample);
      }
      final SDCThree1BVerifier verifier2 =
        new SDCThree1BVerifier(cChecker, model, getFactory());
      final VerificationResult result2;
      try {
        verifier2.run();
      } finally {
        result2 = verifier2.getAnalysisResult();
      }
      if (!result2.isSatisfied()) {
        final SafetyTraceProxy counterexample = verifier2.getCounterExample();
        return setFailedResult(counterexample);
      }
      result2.merge(result1);
      return setSatisfiedResult();

    } finally {
      tearDown();
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

}
