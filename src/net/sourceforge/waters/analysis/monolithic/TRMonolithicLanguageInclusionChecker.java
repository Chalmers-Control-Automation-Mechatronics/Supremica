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

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;


/**
 * A Java implementation of the monolithic language inclusion check algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Robi Malik
 */

public class TRMonolithicLanguageInclusionChecker
  extends TRMonolithicSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public TRMonolithicLanguageInclusionChecker()
  {
    super(LanguageInclusionKindTranslator.getInstance(),
          LanguageInclusionDiagnostics.getInstance());
  }

  public TRMonolithicLanguageInclusionChecker(final ProductDESProxy model)
  {
    super(model,
          LanguageInclusionKindTranslator.getInstance(),
          LanguageInclusionDiagnostics.getInstance());
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.monolithic.
  //# AbstractTRMonolithicModelAnalyzer
  @Override
  protected void setUpAutomata()
    throws AnalysisException
  {
    super.setUpAutomata();
    final int numAut = getTRAutomata().length;
    int failedSpec = -1;
    for (int a = 0; a < numAut; a++) {
      final TRAutomatonProxy aut = getTRAutomaton(a);
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.getFirstInitialState() < 0) {
        final KindTranslator translator = getKindTranslator();
        if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
          setSatisfiedResult();
          return;
        } else {
          failedSpec = a;
        }
      }
    }
    if (failedSpec >= 0) {
      final SafetyCounterExampleProxy counterExample =
        buildCounterExample(-1, -1, failedSpec);
      setFailedResult(counterExample);
    }
  }

}
