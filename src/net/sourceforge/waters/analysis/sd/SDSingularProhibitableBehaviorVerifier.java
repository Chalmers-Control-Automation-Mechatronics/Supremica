//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;


/**
 * <P>A model verifier to check the property of SD singular prohibitable
 * behaviour.</P>
 *
 * <P>The check is done by creating a test automaton and modifying plant
 * automata each prohibitable event in the model, and passing these models
 * to a language inclusion checker.</P>
 *
 * <P><STRONG>Reference.</STRONG> Mahvash Baloch. A compositional approach for
 * verifying sampled-data supervisory control. M.Sc. Thesis, Dept. of
 * Computing and Software, McMaster University, March 2012.</P>
 *
 * @see SDPropertyBuilder
 * @see LanguageInclusionChecker
 *
 * @author Mahvash Baloch , Robi Malik
 */

public class SDSingularProhibitableBehaviorVerifier
  extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDSingularProhibitableBehaviorVerifier
    (final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SDSingularProhibitableBehaviorVerifier
    (final LanguageInclusionChecker checker,
     final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SDSingularProhibitableBehaviorVerifier
    (final LanguageInclusionChecker checker,
     final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mFailedProhibitable = null;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();
      final SDPropertyBuilder builder =
        new SDPropertyBuilder(model, getFactory());
      final Collection<EventProxy> hibs = builder.getHibEvents();
      for (final EventProxy hib : hibs) {
        logHibEvent(hib, hibs);
        final ProductDESProxy convertedModel = builder.createSingularModel(hib);
        final LanguageInclusionChecker checker = getLanguageInclusionChecker();
        checker.setModel(convertedModel);
        try {
          checker.run();
        } finally {
          final VerificationResult result = checker.getAnalysisResult();
          recordStatistics(result);
        }
        final VerificationResult result = getAnalysisResult();
        if (!result.isSatisfied()) {
          final SafetyCounterExampleProxy counterexample =
            checker.getCounterExample();
          mFailedProhibitable = hib;
          return setFailedResult(counterexample);
        }
      }
      return setSatisfiedResult();
    } finally {
      tearDown();
    }
  }

  public EventProxy getFailedProhibitable()
  {
    return mFailedProhibitable;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mFailedProhibitable;

}
