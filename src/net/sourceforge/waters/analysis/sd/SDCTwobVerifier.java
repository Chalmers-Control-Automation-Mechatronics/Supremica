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

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

/**
 * A model verifier to check SD point ii.b.
 *
 * The check is done by converting the verification problem to an
 * equivalent nonblocking verification problem, and passing that to a
 * conflict checker.
 *
 * @see SDPropertyBuilder
 * @see ConflictChecker
 *
 * @author Robi Malik, Mahvash Baloch
 */

public class SDCTwobVerifier extends AbstractSDConflictChecker
{

  //#########################################################################
  //# Constructors
  public SDCTwobVerifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SDCTwobVerifier(final ConflictChecker checker,
                              final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SDCTwobVerifier( final ConflictChecker checker,
                                     final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Invocation
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

      final SDCTwoPropertyBuilder builder =
        new SDCTwoPropertyBuilder(model, getFactory());


      ProductDESProxy convertedModel = null;
      convertedModel = builder.createSDTwo_bModel();

      final ConflictChecker checker = getConflictChecker();
      checker.setModel(convertedModel);

      final VerificationResult result;
      try {
        checker.run();
      } finally {
        result = checker.getAnalysisResult();
        setAnalysisResult(result);
                  }
      if (result.isSatisfied()) {
        return true;
      } else {
        final ConflictCounterExampleProxy counterexample =
          checker.getCounterExample();
        return setFailedResult(counterexample);
      }
    } finally {
      tearDown();
    }
  }



//#########################################################################
//# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
@Override
public boolean supportsNondeterminism()
{
  return false;
}

}
